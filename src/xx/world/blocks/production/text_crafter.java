package xx.world.blocks.production;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.core.Renderer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.blocks.power.BeamNode;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import xx.gen.xx_Building;
import xx.world.consumes.xx_ConsumePower;
import xx.world.meta.xx_Stat;
import xx.world.meta.xx_StatUnit;
import xx.world.modules.xx_PowerModule;

import static mindustry.Vars.*;
import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class text_crafter extends GenericCrafter {

    public int maxVoltage;//机器能够承受的最大电压，电网电压不匹配忽略
    public float maxCurrent;//机器能够承受的最大电流，应在电网类检测并操作

    public text_crafter(String name) {
        super(name);
        connectedPower = false;
        this.maxVoltage = 0;
        this.maxCurrent = 0;
    }

    @Override
    public void setStats(){
        super.setStats();
        //TODO 后面需要适配不耗电的工厂
        //目前没有最大输入功率一说，没有过载。
        if(hasPower) {
            stats.add(xx_Stat.maxVoltage, maxVoltage, xx_StatUnit.voltage);
        }
    }

    @Override
    public void setBars(){
        super.setBars();
    }

    @Override
    public ConsumePower consumePower(float powerPerTick){
        return consume(new xx_ConsumePower(powerPerTick, 0.0f, false));
    }

    //完全自由
    public ConsumePower consumePower(float powerPerTick , int ratedVoltage , float ratedCurrent , float minCurrent , float resistance){
        return consume(new xx_ConsumePower(powerPerTick, ratedVoltage, ratedCurrent, minCurrent, resistance));
    }

    //这个用于标准与最小一致，即只能满效运行
    public ConsumePower consumePower(float powerPerTick, int ratedVoltage, float resistance){
        return consume(new xx_ConsumePower(powerPerTick, ratedVoltage, resistance));
    }


    @Override
    public void init(){
        super.init();
    }

    @Override//我不知道这个有什么用
    public void reinitializeConsumers(){
        super.reinitializeConsumers();

    }

    @Override
    public <T extends Consume> T consume(T consume){
        if(consume instanceof xx_ConsumePower){
            //there can only be one power consumer
            consumeBuilder.removeAll(b -> b instanceof ConsumePower);
            consPower = (xx_ConsumePower)consume;
        }
        consumeBuilder.add(consume);
        return consume;
    }

    @Override//预览建造时预览连接电力节点
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

    public class TextBuild extends xx_Building {
        public float progress;
        public float totalProgress;
        public float warmup;

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }



        @Override
        public boolean shouldConsume(){
            if(outputItems != null){
                for(var output : outputItems){
                    if(items.get(output.item) + output.amount > itemCapacity){
                        return false;
                    }
                }
            }

            if(outputLiquids != null && !ignoreLiquidFullness){
                boolean allFull = true;
                for(var output : outputLiquids){
                    if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                        if(!dumpExtraLiquid){
                            return false;
                        }
                    }else{
                        //if there's still space left, it's not full for all liquids
                        allFull = false;
                    }
                }

                //if there is no space left for any liquid, it can't reproduce
                if(allFull){
                    return false;
                }
            }

            return enabled;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){

                progress += getProgressIncrease(craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency
                if(outputLiquids != null){
                    float inc = getProgressIncrease(1f);
                    for(var output : outputLiquids){
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                craft();
            }

            dumpOutputs();
        }

        @Override
        public float getProgressIncrease(float baseTime){
            if(ignoreLiquidFullness){
                return super.getProgressIncrease(baseTime);
            }

            //limit progress increase by maximum amount of liquid it can produce
            float scaling = 1f, max = 1f;
            if(outputLiquids != null){
                max = 0f;
                for(var s : outputLiquids){
                    float value = (liquidCapacity - liquids.get(s.liquid)) / (s.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            //when dumping excess take the maximum value instead of the minimum.
            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        public float warmupTarget(){
            return 1f;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        public void craft(){
            consume();

            if(outputItems != null){
                for(var output : outputItems){
                    for(int i = 0; i < output.amount; i++){
                        offload(output.item);
                    }
                }
            }

            if(wasVisible){
                craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        public void dumpOutputs(){
            if(outputItems != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }

            if(outputLiquids != null){
                for(int i = 0; i < outputLiquids.length; i++){
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                    dumpLiquid(outputLiquids[i].liquid, 2f, dir);
                }
            }
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return progress();
            //attempt to prevent wild total liquid fluctuation, at least for crafters
            if(sensor == LAccess.totalLiquids && outputLiquid != null) return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public float progress(){
            return Mathf.clamp(progress);
        }

        @Override
        public int getMaximumAccepted(Item item){
            return itemCapacity;
        }

        @Override
        public boolean shouldAmbientSound(){
            return efficiency > 0;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.f(warmup);
            if(legacyReadWarmup) write.f(0f);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            if(legacyReadWarmup) read.f();
        }


        @Override//用于调试
        public String getPowerAll(){
            return super.getPowerAll();
        }

    }

}
