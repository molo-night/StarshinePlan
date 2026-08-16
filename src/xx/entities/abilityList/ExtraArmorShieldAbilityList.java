package xx.entities.abilityList;

import arc.scene.ui.layout.Table;
import mindustry.entities.abilities.Ability;

public class ExtraArmorShieldAbilityList extends Ability {
    public float amount=6000;
    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("extraarmorshield-desc")).row();
        t.add(abilityStat("amount",amount)).row();
    }
}
