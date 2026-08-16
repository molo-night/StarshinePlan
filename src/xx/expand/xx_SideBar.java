package xx.expand;

import arc.func.Boolp;
import arc.func.Floatp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.scene.Element;
import arc.util.Tmp;
import mindustry.graphics.Pal;

class xx_SideBar extends Element {
    public final Floatp amount;
    public final boolean flip;
    public final Boolp flash;

    float last, blink, value;

    public Color backgroundcolor = Pal.darkishGray;

    public xx_SideBar(Floatp amount, Boolp flash, boolean flip){
        this.amount = amount;
        this.flip = flip;
        this.flash = flash;

        setColor(Pal.health);
    }
    public xx_SideBar(Floatp amount, Boolp flash, boolean flip, Color color){
        this.amount = amount;
        this.flip = flip;
        this.flash = flash;

        setColor(color);
    }

    public xx_SideBar(Floatp amount, Boolp flash, boolean flip, Color color, Color backgroundcolor){
        this.amount = amount;
        this.flip = flip;
        this.flash = flash;

        setColor(color);
        this.backgroundcolor = backgroundcolor;
    }

    @Override
    public void draw(){
        float next = amount.get();

        if(Float.isNaN(next) || Float.isInfinite(next)) next = 1f;

        if(next < last && flash.get()){
            blink = 1f;
        }

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, next, 0.15f);
        last = next;

        if(Float.isNaN(value) || Float.isInfinite(value)) value = 1f;

        drawInner(backgroundcolor, 1f);
        drawInner(Tmp.c1.set(color).lerp(Color.white, blink), value);
    }

    void drawInner(Color color, float fract){
        if(fract < 0) return;

        fract = Mathf.clamp(fract);
        if(flip){
            x += width;
            width = -width;
        }

        float stroke = width * 0.35f;
        float bh = height/2f;
        Draw.color(color, parentAlpha);

        float f1 = Math.min(fract * 2f, 1f), f2 = (fract - 0.5f) * 2f;

        float bo = -(1f - f1) * (width - stroke);

        Fill.quad(
                x, y,
                x + stroke, y,
                x + width + bo, y + bh * f1,
                x + width - stroke + bo, y + bh * f1
        );

        if(f2 > 0){
            float bx = x + (width - stroke) * (1f - f2);
            Fill.quad(
                    x + width, y + bh,
                    x + width - stroke, y + bh,
                    bx, y + height * fract,
                    bx + stroke, y + height * fract
            );
        }

        Draw.reset();

        if(flip){
            width = -width;
            x -= width;
        }
    }
}
