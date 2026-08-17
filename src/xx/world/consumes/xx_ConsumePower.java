package xx.world.consumes;

import mindustry.gen.Building;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import xx.world.meta.xx_Stat;
import xx.world.meta.xx_StatUnit;

public class xx_ConsumePower extends ConsumePower {

        //remind usage就是需求功率，额定功率，这里的额定是最小功率，低于它将不工作
        public float maxUsage;//最大功率，现在没什么用，用于过载判断及爆破

        public int ratedVoltage = 1;//标准电压等级，可以高不能低，这是工作门槛

        public float ratedCurrent = 1;//标准电流
        public float minCurrent = 1;//最小电流
        //最大电流在block里，是机器本身的性质


        public float resistance = 0.1f;//remind该配方的电阻，因为一个配方最多有一个xx_ConsumePower
        //public float equivalentResistance;//remind 应该要有个等效电阻，即将所有电能转化为内能，这个电阻可以直接算出正确的电流

        //电力限制关于电压于电流，效率限制关于功率,感觉可以弄一个新的类

        public float maxDischargeCurrent;//最大放电电流，这是用于电池的
        public float maxDischargePower;//放电功率

        //TODO有关电池的设置以后来弄。

        //默认
        public xx_ConsumePower(){
                super();
        }

        public xx_ConsumePower(float usage, float capacity, boolean buffered){
                this.usage = usage;
                this.capacity = capacity;
                this.buffered = buffered;
        }

        public xx_ConsumePower(float usage , int ratedVoltage , float ratedCurrent , float minCurrent , float resistance){
                this(usage , 0 , false);
                this.ratedVoltage = ratedVoltage;
                this.ratedCurrent = ratedCurrent;
                this.minCurrent = minCurrent;
                this.resistance = resistance;
        }

        //remind 这个才有用
        public xx_ConsumePower(float usage , int ratedVoltage , float resistance){
                this(usage , 0 , false);
                this.ratedVoltage = ratedVoltage;
                this.resistance = resistance;
                this.maxUsage = usage;
        }

        @Override
        public void display(Stats stats){
                if(usage > 0f){
                        stats.add(Stat.powerUse, usage, xx_StatUnit.powerSecond2);
                        stats.add(xx_Stat.ratedVoltage, ratedVoltage, xx_StatUnit.voltage);
                }
        }


        @Override
        public float requestedPower(Building entity){
                return usage * (entity.shouldConsume() ? 1f : 0f);
        }
}
