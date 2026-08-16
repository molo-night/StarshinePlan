package xx.entities.abilities;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.IntSet;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;


public class SlowBulletAbility extends Ability {


    public float radius = 80f;
    public float speedMultiplier = 0.3f;
    public boolean drawCircle = true;
    public float pushForce = 0f;  // 推力强度（每帧增加的速度）

    private final IntSet insideLastFrame = new IntSet();

    @Override
    public void update(Unit unit) {

        IntSet insideThisFrame = new IntSet();

        Groups.bullet.intersect(
                unit.x - radius, unit.y - radius,
                radius * 2f, radius * 2f,
                bullet -> {
                    if (bullet.team == unit.team) return;
                    if (!bullet.type.collides || !bullet.type.reflectable) return;

                    float dst = bullet.dst(unit.x, unit.y);

                    if (dst <= radius) {
                        insideThisFrame.add(bullet.id);

                        if (!insideLastFrame.contains(bullet.id)) {
                            bullet.vel.scl(speedMultiplier);
                            float angle = unit.angleTo(bullet);


                            bullet.vel.add(
                                    Mathf.cosDeg(angle) * pushForce * Time.delta,
                                    Mathf.sinDeg(angle) * pushForce * Time.delta
                            );


                            Fx.circleColorSpark.at(bullet.x, bullet.y, unit.team.color);
                        }
                    }
                }
        );

        insideLastFrame.clear();
        insideLastFrame.addAll(insideThisFrame);
    }

    @Override
    public void draw(Unit unit) {
        if (!drawCircle) return;
        Draw.z(Layer.effect);
        Draw.color(Pal.accent, 0.3f);
        Lines.stroke(3f);
        Lines.circle(unit.x, unit.y, radius);
        Draw.reset();
    }
}