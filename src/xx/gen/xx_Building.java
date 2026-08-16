package xx.gen;

import arc.util.Interval;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.consumers.Consume;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import mindustry.world.modules.PowerModule;
import xx.world.blocks.power.xx_PowerGraph;
import xx.world.consumes.xx_ConsumePower;
import xx.world.modules.xx_PowerModule;

public class xx_Building extends Building {
    //电压电流移动到xx_PowerModule里了，弄方法去设置xx_PowerModule
    //TODO首先弄电网的分电，电阻计算，电流给予

    public boolean consumedInput_flag = false;//消耗过物品标记





    //TODO 需保存变量

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
    //cheating()用于检测该队伍是否拥有强制生产的规则，即无视消耗强制生产（原版规则里我好像没看到过这个选项）
    @Override//用于更新效率，是否消耗电力
    public void updateConsumption() {
        if (this.block.hasConsumers && !this.cheating()) {
            if (!this.enabled) {
                this.potentialEfficiency = this.efficiency = this.optionalEfficiency = 0.0F;
                this.shouldConsumePower = false;
            } else {
                boolean update = this.shouldConsume() && this.productionValid();
                float minEfficiency = 1.0F;
                this.efficiency = this.optionalEfficiency = 1.0F;
                this.shouldConsumePower = true;

                for(Consume cons : this.block.nonOptionalConsumers) {
                    float result = cons.efficiency(this);
                    if (cons != this.block.consPower && result <= 1.0E-7F) {
                        this.shouldConsumePower = false;
                    }

                    minEfficiency = Math.min(minEfficiency, result);
                }

                for(Consume cons : this.block.optionalConsumers) {
                    this.optionalEfficiency = Math.min(this.optionalEfficiency, cons.efficiency(this));
                }

                this.efficiency = minEfficiency;
                this.optionalEfficiency = Math.min(this.optionalEfficiency, minEfficiency);
                this.potentialEfficiency = this.efficiency;
                if (!update) {
                    this.efficiency = this.optionalEfficiency = 0.0F;
                }

                this.updateEfficiencyMultiplier();
                if (update && this.efficiency > 0.0F) {
                    for(Consume cons : this.block.updateConsumers) {
                        cons.update(this);
                    }
                }

            }
        } else {
            this.potentialEfficiency = this.enabled && this.productionValid() ? 1.0F : 0.0F;
            this.efficiency = this.optionalEfficiency = this.shouldConsume() ? this.potentialEfficiency : 0.0F;
            this.shouldConsumePower = true;
            this.updateEfficiencyMultiplier();
        }
    }


    //检测必需输入是否充足
    public boolean adequateInput(){//我充满了疑惑，对于consume数组，谁能教教我啊
        float minEfficiency = 1f;
        for(Consume cons : this.block.nonOptionalConsumers) {
            float result = cons.efficiency(this);
            if (cons != this.block.consPower && result <= 1.0E-7F) {//TODO
                this.shouldConsumePower = false;
            }
            minEfficiency = Math.min(minEfficiency, result);
        }
        return minEfficiency > 0;
    }

    @Override//建筑放置时自动连接电力节点
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

    //用于调试
    public String getPowerAll(){
        xx_PowerModule power = (xx_PowerModule)this.power;
        xx_ConsumePower consPower = (xx_ConsumePower) this.block.consPower;
        return "xx_PowerModule{"+
                "\n当前电压voltage = "+power.voltage+" / "+
                "\n当前电流current = "+power.current+" / "+
                "\n电阻resistance = "+consPower.resistance+
                "\n是并联parallelConnection = "+power.parallelConnection+
                "\nstatus = "+ power.status +
                "\n}";

    }
}
