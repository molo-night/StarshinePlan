package xx.world.blocks.production;

import arc.audio.RandomSound;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.Conduit.*;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.consumers.ConsumeLiquids;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import xx.gen.xx_Building;
import xx.world.xx_Block;

import static mindustry.Vars.*;

public class xx_GenericCrafter extends xx_Block {
    /** Written to outputItems as a single-element array if outputItems is null. */
    public @Nullable ItemStack outputItem;
    /** Overwrites outputItem if not null. */
    public @Nullable ItemStack[] outputItems;

    /** Written to outputLiquids as a single-element array if outputLiquids is null. */
    public @Nullable LiquidStack outputLiquid;
    /** Overwrites outputLiquid if not null. */
    public @Nullable LiquidStack[] outputLiquids;
    /** Liquid output directions, specified in the same order as outputLiquids. Use -1 to dump in every direction. Rotations are relative to block. */
    public int[] liquidOutputDirections = {-1};

    /** if true, crafters with multiple liquid outputs will dump excess when there's still space for at least one liquid type */
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;

    public float craftTime = 80;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    /** Only used for legacy cultivator blocks. */
    @NoPatch
    public boolean legacyReadWarmup = false;

    public DrawBlock drawer = new DrawDefault();

    public xx_GenericCrafter(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        ambientSound = Sounds.loopMachine;
        sync = true;
        ambientSoundVolume = 0.03f;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;
    }

    @Override
    public void setStats(){
        stats.timePeriod = craftTime;
        super.setStats();
        if((hasItems && itemCapacity > 0) || outputItems != null){
            stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        }

        if(outputItems != null){
            stats.add(Stat.output, StatValues.items(craftTime, outputItems));
        }

        if(outputLiquids != null){
            stats.add(Stat.output, StatValues.liquids(1f, outputLiquids));
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        //set up liquid bars for liquid outputs
        if(outputLiquids != null && outputLiquids.length > 0){
            //no need for dynamic liquid bar
            removeBar("liquid");

            //then display output buffer
            for(var stack : outputLiquids){
                addLiquidBar(stack.liquid);
            }
        }
    }

    @Override
    public boolean rotatedOutput(int fromX, int fromY, Tile destination){
        if(!(destination.build instanceof ConduitBuild)) return false;

        Building crafter = world.build(fromX, fromY);
        if(crafter == null) return false;
        int relative = Mathf.mod(crafter.relativeTo(destination) - crafter.rotation, 4);
        for(int dir : liquidOutputDirections){
            if(dir == -1 || dir == relative) return false;
        }

        return true;
    }

    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }

    @Override
    public void init(){

        if(destroySound == Sounds.unset){
            destroySound =
                    size >= 3 ? Sounds.blockExplode3 :
                            size >= 2 ? new RandomSound(Sounds.blockExplode2, Sounds.blockExplode2Alt) :
                                    new RandomSound(Sounds.blockExplode1, Sounds.blockExplode1Alt);
        }

        if(placeSound == Sounds.unset){
            placeSound =
                    size >= 3 ? Sounds.blockPlace3 :
                            size >= 2 ? Sounds.blockPlace2 :
                                    Sounds.blockPlace1;
        }

        if(breakSound == Sounds.unset){
            breakSound =
                    size >= 3 ? Sounds.blockBreak3 :
                            size >= 2 ? Sounds.blockBreak2 :
                                    Sounds.blockBreak1;
        }

        //disable standard shadow
        if(customShadow){
            hasShadow = false;
        }

        if(underBullets){
            priority = TargetPriority.under;
        }

        if(fogRadius > 0){
            flags = flags.with(BlockFlag.hasFogRadius);
        }

        if(sync){
            flags = flags.with(BlockFlag.synced);
        }

        //initialize default health based on size
        if(health == -1){
            boolean round = false;
            if(scaledHealth < 0){
                scaledHealth = 40;

                float scaling = 1f;
                for(var stack : requirements){
                    scaling += stack.item.healthScaling;
                }

                scaledHealth *= scaling;
                round = true;
            }

            health = round ?
                    Mathf.round(size * size * scaledHealth, 5) :
                    (int)(size * size * scaledHealth);
        }

        clipSize = Math.max(clipSize, size * tilesize);

        lightClipSize = Math.max(lightClipSize, clipSize);

        if(hasLiquids && drawLiquidLight){
            emitLight = true;
            lightClipSize = Math.max(lightClipSize, size * 30f * 2f);
        }

        if(emitLight){
            lightClipSize = Math.max(lightClipSize, lightRadius * 2f);
        }

        if(group == BlockGroup.transportation || category == Category.distribution){
            acceptsItems = true;
        }

        offset = ((size + 1) % 2) * tilesize / 2f;
        sizeOffset = -((size - 1) / 2);

        if(requirements.length > 0 && buildTime < 0){
            buildTime = 0f;
            for(ItemStack stack : requirements){
                buildTime += stack.amount * stack.item.cost;
            }
        }//根据建造需求自动计算建造时间

        if(buildTime < 0){
            buildTime = 20f;
        }

        buildTime *= buildCostMultiplier;

        consumers = consumeBuilder.toArray(Consume.class);
        optionalConsumers = consumeBuilder.select(consume -> consume.optional && !consume.ignore()).toArray(Consume.class);
        nonOptionalConsumers = consumeBuilder.select(consume -> !consume.optional && !consume.ignore()).toArray(Consume.class);
        updateConsumers = consumeBuilder.select(consume -> consume.update && !consume.ignore()).toArray(Consume.class);
        hasConsumers = consumers.length > 0;
        itemFilter = new boolean[content.items().size];
        liquidFilter = new boolean[content.liquids().size];

        for(Consume cons : consumers){
            cons.apply(this);
        }

        setBars();

        stats.useCategories = true;

        //TODO check for double power consumption

        if(!logicConfigurable){
            configurations.each((key, val) -> {
                if(UnlockableContent.class.isAssignableFrom(key)){
                    logicConfigurable = true;
                }
            });
        }

        if(!outputsPower && consPower != null && consPower.buffered){
            Log.warn("Consumer using buffered power: @. Disabling buffered power.", name);
            consPower.buffered = false;
        }

        if(buildVisibility == BuildVisibility.sandboxOnly){
            hideDetails = false;
        }
    }

