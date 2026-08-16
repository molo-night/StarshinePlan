package xx.world.blocks.production;

import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Sounds;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumePower;

public class ConsumeCrafter extends GenericCrafter {

    public ConsumeCrafter(String name) {
        super(name);
    }


    /*
    TODO 改变每帧消耗逻辑，电不足不工作，以后做开始工作消耗更多电
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
    */

    public class ConsumeCrafterBuilding extends GenericCrafterBuild {
        public boolean isCrafting = false;  //进行生产中
        //TODO 需保存的变量




        @Override
        public void update() {
            if ((this.timeScaleDuration -= Time.delta) <= 0.0F || !this.block.canOverdrive) {
                this.timeScale = 1.0F;
            }

            if (!Vars.headless && this.block.ambientSound != Sounds.none && this.shouldAmbientSound()) {
                Vars.control.sound.loop(this.block.ambientSound, this, this.block.ambientSoundVolume * this.ambientVolume());
            }

            //this.updateConsumption();

            if (canUpdateCraft()) this.updateCraft();
            else {
                this.progress = 0;
                this.isCrafting = false;
            }

            if (this.enabled || !this.block.noUpdateDisabled) {
                this.updateTile();
            }

        }


        @Override
        public void updateTile() {
            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;
            dumpOutputs();                                       //将物品传输给其他方块
        }


//        @Override//完全重写，真辛苦
//        public void updateConsumption() {
//            //TODO may look bad, revert to edelta() if so
//            totalProgress += warmup * Time.delta;
//            dumpOutputs();                                      //用于将物品传输给其他方块
//        }

        //用于消耗输入，生成输出的逻辑部分，原版的我不喜欢，当然更多的是看不懂...
        public void updateCraft() {
            Log.info("已开工" );
            this.efficiency = 1;//TODO 以后再用

            if (!isCrafting) {                                  //是否开始生产与物品消耗
                consume();
                isCrafting = true;
                Log.info("第一个判断aaaaaaaa");
            }

            if (isCrafting) {
                progress += getProgressIncrease(craftTime);     //增加进度
                particleEffect();                               //用于绘制工厂的粒子特效
                Log.info("第二个判断" + progress);
            }

            if (progress >= 1f) {
                output();
                progress %= 1f;
                isCrafting = false;
                Log.info("第三个判断LLLLLl");
            }

        }

        //用于绘制工厂运行时的粒子特效
        public void particleEffect() {
            if (wasVisible && Mathf.chanceDelta(updateEffectChance)) {
                updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
            }
        }

        //输出
        public void output() {
            if (outputItems != null) {
                for (var output : outputItems) {
                    for (int i = 0; i < output.amount; i++) {
                        offload(output.item);
                    }
                }
            }

            if (wasVisible) {
                craftEffect.at(x, y);
            }

        }

        //是否可以开始消耗输入并开始生产
        public boolean canUpdateCraft() {

            float itemMinEfficiency = 1f;
            float powerMinEfficiency = 1f;
            boolean canCraft;//可以进行生产
            this.shouldConsumePower = true;
            for (Consume cons : this.block.nonOptionalConsumers) {
                if(cons instanceof ConsumeItems) {
                    itemMinEfficiency = Math.min(itemMinEfficiency, cons.efficiency(this));//物品效率=该物品充足?1:0
                }
                else if(cons instanceof ConsumePower) powerMinEfficiency= Math.min(itemMinEfficiency, cons.efficiency(this));
            }

            //生产中打断检测，被打断就老老实实地受着吧
            if(this.isCrafting && !this.enabled){
                Log.info("被打断了interrpute" );
                isCrafting = false;
            }

            this.shouldConsumePower = isCrafting;

            canCraft = (itemMinEfficiency >= 1 || isCrafting) && powerMinEfficiency >=1;

            Log.info("powerMinEfficiency=    "+powerMinEfficiency );
            this.efficiency = (canCraft && this.enabled && shouldConsume())? 1 : 0 ;//TODO目前只作ui显示，后面再用

            return canCraft && this.enabled && shouldConsume();
        }

        @Override//TODO 对液体部分进行重写
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
        public float getProgressIncrease(float baseTime){
            return 1.0F / baseTime * this.edelta();
        }

        @Override//改写逻辑，去除efficiency影响
        public float edelta() {
            //return this.efficiency * this.delta();
            return this.delta();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(isCrafting);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            isCrafting = read.bool();
        }

    }
}