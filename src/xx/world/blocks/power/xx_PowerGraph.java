package xx.world.blocks.power;

import arc.math.Mathf;
import arc.math.WindowedMean;
import arc.struct.IntSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.gen.PowerGraphUpdater;
import mindustry.world.blocks.power.PowerGraph;
import xx.gen.xx_Building;
import xx.world.consumes.xx_ConsumePower;
import xx.world.modules.xx_PowerModule;

public class xx_PowerGraph extends PowerGraph {//极具简化的电力系统，想要更加拟真，电脑会算冒烟的。这不是做电路模拟
    public int graphVoltage;//电压，这里指电压等级，如果真的用数值的话，我估计我会写死，玩家烦死，电脑算死

    public float TotalResistance;//电网中总电阻

    public final Seq<Building> parallelConnectionBuilding = new Seq<>(false,16 , Building.class);//并联连接组
    public final Seq<Building> seriesConnectionBuilding = new Seq<>(false,16 , Building.class);//串联连接组

    private static final Queue<Building> queue = new Queue<>();
    private static final Seq<Building> outArray1 = new Seq<>();
    private static final Seq<Building> outArray2 = new Seq<>();
    private static final IntSet closedSet = new IntSet();

    private final @Nullable PowerGraphUpdater entity;
    private final WindowedMean powerBalance = new WindowedMean(60);
    private float lastPowerProduced, lastPowerNeeded, lastPowerStored;
    private float lastScaledPowerIn, lastScaledPowerOut, lastCapacity;
    //diodes workaround for correct energy production info
    private float energyDelta = 0f;

    private final int graphID;
    private static int lastGraphID;

    //古法编程，你值得拥有

    public xx_PowerGraph(){
        entity = PowerGraphUpdater.create();
        entity.graph = this;
        graphID = lastGraphID++;
    }

    public xx_PowerGraph(boolean noEntity){
        entity = null;
        graphID = lastGraphID++;
    }

    public float getPowerBalance(){
        return powerBalance.rawMean();
    }

    public boolean hasPowerBalanceSamples(){
        return powerBalance.hasEnoughData();
    }

    public float getLastPowerProduced(){
        return lastPowerProduced;
    }

    public float getLastPowerNeeded(){
        return lastPowerNeeded;
    }


    //用于计算电网中总电阻，会智能忽略不需消耗电的工厂（停工，电压不足不耗电。输入功率不足会导致低效运行或空转）
    //remind shouldConsumePower与enable需处理好。
//    public float getTotalResistance(){
//        float resistance = 0;//临时存储
//        var items = parallelConnectionBuilding.items;
//        for(int i = 0; i < parallelConnectionBuilding.size; i++){//计算并联
//            var parallelConnection = items[i];
//            xx_ConsumePower consumePower = (xx_ConsumePower) parallelConnection.block.consPower;
//            if(parallelConnection.shouldConsumePower && consumePower.ratedVoltage >= graphVoltage) {
//                resistance += 1 / consumePower.resistance;
//            }
//        }
//
//        resistance = 1/resistance;
//
//        items = seriesConnectionBuilding.items;
//        for(int i = 0; i < seriesConnectionBuilding.size; i++){//计算串联
//            var seriesConnection = items[i];
//            xx_ConsumePower consumePower = (xx_ConsumePower) seriesConnection.block.consPower;
//            resistance += consumePower.resistance;//remind 目前串联的只有电力节点，所以没有判断
//        }
//        return resistance;
//    }

    @Override
    public void add(Building build){
        super.add(build);
        xx_PowerModule power = (xx_PowerModule)build.power;
        if(power.parallelConnection){
            parallelConnectionBuilding.add(build);
        }
        else seriesConnectionBuilding.add(build);

    }

    @Override
    public void clear() {
        parallelConnectionBuilding.clear();
        seriesConnectionBuilding.clear();
        super.clear();
    }

    @Override
    public void removeList(Building build){
        all.remove(build);
        producers.remove(build);
        consumers.remove(build);
        batteries.remove(build);
        parallelConnectionBuilding.clear();
        seriesConnectionBuilding.clear();
    }

    @Override
    public void remove(Building tile){

        //go through all the connections of this tile
        for(Building other : tile.getPowerConnections(outArray1)){
            //a graph has already been assigned to this tile from a previous call, skip it
            if(other.power.graph != this) continue;

            xx_PowerGraph graph = new xx_PowerGraph();
            graph.checkAdd();
            graph.add(other);
            //add to queue for BFS
            queue.clear();
            queue.addLast(other);
            while(queue.size > 0){
                //get child from queue
                Building child = queue.removeFirst();
                //add it to the new branch graph
                graph.add(child);
                //go through connections
                for(Building next : child.getPowerConnections(outArray2)){
                    //make sure it hasn't looped back, and that the new graph being assigned hasn't already been assigned
                    //also skip closed tiles
                    if(next != tile && next.power.graph != graph){
                        graph.add(next);
                        queue.addLast(next);
                    }
                }
            }
            //update the graph once so direct consumers without any connected producer lose their power
            graph.update();
        }

        //implied empty graph here
        if(entity != null) entity.remove();
    }

    @Override//总产电功率
    public float getPowerProduced(){
        float powerProduced = 0f;
        var items = producers.items;
        for(int i = 0; i < producers.size; i++){
            var producer = items[i];
            powerProduced += producer.getPowerProduction() /* producer.delta()*/;
        }
        return powerProduced;
    }



