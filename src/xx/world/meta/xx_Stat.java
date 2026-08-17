package xx.world.meta;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class xx_Stat extends Stat {
    public xx_Stat(String name) {
        super(name);
    }

    public static final Stat
            baseProtentionVoltage = new Stat("baseProtentionVoltage", StatCat.power);
}
