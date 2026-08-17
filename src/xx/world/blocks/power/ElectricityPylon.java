package xx.world.blocks.power;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons2;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Intersector;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.BeamNode;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.PowerModule;
import xx.world.meta.xx_Stat;


import java.util.Arrays;

import static mindustry.Vars.*;

public class ElectricityPylon extends BeamNode {

    public boolean irresistible = true;         //塔与塔间连接不可被塑钢墙阻挡，即势不可挡

    public float laserRange = 6;                //PB激光范围，一般是要比range小的
    public boolean sameBlockConnection = false;
    public boolean drawRange = true;
    public int maxNodes = 3;                    //最大PB连接数
    public float laserScale = 0.25f;            //PB激光宽度


    public ElectricityPylon (String name){
        super(name);
        configurable = true;
        ignoreResizeConfig = true;
        schematicPriority = -10;                //蓝图建筑优先度
        //关于配置模式
        config(Integer.class, (entity, value) -> {
            PowerModule power = entity.power;
            Building other = world.build(value);
            boolean contains = power.links.contains(value), valid = other != null && other.power != null;
            int nonPylonLinks = 0;
            for (int i = 0; i < power.links.size; i++) {
                int linkPos = power.links.get(i);
                Building link = world.build(linkPos);
                if (link != null && !(link.block instanceof ElectricityPylon)) {
                    nonPylonLinks++;
                }
            }
            //Log.info("连接别数: @" + nonPylonLinks);


            if(contains){
                //unlink
                power.links.removeValue(value);
                if(valid) other.power.links.removeValue(entity.pos());

                PowerGraph newgraph = new PowerGraph();

                //reflow from this point, covering all tiles on this side
                newgraph.reflow(entity);

                newgraph.update();

                if(valid && other.power.graph != newgraph){
                    //create new graph for other end
                    PowerGraph og = new PowerGraph();
                    //reflow from other end
                    og.reflow(other);

                    og.update();
                }
            }else if(linkValid(entity, other) && valid && nonPylonLinks < maxNodes){

                power.links.addUnique(other.pos());

                if(other.team == entity.team){
                    other.power.links.addUnique(entity.pos());
                }

                power.graph.addGraph(other.power.graph);
            }
        });
        //复制保存连接
        config(Point2[].class, (tile, value) -> {
            IntSeq old = new IntSeq(tile.power.links);
            @SuppressWarnings("unchecked")
            Cons2<Building, Integer> intHandler = configurations.get(Integer.class);
            for(int i = 0; i < old.size; i++){
                intHandler.get(tile, old.get(i));
            }
            for(Point2 p : value){
                intHandler.get(tile, Point2.pack(p.x + tile.tileX(), p.y + tile.tileY()));
            }
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("connections", entity -> new Bar(
                () -> {
                    int nonPylonLinks = 0;
                    for (int i = 0; i < entity.power.links.size; i++) {
                        Building link = world.build(entity.power.links.get(i));
                        if (link != null && !(link.block instanceof ElectricityPylon)) {
                            nonPylonLinks++;
                        }
                    }
                    return Core.bundle.format("bar.powerlines", nonPylonLinks, maxNodes);
                },
                () -> Pal.items,
                () -> {
                    int nonPylonLinks = 0;
                    for (int i = 0; i < entity.power.links.size; i++) {
                        Building link = world.build(entity.power.links.get(i));
                        if (link != null && !(link.block instanceof ElectricityPylon)) {
                            nonPylonLinks++;
                        }
                    }
                    return (float) nonPylonLinks / (float) maxNodes;
                }
        ));
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(xx_Stat.pbrang, laserRange, StatUnit.blocks);
        stats.add(Stat.powerConnections, maxNodes, StatUnit.none);
    }

    //绘制圆环
    public void drawCircle (int x , int y){
        Lines.stroke(1f);
        Draw.color(Pal.placing);
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, laserRange * tilesize);
        Draw.reset();
    }

    @Override//改写建造预览时的虚线绘制
    public void drawPlace(int x, int y, int rotation, boolean valid){
        for(int i = 0; i < 4; i++){
            int maxLen = range + size/2;
            Building dest = null;
            var dir = Geometry.d4[i];
            int dx = dir.x, dy = dir.y;
            int offset = size/2;
            for(int j = 1 + offset; j <= range + offset; j++){
                var other = world.build(x + j * dir.x, y + j * dir.y);

                //hit insulated wall
                if(!irresistible && other != null && other.isInsulated()){
                    break;
                }

                if(other != null && other.block instanceof ElectricityPylon && other.team == Vars.player.team()){
                    maxLen = j;
                    dest = other;
                    break;
                }
            }

            Drawf.dashLine(Pal.placing,
                    x * tilesize + dx * (tilesize * size / 2f + 2),
                    y * tilesize + dy * (tilesize * size / 2f + 2),
                    x * tilesize + dx * (maxLen) * tilesize,
                    y * tilesize + dy * (maxLen) * tilesize
            );

            if(dest != null){
                Drawf.square(dest.x, dest.y, dest.block.size * tilesize/2f + 2.5f, 0f);
            }
        }
        drawCircle(x,y);
    }

    protected boolean overlaps(float srcx, float srcy, Tile other, Block otherBlock, float range){
        return Intersector.overlaps(Tmp.cr1.set(srcx, srcy, range), Tmp.r1.setCentered(other.worldx() + otherBlock.offset, other.worldy() + otherBlock.offset,
                otherBlock.size * tilesize, otherBlock.size * tilesize));
    }

    protected boolean overlaps(float srcx, float srcy, Tile other, float range){
        return Intersector.overlaps(Tmp.cr1.set(srcx, srcy, range), other.getHitbox(Tmp.r1));
    }

    protected boolean overlaps(Building src, Building other, float range){
        return overlaps(src.x, src.y, other.tile, range);
    }

    protected boolean overlaps(Tile src, Tile other, float range){
        return overlaps(src.drawx(), src.drawy(), other, range);
    }
    //计算建筑是否在圆周范围内，圆与方形是否相切
    public boolean overlaps(@Nullable Tile src, @Nullable Tile other){
        if(src == null || other == null) return true;
        return Intersector.overlaps(Tmp.cr1.set(src.worldx() + offset, src.worldy() + offset, laserRange * tilesize), Tmp.r1.setSize(size * tilesize).setCenter(other.worldx() + offset, other.worldy() + offset));
    }

    public boolean linkValid(Building tile, Building link){
        return linkValid(tile, link, true);
    }
    //检测是否能连接
    public boolean linkValid(Building tile, Building link, boolean checkMaxNodes){

        if(tile == link || link == null || link.block instanceof ElectricityPylon || !link.block.hasPower || !link.block.connectedPower || tile.team != link.team || (sameBlockConnection && tile.block != link.block)) return false;

        if(overlaps(tile, link, laserRange * tilesize) || (link.block instanceof PowerNode node && overlaps(link, tile, node.laserRange * tilesize))){
            if(checkMaxNodes && link.block instanceof PowerNode node){
                return link.power.links.size < node.maxNodes || link.power.links.contains(tile.pos());
            }
            return true;
        }
        return false;
    }

    private static int currentFindX, currentFindY;
    private static BuildPlan currentPlan;
    private static final Boolf<BuildPlan> planFinder = other -> other.block != null
            && (currentFindX >= other.x - ((other.block.size - 1) / 2) && currentFindY >= other.y - ((other.block.size - 1) / 2) && currentFindX <= other.x + other.block.size / 2 && currentFindY <= other.y + other.block.size / 2)
            && other != currentPlan && other.block.hasPower;

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        if(plan.config instanceof Point2[] ps){
            Draw.color(laserColor1, laserColor2,Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);

            for(Point2 point : ps){
                currentFindX = plan.x + point.x;
                currentFindY = plan.y + point.y;
                currentPlan = plan;

                var otherReq = findPlan(list, currentFindX, currentFindY, planFinder);

                if(otherReq == null || otherReq.block == null) continue;

                drawPBLaser(plan.drawx(), plan.drawy(), otherReq.drawx(), otherReq.drawy(), size, otherReq.block.size, false);
            }
            Draw.color();
        }
    }

