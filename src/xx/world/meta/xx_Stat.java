package xx.world.meta;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class xx_Stat extends Stat {
    public xx_Stat(String name) {
        super(name);
    }

    public static final Stat
            pbrang = new Stat("pbrang", StatCat.power),
            baseProtentionVoltage = new Stat("baseProtentionVoltage", StatCat.power),
            ratedVoltage = new Stat("ratedVoltage", StatCat.power),
            maxVoltage = new Stat("maxVoltage", StatCat.power),
            maxUsage = new Stat("maxUsage", StatCat.power);

}
