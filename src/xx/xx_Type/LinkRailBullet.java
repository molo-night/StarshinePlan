package xx.xx_Type;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.RailBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import xx.expand.F_CompositeUnitEntity;
import xx.expand.xx_Fx;

public class LinkRailBullet extends BasicBulletType {

    public @Nullable BulletType linkBullet = null;



    public LinkRailBullet() {
        super();
        scaleLife = true;
    }



    public void createLink(Bullet b, float x, float y){

        Seq<Unit> targets = new Seq<>();

        float radius = linkBullet.lifetime * linkBullet.speed;
        if (linkBullet instanceof RailBulletType lb){
            radius = lb.length;
        }
        Units.nearbyEnemies(b.team, b.x, b.y, radius, unit -> {
            if (!unit.isValid() || unit.dead()) return;
            targets.add(unit);
        });

        for(Unit target : targets){
            float angle = b.angleTo(target);
            xx_Fx.createLine(60f , x , y , target.x , target.y ,3f , Pal.accent).at(x,y);
            linkBullet.create(b.owner,b.team,b.x,b.y,angle,-1,1f,1f,null,null,target.x,target.y);
        }


    }

    public float distanceToMapBoundary(float x, float y) {
        float width = Vars.world.width() * 8f;
        float height = Vars.world.height() * 8f;

        // 将点限制在矩形内，计算偏移量
        float clampedX = Mathf.clamp(x, 0f, width);
        float clampedY = Mathf.clamp(y, 0f, height);

        return Mathf.dst(x, y, clampedX, clampedY);
    }

    @Override
    public void hit(Bullet b, float x, float y, boolean createFrags){
        super.hit(b,x,y,createFrags);

        float dist = distanceToMapBoundary(x,y);
        if (dist > 138f) return;

        if(linkBullet != null) createLink(b,x,y);



    }


}
