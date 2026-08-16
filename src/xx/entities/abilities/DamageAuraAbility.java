package xx.entities.abilities;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

public class DamageAuraAbility extends Ability {
    public float range = 80f;
    public float ratio =1f;//每12帧计算一次
    public float lastDamage = 0f;
    private float counter = 0f;

    @Override
    public void update(Unit unit) {
        counter ++;
        if (counter >= 12) {
            counter = 0f;
            Units.nearbyEnemies(unit.team, unit.x, unit.y, range, enemy -> {
                float damage = enemy.health * ratio;
                if(damage < lastDamage) damage = lastDamage;
                enemy.health(enemy.health - damage);
            });
        }

        int tileRadius = (int)(range / 8f);
        for (int dx = -tileRadius; dx <= tileRadius; dx++) {
            for (int dy = -tileRadius; dy <= tileRadius; dy++) {
                Building build = Vars.world.buildWorld(
                        unit.x + dx * 8f,
                        unit.y + dy * 8f
                );
                if (build != null && build.team != unit.team) {
                    float damage = build.health * ratio;
                    if (damage < lastDamage) damage = lastDamage;
                    build.damage(unit.team, damage);
                }
            }
        }
    }

    @Override
    public void draw(Unit unit) {
        Draw.z(Layer.effect);
        Draw.color(Pal.accent);
        Lines.circle(unit.x, unit.y, range);
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("damageaura-desc")).row();
        t.add(abilityStat("ratio",ratio)).row();
        t.add(abilityStat("lastDamage",lastDamage)).row();
        t.add(abilityStat("range",range)).row();
    }
}
