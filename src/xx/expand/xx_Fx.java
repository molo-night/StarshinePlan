package xx.expand;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;


public class xx_Fx {



    public static final Effect fxConveyCircle = new Effect(25f, e -> {

        float progress = e.fout(Interp.pow2Out);

        float radius = progress * 300f;

        float alpha = e.fin(Interp.pow2In);//in缓入 out缓出

        Draw.z(Layer.effect);
        Draw.alpha(alpha);
        Draw.color(Color.white);


        Lines.stroke(3f * alpha);
        Lines.square(e.x, e.y, radius,45);



        Draw.reset();


        float progress2 = e.fout();

        float radius2 = progress2 * 300f;

        float alpha2 = e.fin();


        Draw.z(Layer.effect);
        Draw.alpha(alpha2);
        Draw.color(Color.white);

        Lines.stroke(3f * alpha2);
        Lines.square(e.x, e.y, radius2,45);


        Draw.reset();
    }),
            //替补
    fx= new Effect(0f, e -> {
        Draw.reset();
    }),


    fxConveyFillCircle= new Effect(150f, e -> {

        float progress = e.fout(Interp.pow2In);

        float radius = progress * 30f;

        Draw.z(Layer.effect);
        Draw.alpha(1);
        Draw.color(Color.white);

        Lines.stroke(1f);
        Fill.circle(e.x, e.y, radius);

        Draw.reset();
    }),






    fxConveyCircleB= new Effect(60f, e -> {

        float progress = e.fin(Interp.pow2Out);

        float radius = progress * 400f;

        float alpha = e.fout(Interp.pow2In);

        Draw.z(Layer.effect);
        Draw.alpha(alpha);
        Draw.color(Color.white);

        Lines.stroke(1.4f * alpha);
        Lines.square(e.x, e.y, radius,45);


        Draw.reset();
    });

