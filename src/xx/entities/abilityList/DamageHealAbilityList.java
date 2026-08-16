package xx.entities.abilityList;

import arc.scene.ui.layout.Table;
import mindustry.entities.abilities.Ability;

public class DamageHealAbilityList extends Ability {
    public float damageHeal = 240f,damageHealMultiply = 2f,chance = 0.3f;

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("damageheal-desc")).row();
        t.add(abilityStat("damageheal",damageHeal,damageHealMultiply,chance*100)).row();
    }
}