    public void drawPBLaser(float x1, float y1, float x2, float y2, int size1, int size2){
        drawPBLaser(x1, y1, x2, y2, size1, size2, true);
    }

    public void drawPBLaser(float x1, float y1, float x2, float y2, int size1, int size2, boolean light){
        float w = laserScale + Mathf.absin(pulseScl, pulseMag);
        float angle1 = Angles.angle(x1, y1, x2, y2),
                vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
                len1 = size1 * tilesize / 2f - 1.5f, len2 = size2 * tilesize / 2f - 1.5f;

        Drawf.laser(laser, laserEnd, laserEnd, x1 + vx*len1, y1 + vy*len1, x2 - vx*len2, y2 - vy*len2, w, light);
    }


    //TODO 仅手动连接塔与非塔，为了电压机制，当然更多的是我懒
    public class BeamNodeBuild extends Building{

        //current links in cardinal directions
        public Building[] links = new Building[4];
        public Tile[] dests = new Tile[4];
        public int lastChange = -2;

        //这好像是用于检察是否可以连到目标
        /** @return whether a beam could theoretically connect with the specified block at a position */
        public boolean couldConnect(int direction, Block target, int targetX, int targetY){
            int offset = -(target.size - 1) / 2;
            int minX = targetX + offset, minY = targetY + offset, maxX = targetX + offset + target.size - 1, maxY = targetY + offset + target.size - 1;
            var dir = Geometry.d4[direction];

            int rangeOffset = size/2;

            //find first block with power in range
            for(int j = 1 + rangeOffset; j <= range + rangeOffset; j++){
                var other = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);

                if(other == null) return false;

                //hit insulated wall
                if((other.build != null && other.build.isInsulated()) || (other.block() instanceof ElectricityPylon && other.team() == team)){
                    return false;
                }

                //within target rectangle
                if(other.x >= minX && other.y >= minY && other.x <= maxX && other.y <= maxY){
                    return true;
                }
            }