    public static final Effect f_AheadCharge = new Effect(205f, e -> {
        float angle = e.rotation;

        Draw.z(Layer.effect);
        Draw.color(Color.white, 1f);

        float longAxis = 400f * e.fin(Interp.pow2Out);   // 长轴
        float shortAxis =10f * e.fout(Interp.pow2Out);   // 短轴
        float moveX = 6 * e.fin(Interp.pow5Out) * Mathf.cosDeg(angle);
        float moveY = 6 * e.fin(Interp.pow5Out) * Mathf.sinDeg(angle);
        float moveX2 = -64 * e.fin() * Mathf.cosDeg(angle);
        float moveY2 = -64 * e.fin() * Mathf.sinDeg(angle);


        float cos = Mathf.cosDeg(angle);
        float sin = Mathf.sinDeg(angle);



        float topX = e.x + (-0 * cos - longAxis * sin) + moveX2;
        float topY = e.y + (-0 * sin + longAxis * cos) + moveY2;

        float rightX = e.x + (shortAxis * cos - 0 * sin) + moveX;
        float rightY = e.y + (shortAxis * sin + 0 * cos) + moveY;

        float bottomX = e.x + (0 * cos - (-longAxis) * sin) + moveX2;
        float bottomY = e.y + (0 * sin + (-longAxis) * cos) + moveY2;

        float leftX = e.x + (-shortAxis * cos - 0 * sin) + moveX;
        float leftY = e.y + (-shortAxis * sin + 0 * cos) + moveY;


        Fill.tri(topX, topY, rightX, rightY, leftX, leftY);
        Fill.tri(rightX, rightY, bottomX, bottomY, leftX, leftY);


        float radius = 380 * e.fout(Interp.pow3In);
        Draw.color(Color.white , 1f);
        Lines.stroke(2f * e.fin());

        Lines.circle(e.x, e.y, radius);




        float extraAngle = 25f * e.fin(); // 旋转偏移角度
        float angle2 = angle + extraAngle;

        // 可独立调整尺寸
        float longAxis2 = 300f * e.fin(Interp.pow2Out);
        float shortAxis2 = 8f * e.fout(Interp.pow2Out);
        float moveX2_extra = 6 * e.fin(Interp.pow5Out) * Mathf.cosDeg(angle2);
        float moveY2_extra = 6 * e.fin(Interp.pow5Out) * Mathf.sinDeg(angle2);
        float moveX2_opp = -64 * e.fin() * Mathf.cosDeg(angle2);
        float moveY2_opp = -64 * e.fin() * Mathf.sinDeg(angle2);

        float cos2 = Mathf.cosDeg(angle2);
        float sin2 = Mathf.sinDeg(angle2);

        float topX2 = e.x + (-0 * cos2 - longAxis2 * sin2) + moveX2_opp;
        float topY2 = e.y + (-0 * sin2 + longAxis2 * cos2) + moveY2_opp;
        float rightX2 = e.x + (shortAxis2 * cos2 - 0 * sin2) + moveX2_extra;
        float rightY2 = e.y + (shortAxis2 * sin2 + 0 * cos2) + moveY2_extra;
        float bottomX2 = e.x + (0 * cos2 - (-longAxis2) * sin2) + moveX2_opp;
        float bottomY2 = e.y + (0 * sin2 + (-longAxis2) * cos2) + moveY2_opp;
        float leftX2 = e.x + (-shortAxis2 * cos2 - 0 * sin2) + moveX2_extra;
        float leftY2 = e.y + (-shortAxis2 * sin2 + 0 * cos2) + moveY2_extra;

        Fill.tri(topX2, topY2, rightX2, rightY2, leftX2, leftY2);
        Fill.tri(rightX2, rightY2, bottomX2, bottomY2, leftX2, leftY2);

        extraAngle = -25f * e.fin(); // 旋转偏移角度
        angle2 = angle + extraAngle;

        // 可独立调整尺寸
        longAxis2 = 300f * e.fin(Interp.pow2Out);
        shortAxis2 = 8f * e.fout(Interp.pow4Out);
        moveX2_extra = 6 * e.fin(Interp.pow5Out) * Mathf.cosDeg(angle2);
        moveY2_extra = 6 * e.fin(Interp.pow5Out) * Mathf.sinDeg(angle2);
        moveX2_opp = -32 * e.fin() * Mathf.cosDeg(angle2);
        moveY2_opp = -32 * e.fin() * Mathf.sinDeg(angle2);

        cos2 = Mathf.cosDeg(angle2);
        sin2 = Mathf.sinDeg(angle2);

        topX2 = e.x + (-0 * cos2 - longAxis2 * sin2) + moveX2_opp;
        topY2 = e.y + (-0 * sin2 + longAxis2 * cos2) + moveY2_opp;
        rightX2 = e.x + (shortAxis2 * cos2 - 0 * sin2) + moveX2_extra;
        rightY2 = e.y + (shortAxis2 * sin2 + 0 * cos2) + moveY2_extra;
        bottomX2 = e.x + (0 * cos2 - (-longAxis2) * sin2) + moveX2_opp;
        bottomY2 = e.y + (0 * sin2 + (-longAxis2) * cos2) + moveY2_opp;
        leftX2 = e.x + (-shortAxis2 * cos2 - 0 * sin2) + moveX2_extra;
        leftY2 = e.y + (-shortAxis2 * sin2 + 0 * cos2) + moveY2_extra;

        Fill.tri(topX2, topY2, rightX2, rightY2, leftX2, leftY2);
        Fill.tri(rightX2, rightY2, bottomX2, bottomY2, leftX2, leftY2);


        randLenVectors(e.id, 60, 320f * e.fout(Interp.pow2In), (x, y) -> {
            // 绘制粒子
            Fill.circle(e.x + x, e.y + y, 8f * e.fin());
            // 绘制灯光（可选）
            Drawf.light(e.x + x, e.y + y, 10f * e.fin(), Color.white, 0.6f);
        });

        randLenVectors(e.id, 40, 320f * e.fout(Interp.pow3In), (x, y) -> {
            // 绘制粒子
            Fill.circle(e.x + x, e.y + y, 8f * e.fin());
            // 绘制灯光（可选）
            Drawf.light(e.x + x, e.y + y, 10f * e.fin(), Color.white, 0.6f);
        });

        Draw.blend();
        Draw.color();
    }).followParent(true).rotWithParent(true);


