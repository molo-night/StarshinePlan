package xx.xx_Type;

import mindustry.entities.bullet.RailBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;

public class RailBullet2Type extends RailBulletType {

    public int declinePierceCap = 0;
    public float declinePierceDamage = 1f;

    public RailBullet2Type() {
        super();
    }

    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b,entity,health);

        if (b.collided.size <= declinePierceCap)
            b.damage *= declinePierceDamage;
    }

}