            return false;
        }

        @Override//更新
        public void updateTile(){
            //TODO this block technically does not need to update every frame, perhaps put it in a special list.
            if(lastChange != world.tileChanges){
                lastChange = world.tileChanges;
                updateDirections();
            }
        }

        @Override//根据电网调整自身状态，状态
        public BlockStatus status(){
            float balance = power.graph.getPowerBalance();
            if(balance > 0f) return BlockStatus.active;
            if(balance < 0f && power.graph.getLastPowerStored() > 0) return BlockStatus.noOutput;
            return BlockStatus.noInput;
        }

        @Override//每帧更新
        public void draw(){
            PPdraw();
            PBdraw();
        }
        //仅绘制塔与塔间连接
        public void PPdraw(){
            super.draw();

            if(Mathf.zero(Renderer.laserOpacity) || team == Team.derelict) return;

            Draw.z(Layer.power);
            Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);
            float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

            for(int i = 0; i < 4; i ++){
                if(dests[i] != null && links[i].wasVisible && links[i].block instanceof ElectricityPylon pylon &&
                        ((links[i].tileX() != tileX() && links[i].tileY() != tileY()) ||
                                (links[i].id > id && range >= pylon.range) || range > pylon.range)){

                    int dst = Math.max(Math.abs(dests[i].x - tile.x),  Math.abs(dests[i].y - tile.y));
                    //don't draw lasers for adjacent blocks
                    if(dst > 1 + size/2){
                        var point = Geometry.d4[i];
                        float poff = tilesize/2f;
                        Drawf.laser(laser, laserEnd, x + poff*size*point.x, y + poff*size*point.y, dests[i].worldx() - poff*point.x, dests[i].worldy() - poff*point.y, w);
                    }
                }
            }

