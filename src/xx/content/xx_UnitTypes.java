package xx.content;

import arc.Events;
import arc.graphics.Color;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.ShapePart;
import mindustry.game.EventType;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import xx.entities.abilities.*;
import xx.entities.abilityList.DamageHealAbilityList;
import xx.entities.abilityList.ExtraArmorShieldAbilityList;
import xx.entities.abilityList.ReviveLifeAbilityList;
import xx.entities.abilityList.SelfSustainingSystemAbilityList;
import xx.expand.F_AheadWeapon;
import xx.expand.xx_Fx;
import xx.xx_Type.F_LaserBulletType;
import xx.xx_Type.F_CompositeUnitType;


public class xx_UnitTypes {


    public static F_CompositeUnitType fezedawntital;





    public static void load(){



        fezedawntital = new F_CompositeUnitType("fezedawntital"){{//凌城G
            health =3000000f;//3,000,000
            armor = 240f;
            hitSize = 300f;
            speed = 0.101f;
            rotateSpeed = 0.06f;
            accel = 0.01f;//加速度
            drag = 0.0028f;//阻力系数
            flying = true;
            buildSpeed = 10f;
            faceTarget = false;
            wobble = false;

            massMultiply = 10000000;
            revivesMax = 5 ;
            reviveDelay = 10f *60f;
            damageHeal = 240f;
            damageHealMultiply = 2f;
            chance = 0.3f;
            maxCleanIntervals = 10;//最高免疫次数
            increaseCleanIntervals = 1;//受到伤害后，免疫此后受到的伤害的次数。同时，这也是初始免疫次数
            coreArmor = 10f;//100
            damagedDelay = 20;//受到伤害后，数帧内巨额减伤


            Events.on(EventType.ContentInitEvent.class, e -> {
                for (StatusEffect effect : Vars.content.statusEffects()) {
                    if (effect == StatusEffects.dynamic) continue;
                    fezedawntital.immunities.add(effect);
                }
            });

            final float R = 400f;
            final float W = 96f;

            abilities.add(new ExtraArmorShieldAbilityList());
            abilities.add(new ReviveLifeAbilityList());
            abilities.add(new DamageHealAbilityList());
            abilities.add(new SelfSustainingSystemAbilityList());
            abilities.add(new SlowSpeedHealAbility(){{
                healAmount = 10f;
                limit = 1000000f;
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = 0f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = 45f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = 90f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = 135f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = 180f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = -45f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = -90f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new ShieldArcAbility(){{
                region = "tecta-shield";
                radius = R;//半径
                angle = 45f;//角度
                angleOffset = -135f;
                regen = 10500f / 60f;//回复
                cooldown = 0f;//冷却
                max = 2500000f;
                y = 0f;
                width = W;//宽度
                whenShooting = false;
                pushUnits = false;
                chanceDeflect = 1f;//反弹概率
            }});
            abilities.add(new DamageAuraAbility(){{
                ratio = 0.05f;
                range = 2040f;
                lastDamage = 10f;
            }});
            abilities.add(new PushEnemyAbility(){{
                radius = R;
                force = 0.8f;
            }});
            abilities.add(new SlowBulletAbility(){{
                radius = R + 320f;
                speedMultiplier = 0.2f;
                pushForce = 0.8f;
            }});
            abilities.add(new RepairFieldAbility(1800f, 60f * 2, 640f));
            abilities.add(new RepairFieldAbility(10000f, 60f * 10, 400f));




            weapons.add(new F_AheadWeapon("ahead-weapon"){{//头武器
                reload = 300f;
                x = 0f;
                y = 280f;
                shootY = 0;
                chargeSound = Sounds.chargeCorvus;
                shootSound = Sounds.shootCorvus;
                rotate = true;
                rotateSpeed = 120f;
                top = true;
                mirror = false;
                shoot.firstShotDelay = 200f;
                parentizeEffects = true;
                //postLockDuration = 60f;
                continuous = false;
                alwaysContinuous = false;
                parts.add(new ShapePart() {{
                    x = 0;
                    y = 0;
                    circle = true;
                    radius = 20f;
                    radiusTo = 6f;
                    layer = Layer.effect;
                    color = Color.white;
                    progress = PartProgress.smoothReload;
                }});




                bullet = new F_LaserBulletType() {{
                    declinePierceCap = 1;
                    declinePierceDamage = 0.4f;
                    damagePrecent = 0.25f;

                    damage = 1;
                    length = 51200f;
                    width = 40f;
                    sideLength = 320f;
                    sideWidth = 2f;
                    sideAngle = 168f;
                    laserAbsorb = false;
                    lifetime = 130f;
                    keepVelocity = false;
                    chargeEffect = new MultiEffect(xx_Fx.f_AheadCharge , xx_Fx.f_AheadCharge2);
                    shootEffect = Fx.none;

                    absorbable = false;
                    hittable = false;
                }};

            }});











        }};






    }
}
