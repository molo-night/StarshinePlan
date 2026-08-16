package xx.expand;

import arc.util.Log;
import arc.util.Time;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class ChargeLockWeapon extends Weapon {

    public float postLockDuration = 60f; // 锁定帧数

    public ChargeLockWeapon(String name) {
        super(name);
        this.mountType = ChargeMount::new;
    }

    public static class ChargeMount extends WeaponMount {
        public float lockedAngle = 0f;

        public ChargeMount(Weapon weapon) {
            super(weapon);
        }
    }

    @Override
    public void update(Unit unit, WeaponMount mount) {
        ChargeMount cm = (ChargeMount) mount;
        boolean charging =  mount.charging && mount.charge < 1f;

        if (charging) {
            // 蓄力开始时记录当前角度
            cm.lockedAngle = mount.rotation;
            mount.rotation = cm.lockedAngle;
            mount.targetRotation = cm.lockedAngle;
            mount.rotate = false;
        }

        float lastReload = mount.reload;
        super.update(unit, mount);
        if (charging) {
            mount.rotation = cm.lockedAngle;
            mount.reload = lastReload;
//            Log.info("A"+mount.reload);
//            Log.info("B"+mount.rotation);
        }
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
        super.shoot(unit,mount,shootX,shootY,rotation);
        Time.run(this.shoot.firstShotDelay, () -> {
            mount.charging = false;
            mount.charge = 0;
        });
    }


}