    public static final Effect f_AheadCharge2 = new Effect(200f, e -> {
        float radius = 380 * e.fout(Interp.pow4In);
        Draw.color(Color.white , 1f);
        Lines.stroke(2f * e.fin());

        Lines.circle(e.x, e.y, radius);

        radius = 20f * e.fout();

        Fill.circle(e.x, e.y, radius);


        Draw.blend();
        Draw.color();
    }).followParent(true).rotWithParent(true);


    public static final Effect f_AheadShoot = new Effect(120f, e -> {
        float radius = 400 * e.fin(Interp.pow4Out);
        Draw.color(Color.white , e.fout());
        Lines.stroke(2f * e.fout());

        Lines.circle(e.x, e.y, radius);


        Draw.blend();
        Draw.color();
    }).followParent(true).rotWithParent(true);

    public static final Effect f_AheadSpot = new Effect(600f, e -> {
        float radius = 2f + 6 * e.fout();
        Draw.color(Color.white , e.fin());

        Fill.circle(e.x, e.y, radius);



        Draw.color();
    }).followParent(true).rotWithParent(true);

    public static final Effect f_AheadSpotShoot = new Effect(120f, e -> {
        float radius = 10 * e.fout();
        Draw.color(Color.white , 1);

        Fill.circle(e.x, e.y, radius);

        Draw.color();
    }).followParent(true).rotWithParent(true);

    public static final Effect missileTrail = new Effect(120, e -> {

        color(Pal.accent,0.5f * e.fout());

        Fill.square(e.x, e.y, 8 ,e.rotation);
    });


    public static final Effect X_Bomb = new Effect(40f, 100f, e -> {//X形
        color(Pal.accent);
        stroke(e.fout() * 2f);
        float circleRad = 4f + e.finpow() * 65f;
        Lines.circle(e.x, e.y, circleRad);

        color(Pal.accent);
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 6f, 100f * e.fout(), i*90 + 45);
        }

        color();
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 3f, 35f * e.fout(), i*90 + 45);
        }

        Drawf.light(e.x, e.y, circleRad * 1.6f, Pal.heal, e.fout());
    });

    public static final Effect star_Bomb = new Effect(40f, 100f, e -> {//+形
        color(Pal.accent);
        stroke(e.fout() * 2f);
        float circleRad = 4f + e.finpow() * 65f;
        Lines.circle(e.x, e.y, circleRad);

        color(Pal.accent);
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 6f, 100f * e.fout(), i*90);
        }

        color();
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 3f, 35f * e.fout(), i*90);
        }

        Drawf.light(e.x, e.y, circleRad * 1.6f, Pal.heal, e.fout());
    });

    //函数
    public static Effect createStarBomb(float life,float starSize , float circleSize){
        return new Effect(life, 100f, e -> {//+形
            color(Pal.accent);
            stroke(e.fout() * 2f);
            float circleRad = circleSize * (4f + e.finpow() * 65f);
            Lines.circle(e.x, e.y, circleRad);

            color(Pal.accent);
            for(int i = 0; i < 4; i++){
                Drawf.tri(e.x, e.y, 6f * starSize, starSize * 100f * e.fout(), i*90);
            }

            color();
            for(int i = 0; i < 4; i++){
                Drawf.tri(e.x, e.y, starSize * 3f, starSize * 35f * e.fout(), i*90);
            }

            Drawf.light(e.x, e.y, circleRad * 1.6f * starSize, Pal.heal, e.fout());
        });
    }

    public static Effect createLine(float life ,float x ,float y , float aimX , float aimY , float stroke , Color color ){
            return new Effect(life, e -> {
                Draw.color(color, e.fout());
                Lines.stroke(stroke * e.fout());
                Lines.line(x, y, aimX, aimY);
                Draw.color();
            });

    }




}
