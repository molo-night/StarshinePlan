package xx.entities.abilityList;

import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import xx.expand.F_CompositeUnitEntity;

public class SelfSustainingSystemAbilityList extends Ability  {

    public float
            sssCounter = 0,
            examineDelay = 30f;//second




    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("selfsustainingsystem-desc")).row();
        t.add(abilityStat("examinedelay",examineDelay)).row();
    }

    @Override
    public void update(Unit unit){
        if (unit instanceof F_CompositeUnitEntity F) this.sssCounter = F.sssCounter /60f;
    }

    @Override
    public void displayBars(Unit unit, Table bars) {

        if ( examineDelay > 0) {
            bars.add(new Bar(
                    () -> "检测: " + Strings.autoFixed(sssCounter,1) + "/" + examineDelay,
                    () -> Pal.regen,
                    () -> (float) sssCounter / examineDelay
            )).row();
        }
    }
}
