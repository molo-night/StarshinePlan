package xx.world.blocks.power;

import arc.graphics.g2d.Draw;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.BeamNode;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import xx.world.modules.xx_PowerModule;

import static mindustry.Vars.*;
import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class xx_ConsumeGenerator extends ConsumeGenerator {
    public int protentionVoltage;//TODO 记得将powerProduction与这个并到一起去


    public xx_ConsumeGenerator(String name) {
        super(name);
        this.connectedPower = false;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(generationType, protentionVoltage, StatUnit.powerSecond);
    }




    @Override
    public void drawPotentialLinks(int x, int y){
        if((consumesPower || outputsPower) && hasPower){//remind 判断很有问题，但目前没问题。
            Tile tile = world.tile(x, y);
            if(tile != null){
                PowerNode.getNodeLinks(tile, this, player.team(), other -> {
                    PowerNode node = (PowerNode)other.block;
                    Draw.color(node.laserColor1, Renderer.laserOpacity * 0.5f);
                    node.drawLaser(x * tilesize + offset, y * tilesize + offset, other.x, other.y, size, other.block.size);

                    Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 2f, Pal.place);
                });

                BeamNode.getNodeLinks(tile, this, player.team(), other -> {
                    BeamNode node = (BeamNode)other.block;
                    Draw.color(node.laserColor1, Renderer.laserOpacity * 0.5f);
                    node.drawLaser(other.x, other.y, x * tilesize + offset, y * tilesize + offset, size, other.block.size);

                    Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 2f, Pal.place);
                });
            }
        }
    }

    public class xx_ConsumeGeneratorBuild extends ConsumeGeneratorBuild{
        public int productionVoltage;//当前产生的电压，用于电网电压，工作时
        //public

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            if (!this.initialized) {
                this.create(tile.block(), team);
            } else if (this.block.hasPower) {
                this.power.init = false;
                (new xx_PowerGraph()).add(this);
            }

            this.proximity.clear();
            this.rotation = rotation;
            this.tile = tile;
            this.set(tile.drawx(), tile.drawy());
            if (shouldAdd) {
                this.add();
            }

            this.checkAllowUpdate();
            this.created();
            return this;
        }

        @Override
        public Building create(Block block, Team team) {
            this.block = block;
            this.team = team;
            this.health = (float)block.health;
            this.maxHealth((float)block.health);
            this.timer(new Interval(block.timers));
            if (block.hasItems) {
                this.items = new ItemModule();
            }

            if (block.hasLiquids) {
                this.liquids = new LiquidModule();
            }

            if (block.hasPower) {
                this.power = new xx_PowerModule();//改成自己的
                this.power.graph.add(this);
            }

            this.initialized = true;
            return this;
        }

        //获取生成电压，与效率相乘
        public int getProtentionVoltage() {
            return (int) (protentionVoltage * productionEfficiency);
        }

        @Override
        public void placed() {
            if (!Vars.net.client()) {
                if ((this.block.consumesPower || this.block.outputsPower) && this.block.hasPower /*&& this.block.connectedPower*/) {
                    PowerNode.getNodeLinks(this.tile, this.block, this.team, (other) -> {
                        if (!other.power.links.contains(this.pos())) {
                            other.configureAny(this.pos());
                        }

                    });
                }

            }
        }
    }
}