            Draw.reset();


        }
        //仅绘制塔与非塔间连接
        public void PBdraw(){
            if(Mathf.zero(Renderer.laserOpacity) || isPayload() || team == Team.derelict) return;

            Draw.z(Layer.power);
            Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);

            for(int i = 0; i < power.links.size; i++){
                Building link = world.build(power.links.get(i));

                if(!linkValid(this, link)) continue;

                if(link.block instanceof PowerNode && link.id >= id) continue;

                drawPBLaser(x, y, link.x, link.y, size, link.block.size);
            }

            Draw.reset();
        }

        @Override
        public void pickedUp(){
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
        }
        //关于更新连接
        public void updateDirections(){
            for(int i = 0; i < 4; i ++){
                var prev = links[i];
                var dir = Geometry.d4[i];
                links[i] = null;
                dests[i] = null;
                int offset = size/2;
                //find first block with power in range
                for(int j = 1 + offset; j <= range + offset; j++){
                    var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

                    //hit insulated wall
                    if(!irresistible && other != null && other.isInsulated()){
                        break;
                    }

                    //power nodes do NOT play nice with beam nodes, do not touch them as that forcefully modifies their links
                    if(other != null && other.block instanceof ElectricityPylon && other.team == team){
                        links[i] = other;
                        dests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
                        break;
                    }
                }

                var next = links[i];

                if(next != prev){
                    //unlinked, disconnect and reflow
                    if(prev != null && prev.isAdded()){
                        prev.power.links.removeValue(pos());
                        power.links.removeValue(prev.pos());

                        PowerGraph newgraph = new PowerGraph();
                        //reflow from this point, covering all tiles on this side
                        newgraph.reflow(this);
                        

                        if(prev.power.graph != newgraph){
                            //reflow power for other end
                            PowerGraph og = new PowerGraph();
                            og.reflow(prev);
                        }
                    }

                    //linked to a new one, connect graphs
                    if(next != null){

                        power.links.addUnique(next.pos());
                        next.power.links.addUnique(pos());

                        power.graph.addGraph(next.power.graph);
                    }
                }
            }
        }

        @Override//进入配置后点击可连接方块后触发
        public boolean onConfigureBuildTapped(Building other){
            //Log.info("连接总数: @" + power.links.size);
            if(linkValid(this, other)){
                configure(other.pos());
                return false;
            }
            return true;
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));

            if(drawRange){
                Drawf.circles(x, y, laserRange * tilesize);

                for(int x = (int)(tile.x - laserRange - 2); x <= tile.x + laserRange + 2; x++){
                    for(int y = (int)(tile.y - laserRange - 2); y <= tile.y + laserRange + 2; y++){
                        Building link = world.build(x, y);

                        if(link != this && linkValid(this, link, false)){
                            boolean linked = linked(link);

                            if(linked){
                                Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                            }
                        }
                    }
                }

                Draw.reset();
            }else{
                power.links.each(i -> {
                    var link = world.build(i);
                    if(link != null && linkValid(this, link, false)){
                        Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                    }
                });
            }
        }

        protected boolean linked(Building other){
            return power.links.contains(other.pos());
        }

        @Override
        public Point2[] config(){
            int count = 0;
            for (int i = 0; i < power.links.size; i++) {
                Building link = world.build(power.links.get(i));
                if (link != null && !(link.block instanceof ElectricityPylon)) {
                    count++;
                }
            }

            Point2[] out = new Point2[count];
            int index = 0;
            for (int i = 0; i < power.links.size; i++) {
                Building link = world.build(power.links.get(i));
                if (link != null && !(link.block instanceof ElectricityPylon)) {
                    out[index++] = Point2.unpack(power.links.get(i)).sub(tile.x, tile.y);
                }
            }
            return out;
        }
    }
}
