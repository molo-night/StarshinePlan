package xx.world.blocks.power;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.BeamNode;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.BlockStatus;
import xx.gen.xx_Building;
import xx.world.modules.xx_PowerModule;

import java.util.Arrays;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class text_node extends BeamNode {

    public text_node(String name) {
        super(name);
        connectedPower = false;
    }

    @Override//虚线绘制
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
                if(other != null && other.isInsulated()){
                    break;
                }

                if(other != null && other.team == Vars.player.team() && other.block instanceof text_node){
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
    }


    public class text_nodeBuild extends xx_Building {

        @Override
        public void created(){ // Called when one is placed/loaded in the world
            xx_PowerModule power = (xx_PowerModule) this.power;
            power.parallelConnection = false;
            super.created();
        }



        //current links in cardinal directions
        public Building[] links = new Building[4];
        public Tile[] dests = new Tile[4];
        public int lastChange = -2;

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
                if((other.build != null && other.build.isInsulated()) || (other.block() instanceof text_node && other.team() == team)){
                    return false;
                }

                //within target rectangle
                if(other.x >= minX && other.y >= minY && other.x <= maxX && other.y <= maxY){
                    return true;
                }
            }

            return false;
        }

        @Override
        public void updateTile(){
            //TODO this block technically does not need to update every frame, perhaps put it in a special list.
            if(lastChange != world.tileChanges){
                lastChange = world.tileChanges;
                updateDirections();
            }
        }

        @Override
        public BlockStatus status(){
            float balance = power.graph.getPowerBalance();
            if(balance > 0f) return BlockStatus.active;
            if(balance < 0f && power.graph.getLastPowerStored() > 0) return BlockStatus.noOutput;
            return BlockStatus.noInput;
        }

        @Override
        public void draw(){
            super.draw();

            if(Mathf.zero(Renderer.laserOpacity) || team == Team.derelict) return;

            Draw.z(Layer.power);
            Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);
            float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

            for(int i = 0; i < 4; i ++){
                if(dests[i] != null && links[i].wasVisible && links[i].block instanceof text_node node &&
                        ((links[i].tileX() != tileX() && links[i].tileY() != tileY()) ||
                                (links[i].id > id && range >= node.range) || range > node.range)){

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

        @Override
        public void pickedUp(){
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
        }

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
                    if(other != null && other.isInsulated()){
                        break;
                    }

                    //power nodes do NOT play nice with beam nodes, do not touch them as that forcefully modifies their links
                    if(other != null && other.block instanceof text_node && other.team == team ){
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

                        xx_PowerGraph newgraph = new xx_PowerGraph();
                        //reflow from this point, covering all tiles on this side
                        newgraph.reflow(this);

                        if(prev.power.graph != newgraph){
                            //reflow power for other end
                            xx_PowerGraph og = new xx_PowerGraph();
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
    }
}
