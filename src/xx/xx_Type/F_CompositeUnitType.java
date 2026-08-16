package xx.xx_Type;

import mindustry.gen.EntityMapping;
import mindustry.type.UnitType;
import mindustry.world.meta.Stat;
import xx.expand.F_CompositeUnitEntity;

public class F_CompositeUnitType extends UnitType {

    public int
            damagedDelay = 0,//
            revivesMax = 0,
            increaseCleanIntervals = 0,//受到伤害后，免疫此后受到的伤害的次数。同时，这也是初始免疫次数
            maxCleanIntervals = 0;//最高免疫次数
    public float

            coreArmor = 0,
            massMultiply = 1,
            damageHeal = 0,
            damageHealMultiply = 1f,
            chance = 0f,//概率
            reviveDelay = 0f;//血条层数恢复时间(s)






    public F_CompositeUnitType(String name) {
        super(name);
        constructor = F_CompositeUnitEntity::new;
    }


    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.armor, "/  " + coreArmor);

    }
    @Override
    public void init() {
        super.init();
        // 注册实体映射
        EntityMapping.nameMap.put(name, constructor);
    }












}



