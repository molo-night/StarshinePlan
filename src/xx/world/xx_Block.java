package xx.world;

import arc.audio.RandomSound;
import arc.math.Mathf;
import arc.util.Log;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.TargetPriority;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.consumers.ConsumeLiquids;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BuildVisibility;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

public class xx_Block extends Block {
    public boolean connected_xx_Power = true;//是否连接xx电网


    public xx_Block(String name) {
        super(name);
        this.connectedPower = false;
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

        //some blocks don't have hasLiquids, but have liquid consumers/liquid capacity
//        if(liquidCapacity < 0){
//            float consumeAmount = 1f;
//            for(var cons : consumeBuilder){
//                if(cons instanceof ConsumeLiquidBase liq){
//                    consumeAmount = Math.max(consumeAmount, liq.amount * 60f);
//                }else if(cons instanceof ConsumeLiquids liq){
//                    for(var stack : liq.liquids){
//                        consumeAmount = Math.max(consumeAmount, stack.amount * 60f);
//                    }
//                }
//            }
//            liquidCapacity = Mathf.round(10f * consumeAmount);
//        }

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
        }

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

    @Override//我不知道这个有什么用
    public void reinitializeConsumers(){
        Log.info("reinitializeConsumers()被调用，你知道这是在哪调用的吗？");
        super.reinitializeConsumers();

    }



}
