package xx.world.blocks.power;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Time;
import mindustry.core.Renderer;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.Env;
import mindustry.world.modules.PowerModule;
import xx.gen.xx_Building;
import xx.world.modules.xx_PowerModule;

import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class text_node2 extends PowerNode{

    public float resistance;//TODO电阻，视为纯电阻，当然后面可以做升电压降电阻

    public text_node2(String name){
        super(name);
        resistance = 1;
        configurable = true;
        ignoreResizeConfig = true;
        consumesPower = false;
        outputsPower = false;
        canOverdrive = false;
        swapDiagonalPlacement = true;
        schematicPriority = -10;
        drawDisabled = false;
        envEnabled |= Env.space;
        destructible = true;
        delayLandingConfig = true;
        drawCached = true;

        connectedPower = false;

        //nodes do not even need to update
        update = false;

        config(Integer.class, (entity, value) -> {
            PowerModule power =  entity.power;
            Building other =  world.build(value);
            boolean contains = power.links.contains(value), valid = other != null && other.power != null;

            if(contains){
                //unlink
                power.links.removeValue(value);
                if(valid) other.power.links.removeValue(entity.pos());

                xx_PowerGraph newgraph = new xx_PowerGraph();

                //reflow from this point, covering all tiles on this side
                newgraph.reflow(entity);

                newgraph.update();

                if(valid && other.power.graph != newgraph){
                    //create new graph for other end
                    xx_PowerGraph og = new xx_PowerGraph();
                    //reflow from other end
                    og.reflow(other);

                    og.update();
                }
            }else if(linkValid(entity, other) && valid && power.links.size < maxNodes){

                power.links.addUnique(other.pos());

                if(other.team == entity.team){
                    other.power.links.addUnique(entity.pos());
                }

                power.graph.addGraph(other.power.graph);
            }
        });

        config(Point2[].class, (tile, value) -> {
            IntSeq old = new IntSeq(tile.power.links);

            //clear old
            for(int i = 0; i < old.size; i++){
                configurations.get(Integer.class).get(tile, old.get(i));
            }

            //set new
            for(Point2 p : value){
                configurations.get(Integer.class).get(tile, Point2.pack(p.x + tile.tileX(), p.y + tile.tileY()));
            }
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("power");
        removeBar("batteries");
        removeBar("connections");

        addBar("power", makePowerBalance());
        addBar("batteries", makeBatteryBalance());

        addBar("connections", entity -> new Bar(() ->
                Core.bundle.format("bar.powerlines", entity.power.links.size, maxNodes),
                () -> Pal.items,
                () -> (float)entity.power.links.size / (float)maxNodes
        ));
    }

    public static Func<Building, Bar> makePowerBalance(){
        return entity -> {
            xx_PowerGraph graph = (xx_PowerGraph) entity.power.graph;
            return new Bar(() ->
                    Core.bundle.format("bar.powerbalance2",
                            (graph.getPowerBalance() >= 0 ? "+" : "") +
                                    UI.formatAmount((long)graph.getPowerBalance()),
                            UI.formatAmount((long) graph.lineLossRate)
                    ),
                    () -> Pal.powerBar,
                    () -> Mathf.clamp(graph.getLastPowerProduced() / graph.getLastPowerNeeded())
            );
        };
    }



    @Override
    protected void getPotentialLinks(Tile tile, Team team, Cons<Building> others){
        if(!autolink) return;

        Boolf<Building> valid = other -> other != null && other.tile != tile && !other.block.connectedPower && other.power.graph instanceof xx_PowerGraph &&
                (other.block.outputsPower || other.block.consumesPower || other.block instanceof PowerNode || other.power instanceof xx_PowerModule) &&
                overlaps(tile.x * tilesize + offset, tile.y * tilesize + offset, other.tile, laserRange * tilesize) && other.team == team &&
                !graphs.contains(other.power.graph) &&
                !PowerNode.insulated(tile, other.tile) &&
                !(other instanceof PowerNodeBuild obuild && obuild.power.links.size >= ((PowerNode)obuild.block).maxNodes) &&
                !Structs.contains(Edges.getEdges(size), p -> { //do not link to adjacent buildings
                    var t = world.tile(tile.x + p.x, tile.y + p.y);
                    return t != null && t.build == other;
                });

        tempBuilds.clear();
        graphs.clear();

        //add conducting graphs to prevent double link
        for(var p : Edges.getEdges(size)){
            Tile other = tile.nearby(p);
            if(other != null && other.team() == team && other.build != null && other.build.power != null){
                graphs.add(other.build.power.graph);
            }
        }

        if(tile.build != null && tile.build.power != null){
            graphs.add(tile.build.power.graph);
        }

        var worldRange = laserRange * tilesize;
        var tree = team.data().buildingTree;
        if(tree != null){
            tree.intersect(tile.worldx() - worldRange, tile.worldy() - worldRange, worldRange * 2, worldRange * 2, build -> {
                if(valid.get(build) && !tempBuilds.contains(build)){
                    tempBuilds.add(build);
                }
            });
        }

        tempBuilds.sort((a, b) -> {
            int type = -Boolean.compare(a.block instanceof PowerNode, b.block instanceof PowerNode);
            if(type != 0) return type;
            return Float.compare(a.dst2(tile), b.dst2(tile));
        });

        returnInt = 0;

        tempBuilds.each(valid, t -> {
            if(returnInt ++ < maxNodes){
                graphs.add(t.power.graph);
                others.get(t);
            }
        });
    }


    @Override
    public boolean linkValid(Building tile, Building link, boolean checkMaxNodes){
        if(tile == link || link == null || !link.block.hasPower || !(!link.block.connectedPower && link.power.graph instanceof xx_PowerGraph) || tile.team != link.team || (sameBlockConnection && tile.block != link.block)) return false;

        if(overlaps(tile, link, laserRange * tilesize) || (link.block instanceof PowerNode node && overlaps(link, tile, node.laserRange * tilesize))){
            if(checkMaxNodes && link.block instanceof PowerNode node){
                return link.power.links.size < node.maxNodes || link.power.links.contains(tile.pos());
            }
            return true;
        }
        return false;
    }

    public class text_node2Build extends xx_Building {

        @Override
        public void created(){ // Called when one is placed/loaded in the world
            if(autolink && laserRange > maxRange) maxRange = laserRange;
            xx_PowerModule power = (xx_PowerModule) this.power;
            power.parallelConnection = false;
            super.created();
        }


        @Override
        public void placed(){
            if(net.client() || power.links.size > 0) return;

            getPotentialLinks(tile, team, other -> {
                if(!power.links.contains(other.pos())){
                    configureAny(other.pos());
                }
            });

            super.placed();
        }

        @Override
        public void dropped(){
            power.links.clear();
            updatePowerGraph();
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(linkValid(this, other)){
                configure(other.pos());
                return false;
            }

            if(this == other){ //double tapped
                if(other.power.links.size == 0){ //find links
                    Seq<Point2> points = new Seq<>();
                    getPotentialLinks(tile, team, link -> {
                        if(!insulated(this, link) && points.size < maxNodes){
                            points.add(new Point2(link.tileX() - tile.x, link.tileY() - tile.y));
                        }
                    });
                    configure(points.toArray(Point2.class));
                }else{ //clear links
                    configure(new Point2[0]);
                }
                deselect();
                return false;
            }

            return true;
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            if(!drawRange) return;

            Lines.stroke(1f);

            Draw.color(Pal.accent);
            Drawf.circles(x, y, laserRange * tilesize);
            Draw.reset();
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

        @Override
        public void drawCached(){
            super.draw();
        }

        @Override
        public void draw(){
            if(Mathf.zero(Renderer.laserOpacity) || isPayload() || team == Team.derelict) return;

            Draw.z(powerLayer);
            setupColor(power.graph.getSatisfaction());

            for(int i = 0; i < power.links.size; i++){
                Building link = world.build(power.links.get(i));

                if(!linkValid(this, link)) continue;

                if(link.block instanceof PowerNode && link.id >= id) continue;

                drawLaser(x, y, link.x, link.y, size, link.block.size);
            }

            Draw.reset();
        }

        protected boolean linked(Building other){
            return power.links.contains(other.pos());
        }

        @Override
        public Point2[] config(){
            Point2[] out = new Point2[power.links.size];
            for(int i = 0; i < out.length; i++){
                out[i] = Point2.unpack(power.links.get(i)).sub(tile.x, tile.y);
            }
            return out;
        }
    }
}
