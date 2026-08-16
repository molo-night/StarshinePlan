package xx.entities.abilities;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import xx.expand.F_CompositeUnitEntity;

public class SlowSpeedHealAbility extends Ability{
    public float
            limit = 0,
            healAmount = 0;



    @Override
    public void update(Unit unit){
        float reallyAmount = healAmount * Time.delta;
        if( unit.maxHealth < (unit.health + reallyAmount) && unit instanceof F_CompositeUnitEntity F){
            float raiseCoreShield = reallyAmount + F.health - F.maxHealth ;
            F.coreShield += F.coreShield + raiseCoreShield < limit ? raiseCoreShield :limit - F.coreShield;
        }
        unit.heal(reallyAmount);
    }


    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("slowspeedheal-desc")).row();
        t.add(abilityStat("healamount",healAmount)).row();
        t.add(abilityStat("limit",limit)).row();
    }


}
