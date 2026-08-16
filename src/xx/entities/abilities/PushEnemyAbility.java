package xx.entities.abilities;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/**
 * 推开范围内所有敌方单位。
 * 无能量限制，始终生效。
 */
public class PushEnemyAbility extends Ability {

    /** 推开半径 */
    public float radius = 80f;
    /** 推开力度倍数（1=刚好推出范围，>1=推得更远） */
    public float force = 1f;
    /** 推开特效 */
    public Effect pushEffect = Fx.circleColorSpark;

    @Override
    public void update(Unit unit) {
        float reach = radius;

        Units.nearbyEnemies(unit.team, unit.x, unit.y, reach, enemy -> {
            float overlapDst = reach - enemy.dst(unit.x, unit.y);

            if (overlapDst > 0) {
                // 如果敌人正在靠近，先停住
                if (Angles.angleDist(enemy.angleTo(unit), enemy.vel.angle()) < 90f) {
                    enemy.vel.setZero();
                }

                // 推出范围
                enemy.move(
                        Tmp.v1.set(enemy).sub(unit)
                                .setLength(overlapDst * force + 0.01f)
                );

                // 特效
                if (Mathf.chanceDelta(0.3f * Time.delta)) {
                    pushEffect.at(enemy.x, enemy.y, unit.team.color);
                }
            }
        });
    }

    @Override
    public void draw(Unit unit) {
        Draw.z(Layer.effect);
        Draw.color(Pal.accent, 0.2f);
        Draw.alpha(0.4f);
        Draw.color(Pal.accent);
        Lines.stroke(3f);
        Lines.circle(unit.x, unit.y, radius);
        Draw.reset();
    }
}