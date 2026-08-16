package xx.world.modules;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.world.modules.PowerModule;
import xx.world.blocks.power.xx_PowerGraph;

//电力消耗模块,但你怎么这么自私！
public class xx_PowerModule extends PowerModule {
//    public float maxPowerReserve = 10;
//    public float powerReserve = 0;//内部缓存，不满就吞电，非常地自私
//    public boolean hasReserve;//是否有内部缓存，没有的一般为电力节电

    public int voltage;//当前电压等级,这个应该跟随电网

    public float current;//当前电流

    public float nowPower;
    //应该用不着最大功率，最大电压和最大电流就已经锁死了
    //这里的最大电压电流是机器的承受电力能力，而不是配方需求电力，所以最大有效功率取决于配方的额定功率

    //这里绝对不能为空或为零，我没有做保护
    //public float resistance = 0.1f;//remind电阻，感觉可以做成配方的变量，不同配方有不同电阻，当然这后面再做多配方
    public boolean parallelConnection = true;//是否并联，串联用于电力节电等输电方块


    //TODO需保存变量

    public xx_PowerModule(){
        this.graph = new xx_PowerGraph();
    }//无参构造

    public xx_PowerModule(float maxPowerReserve , int maxVoltage , float maxCurrent){
        this.graph = new xx_PowerGraph();
    }


    //获取当前功率
    public float getNowPower(){
        return this.voltage * this.current;
    }



    //直接加上电网分配给它的电量
//    public void calculateReserve(float PowerConsumption_R){
//        powerReserve = Math.min(powerReserve + PowerConsumption_R,maxPowerReserve);
//    }

    @Override
    public void write(Writes write){
        super.write(write);
    }

    @Override
    public void read(Reads read){
        super.read(read);
    }

}
