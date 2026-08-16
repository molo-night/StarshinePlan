package xx.expand;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.IntSet;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.RailBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Entityc;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import xx.xx_Type.LinkRailBullet;
import xx.xx_Type.RailBullet2Type;

import static arc.graphics.g2d.Draw.color;

public class F_AheadWeapon extends ChargeLockWeapon{

    public F_AheadWeapon(String name) {
        super(name);
    }

    public static class BulletData {
        public IntSet unitIds = new IntSet();
        public float extraDamage = 0;
    }

    @Override
    protected void bullet(Unit unit, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover) {
        if (!unit.isAdded()) return;

        BulletData extraData = new BulletData();
        if (unit instanceof F_CompositeUnitEntity f) {
            extraData.extraDamage = f.record2;
            f.record2 = 0;
        }

        // 计算子弹位置和角度（复制父类的逻辑）
        float xSpread = Mathf.range(xRand);
        float ySpread = Mathf.range(yRand);
        float weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation);
        float mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y);
        float mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);
        float bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread);
        float bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread);
        float shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset;
        float lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / bullet.range) : 1f;
        float angle = shootAngle + Mathf.range(inaccuracy + bullet.inaccuracy);

        Entityc shooter = unit.controller() instanceof MissileAI ai ? ai.shooter : unit;


        mount.bullet = bullet.create(unit, shooter, unit.team, bulletX, bulletY, angle, -1f,
                (1f - velocityRnd) + Mathf.random(velocityRnd) + extraVelocity,
                lifeScl, extraData, mover, mount.aimX, mount.aimY, mount.target);

        handleBullet(unit, mount, mount.bullet);


        if (!continuous) {
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);
        } else {
            initialShootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);
        }

        if (mount.allowShootEffects) {
            ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
            bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
            bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
        }

        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, bullet.recoil));
        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        if (recoils > 0) {
            mount.recoils[mount.barrelCounter % recoils] = 1f;
        }
        mount.heat = 1f;
    }


    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {



        int mapLength = Math.max (Vars.world.height() , Vars.world.width()) * 32;
        super.shoot(unit, mount, shootX, shootY, rotation);


        BulletType tracker = new BasicBulletType() {{//头武器的后续攻击
            backRegion = Core.atlas.find("clear-back");
            frontRegion = Core.atlas.find("clear");
            hitSize = 32f;
            height = 0f;
            width = 0;
            speed = 0;
            splashDamage = 1600f;
            splashDamageRadius = 32f;
            lifetime = 0f;
            hitEffect = despawnEffect = Fx.none;
            hitSound = Sounds.explosionQuad;
            hitSoundVolume = 1f;
            collidesGround = false;
            collidesAir = false;
            removeAfterPierce = false;
            laserAbsorb = false;
            collidesTiles = false;
            collides = false;
            keepVelocity = false;
            hittable = false;
            reflectable = false;
            absorbable = false;



            fragBullets = 4;
            fragRandomSpread = 0;
            fragSpread = 90;
            fragAngle = 0;
            fragBullet = new RailBullet2Type(){{
                declinePierceCap = 1;
                declinePierceDamage= 0.5f;
                backRegion = Core.atlas.find("clear-back");
                frontRegion = Core.atlas.find("clear");
                damage = 100000;
                length = mapLength;
                height = 8f;
                pierceDamageFactor = 0.5f;
                pierceArmor = true;
                pierceCap = -1;

                keepVelocity = false;
                hittable = false;
                reflectable = false;
                absorbable = false;

                hitEffect = Fx.none;
                despawnEffect = Fx.none;
                pointEffectSpace = 1f;
                pointEffect = new ParticleEffect() {{
                    particles = 1;
                    length = -1;
                    baseLength = 0;
                    lifetime = 120;
                    line = true;
                    randLength = false;
                    lenFrom = lenTo = mapLength;
                    strokeFrom = 8f;
                    colorFrom = colorTo = Pal.accent;
                    cone = 0;
                }};
            }};

            //对范围内每个单位发射一个RailBulletType
            intervalRandomSpread = 360f;
            intervalDelay = -1f;
            intervalBullets = 3;
            intervalBullet = new LinkRailBullet(){{//这个不重要
                backRegion = Core.atlas.find("shell-back");
                frontRegion = Core.atlas.find("shell");

                damage = 0;
                speed = 8;
                lifetime =230;
                width = 6;
                height = 6;
                trailColor = Pal.surge;
                trailLength = 12;
                trailWidth = 3f;
                drag = 0.05f;
                scaledSplashDamage = true;

                splashDamage = 50f;
                splashDamageRadius = 40f;

                collidesGround = false;
                collidesAir = false;
                removeAfterPierce = false;
                laserAbsorb = false;
                collidesTiles = false;
                collides = false;
                keepVelocity = false;
                hittable = false;
                reflectable = false;
                absorbable = false;

                despawnHit = true;


                pierceArmor = true;

                hitEffect = despawnEffect = new MultiEffect(
                        new Effect(40f, e->{
                            float radius =  138f;
                            Draw.z(Layer.effect);
                            Draw.color(Pal.accent , 0.8f * e.fout(Interp.pow2In));
                            Lines.stroke(2f * e.fin() );
                            Lines.circle(e.x, e.y, radius);
                            Draw.reset();
                        }),
                        xx_Fx.createStarBomb(40f,2f,2f)
                );



                linkBullet = new RailBulletType(){{


                    length = 138f;
                    damage = 10000f;

                    pierceArmor = true;
                    scaleLife = true;

                    hitEffect = xx_Fx.X_Bomb;
                    despawnEffect = xx_Fx.X_Bomb;
//                    pointEffectSpace = 1f;
//                    pointEffect = new ParticleEffect() {{
//                        particles = 1;
//                        length = 1;
//                        baseLength = 0;
//                        lifetime = 140;
//                        line = true;
//                        randLength = false;
//                        lenFrom = lenTo = 1f;
//                        strokeFrom = 4f;
//                        colorFrom = colorTo = Pal.accent;
//                        cone = 0;
//                    }};
                }};
            }};

        }};


        float offsetX = this.x;  // 注意：Weapon 的 x 和 y 是相对于单位的偏移
        float offsetY = this.y;

        float delay = 200f;
        int count = 127;
        float spacing = 400f;
        float intervalFrames = 1f;


        Time.run(delay, () -> {
            float currentAngle = unit.rotation + mount.rotation;
            float currentWeaponX = unit.x + Angles.trnsx(unit.rotation - 90, offsetX, offsetY);
            float currentWeaponY = unit.y + Angles.trnsy(unit.rotation - 90, offsetX, offsetY);


            for (int i = 0; i < count; i++) {
                float offset = i * spacing;
                float px = currentWeaponX + Mathf.cosDeg(currentAngle) * offset;
                float py = currentWeaponY + Mathf.sinDeg(currentAngle) * offset;
                Time.run((float) i * intervalFrames, () -> {
                    Time.run(xx_Fx.f_AheadSpot.lifetime, () -> {
                        tracker.create(unit, unit.team, px, py, 0f,1f, 1f, null);
                        Sounds.explosionQuad.at(px, py,1f, 4f);
                    });
                    xx_Fx.f_AheadShoot.at(px, py);
                    xx_Fx.f_AheadSpot.at(px, py);
                });
            }

        });
    }
}
