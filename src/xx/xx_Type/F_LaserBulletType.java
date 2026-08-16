package xx.xx_Type;

import arc.struct.IntSet;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;
import xx.expand.F_AheadWeapon;

public class F_LaserBulletType extends LaserBulletType {
    public int declinePierceCap = 0;
    public float declinePierceDamage = 1f;
    public float damagePrecent = 0f;

    public F_LaserBulletType() {
        super();
    }

    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b,entity,health);

        if(entity instanceof Unit unit && b.data instanceof F_AheadWeapon.BulletData data){
            data.unitIds.add(entity.id());
            if (data.unitIds.size <= declinePierceCap ){
                b.damage *= declinePierceDamage;
                unit.health -= unit.maxHealth *  damagePrecent;
            }
            else b.data = null;
        }

    }




    @Override
    public void init(Bullet b) {
        if (b.data instanceof F_AheadWeapon.BulletData data) {
            if (data.extraDamage > 0f) {
                b.damage += data.extraDamage;
            }
        }
        super.init(b);
    }


}