    @Override
    public void afterPatch(){
        super.afterPatch();

        outputsLiquid = outputLiquids != null;

        if(outputItems != null) hasItems = true;
        if(outputLiquids != null) hasLiquids = true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public boolean outputsItems(){
        return outputItems != null;
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        if(outputLiquids != null){
            for(int i = 0; i < outputLiquids.length; i++){
                int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                if(dir != -1){
                    Draw.rect(
                            outputLiquids[i].liquid.fullIcon,
                            x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                            y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                            8f, 8f
                    );
                }
            }
        }
    }

    public class GenericCrafterBuild extends xx_Building {
        public float progress;
        public float totalProgress;
        public float warmup;

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override//检测输出空间是否充足。备注：这将检测这次输出后的空间
        public boolean shouldConsume(){
            //关于物品
            if(outputItems != null){
                for(var output : outputItems){
                    if(items.get(output.item) + output.amount > itemCapacity){
                        return false;
                    }
                }
            }

            //关于流体
            if(outputLiquids != null && !ignoreLiquidFullness){
                boolean allFull = true;
                for(var output : outputLiquids){
                    if(liquids.get(output.liquid) + output.amount > liquidCapacity){
                        if(!dumpExtraLiquid){
                            return false;
                        }
                    }else allFull = false;//检测每种液体是否都会填满输出空间
                }

                return !allFull;
            }
            //return enabled 是否启用在后面检测，这个方法仅用于检测输出空间是否充足以支持生产
            return true;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){

                progress += getProgressIncrease(craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency
                if(outputLiquids != null){
                    float inc = getProgressIncrease(1f);
                    for(var output : outputLiquids){
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                craft();
            }

            dumpOutputs();
        }

        @Override//获取progress增加量
        public float getProgressIncrease(float baseTime){
            return super.getProgressIncrease(baseTime);//TODO 待定重写
        }

        public float warmupTarget(){
            return 1f;
        }//我不知道这个存在的意义。一个小小的疑惑。

        @Override//获得当前预热进度
        public float warmup(){
            return warmup;
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        public void craft(){
            consume();

            if(outputItems != null){
                for(var output : outputItems){
                    for(int i = 0; i < output.amount; i++){
                        offload(output.item);
                    }
                }
            }

            if(wasVisible){
                craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        public void dumpOutputs(){
            if(outputItems != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }

            if(outputLiquids != null){
                for(int i = 0; i < outputLiquids.length; i++){
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                    dumpLiquid(outputLiquids[i].liquid, 2f, dir);
                }
            }
        }//向其他方块主动输出物品与液体

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return progress();
            //attempt to prevent wild total liquid fluctuation, at least for crafters
            if(sensor == LAccess.totalLiquids && outputLiquid != null) return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override//限制显示0-1
        public float progress(){
            return Mathf.clamp(progress);
        }

        @Override//物品的最大获取量
        public int getMaximumAccepted(Item item){
            return itemCapacity;
        }

        @Override//是否播放工厂环境音效
        public boolean shouldAmbientSound(){
            return efficiency > 0;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.f(warmup);
            if(legacyReadWarmup) write.f(0f);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            if(legacyReadWarmup) read.f();
        }
    }
}

