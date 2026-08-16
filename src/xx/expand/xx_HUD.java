package xx.expand;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.gen.Unit;
import mindustry.ui.Styles;

import static mindustry.Vars.player;

public class xx_HUD {

    private final Table container;
    private final Table dataPanel;

    public xx_HUD() {
        container = new Table();
        container.top().left();
        container.setFillParent(true);
        dataPanel = new Table();
        dataPanel.top().left();
        dataPanel.setFillParent(true);
    }







    public void build() {
        container.table(Styles.none, panel -> {
            // 进度条
            panel.add(new xx_SideBar(
                    () -> player.unit() instanceof F_CompositeUnitEntity revive ?
                            (float) revive.revivesSum / revive.revivesMax : 0f,
                    () -> player.unit() instanceof F_CompositeUnitEntity revive && revive.revivesSum == 1,
                    true,
                    xx_Pal.colorRevive00,
                    Color.valueOf("00000000")

            )).width(40f).growY().padRight(-17);

        }).size(1, 80).padRight(4);





        dataPanel.table(Styles.none, data -> {
            data.left().margin(4f);//表格边框宽度
            data.defaults().padTop(1.4f).padBottom(1.4f);//上间距 下间距 大小缩放

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]当前生命:  [white]" + (int) f.health + " / " + f.maxHealth + " * " + f.revivesSum;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]原版护盾:  [white]" + (int) f.shield;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]血条冷却:  [white]" + (int)f.reviveCounter +" / "+f.reviveDelay;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]核心护盾:  [white]" + (int) f.coreShield;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]免疫层数:  [white]" + f.cleanIntervals + " / " + f.maxCleanIntervals;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]适应容量:  [white]" +  f.recordedDamage;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]上次伤害:  [white]" +  f.recorded;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]检测时间:  [white]" +  (int)f.sssCounter  + " / " + f.examineDelay;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]血量倍率:  [white]" +  f.healthMultiplier + " " + f.hasHealthMultiplier + " " + (int)f.healthMultiplierCounter * 10f +" / 600";
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]速度倍率:  [white]" +  f.speedMultiplier;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]伤害倍率:  [white]" +  f.damageMultiplier;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]装填冷却:  [white]" +  f.reloadMultiplier;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]建造速率:  [white]" +  f.buildSpeedMultiplier;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]阻力系数:  [white]" +  f.dragMultiplier;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]护甲覆盖:  [white]" +  f.armorOverride;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]自身护甲:  [white]" +  f.armor + " + " + f.coreArmor;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]附加护甲:  [white]" +  f.extraArmorShield;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]损伤总量:  [white]" +  Math.round(f.rawDamageSum * 10d) / 10d;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]治疗总量:  [white]" +  Math.round(f.healSum * 10d) / 10d;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();

            data.label(() -> {
                if (player.unit() instanceof F_CompositeUnitEntity f) {
                    return "[accent]头炮增伤:  [white]" +  f.record2 ;
                }
                return "";
            }).style(Styles.outlineLabel).left();
            data.row();







        }).padTop(90f).padLeft(0f);




        Core.scene.add(container);
        Core.scene.add(dataPanel);
    };
    public void update() {
        Unit unit = player.unit();
        container.visible = unit instanceof F_CompositeUnitEntity;
        dataPanel.visible = unit instanceof F_CompositeUnitEntity;
    }







}