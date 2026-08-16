package xx.entities.abilityList;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.ui.Bar;
import xx.expand.F_CompositeUnitEntity;
import xx.expand.xx_Pal;

public class ReviveLifeAbilityList extends Ability {
    public int revivesSum ;
    public int revivesMax = 5 ;
    public float reviveDelay = 10;
    public float reviveCounter;

    /* 算了，都变成凌城一个单位的了，数值什么的都已固定，那我就直接写吧 */

    @Override
    public void created(Unit unit){
        if (unit instanceof F_CompositeUnitEntity F) {
            this.reviveDelay = F.reviveDelay / 60f;
        }
    }






    @Override
    public void update(Unit unit){
        if (unit instanceof F_CompositeUnitEntity reviveUnit) {
            this.revivesSum = reviveUnit.revivesSum;
            this.revivesMax = reviveUnit.revivesMax;
            this.reviveCounter = reviveUnit.reviveCounter / 60f;

        }

    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(Core.bundle.format("revive-desc")).row();
        t.add(abilityStat("revive",revivesMax)).row();
        t.add(abilityStat("retime", reviveDelay)).row();
    }





    //状态显示
    @Override
    public void displayBars(Unit unit, Table bars) {

        if ( revivesMax > 0) {
            bars.add(new Bar(
                    () -> "血条: " + revivesSum + "/" + revivesMax,
                    () -> revivesSum <= 1 ? xx_Pal.colorWarn : revivesSum<=revivesMax/2 ? xx_Pal.colorRemind : xx_Pal.colorRevive,
                    () -> (float) revivesSum / revivesMax
            )).row();
            bars.add(new Bar(
                    () -> "冷却: " + Strings.autoFixed(reviveCounter, 1)+ "/" + reviveDelay,
                    () -> xx_Pal.colorRevive,
                    () -> reviveCounter / reviveDelay
            )).row();
        }
    }







}