    @Override//总耗电电量
    public float getPowerNeeded(){
        float powerNeeded = 0f;
        var items = consumers.items;
        for(int i = 0; i < consumers.size; i++){
            var consumer = items[i];
            xx_ConsumePower consumePower = (xx_ConsumePower) consumer.block.consPower;
            if(consumer.shouldConsumePower && consumePower.ratedVoltage >= graphVoltage){//TODO 这里电压判断也许应该放在shouldConsumePower里，注意上面还有
                powerNeeded += consumePower.requestedPower(consumer) /* consumer.delta()*/;
            }
        }
        return powerNeeded;
    }

    //电网电压
    public int getGraphVoltage(){
        int voltage = 0;
        var items = producers.items;
        for(int i = 0; i < producers.size; i++){
            var producer = items[i];
            voltage = Math.max( ((xx_ConsumeGenerator.xx_ConsumeGeneratorBuild) producer).getProtentionVoltage() , voltage );
        }
        return voltage;
    }


    @Override
    public void distributePower(float needed, float produced, boolean charged) {
        float coverage = Mathf.zero(needed) && Mathf.zero(produced) && !charged && Mathf.zero(lastPowerStored) ? 0f : Mathf.zero(needed) ? 1f : Math.min(1, produced / needed);
        //电功率应该根据每个工厂的内阻与总阻的比值来分配，再根据分配来的功率与额定功率的比值确定电力满足度。
        //当然，在这之中还要判断电压电流是否在合适范围内
        //似乎用功率占比来分配电力更好，用电阻本质上是在做功率占比
        var items = consumers.items;
        if (needed <= produced && !Mathf.zero(produced)) {
            for (int i = 0; i < consumers.size; i++) {
                var consumer = items[i];

                xx_ConsumePower consPower = (xx_ConsumePower) consumer.block.consPower;//该电网只会存在这种电力消耗模块

                if (consumer.shouldConsumePower && graphVoltage >= consPower.ratedVoltage) {
                    consumer.power.status = 1;
                }
                else {
                    consumer.power.status =  produced >= (needed + consPower.usage)? 1 : 0 ;//机器未工作时，shouldConsumePower=false，这里是计算工作后，usage等于多少
                }

            }
        }
        else
        {
            for (int i = 0; i < consumers.size; i++) {
                var consumer = items[i];
                consumer.power.status = 0;
            }
        }

    }

    @Override
    public void update(){
        if(!consumers.isEmpty() && consumers.first().cheating()){
            //when cheating, just set status to 1
            for(Building tile : consumers){
                tile.power.status = 1f;
            }

            lastPowerNeeded = lastPowerProduced = 1f;
            return;
        }

        graphVoltage = getGraphVoltage();//计算电网电压
        float powerNeeded = getPowerNeeded();
        float powerProduced = getPowerProduced();
        //float totalResistance = getTotalResistance();//TODO可以优化为数组改变时计算，不变时数值不变，但这都是后话了
        //remind 接下来就是改distributePower(...)了


        lastPowerNeeded = powerNeeded;
        lastPowerProduced = powerProduced;

        lastScaledPowerIn = powerProduced ;
        lastScaledPowerOut = powerNeeded ;
        lastCapacity = getTotalBatteryCapacity();
        lastPowerStored = getBatteryStored();

        powerBalance.add(lastPowerProduced - lastPowerNeeded);//用于电力节点的bar

        if(!(consumers.size == 0 && producers.size == 0 && batteries.size == 0)){
            boolean charged = false;

            if(!Mathf.equal(powerNeeded, powerProduced)){
                if(powerNeeded > powerProduced){
                    float powerBatteryUsed = useBatteries(powerNeeded - powerProduced);
                    powerProduced += powerBatteryUsed;
                    lastPowerProduced += powerBatteryUsed;
                }else if(powerProduced > powerNeeded){
                    charged = true;
                    powerProduced -= chargeBatteries(powerProduced - powerNeeded);
                }
            }

            distributePower(powerNeeded, powerProduced, charged);
        }
    }

    @Override//用于连接两个电网
    public void addGraph(PowerGraph graph){
        if(graph instanceof xx_PowerGraph g) {
            if (g == this) return;
            //if (g.voltage != this.voltage && g.voltage != 0 && voltage !=0) return;


            //merge into other graph instead.
            if (g.all.size > all.size) {
                g.addGraph(this);
                return;
            }

            //other entity should be removed as the graph was merged
            if (g.entity != null) g.entity.remove();

            for (Building tile : g.all) {
                add(tile);
            }
            checkAdd();
        }
    }

    @Override//调试内容
    public String toString(){
        return "xx_PowerGraph{" +
                "\n产电producers = " + producers +
                "\n耗电consumers = " + consumers +
                "\n电池batteries = " + batteries +
                "\n所有all = " + all +
                "\n并联parallelConnectionBuilding = "+parallelConnectionBuilding+
                "\n串联seriesConnectionBuilding = "+seriesConnectionBuilding+
                "\n个数all.size = "+all.size+
                "\ngraphID = " + graphID +
                "\n发电功率 = "+getPowerProduced()+
                "\n耗电功率 = "+getPowerNeeded()+
                "\n电网电压 = "+graphVoltage+
                //"\n总阻 = "+getTotalResistance()+
                "\n}";
    }

}
