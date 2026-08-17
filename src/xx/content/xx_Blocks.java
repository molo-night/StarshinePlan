package xx.content;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.draw.*;
import xx.world.blocks.power.text_node;
import xx.world.blocks.power.text_node2;
import xx.world.blocks.power.xx_ConsumeGenerator;
import xx.world.blocks.production.ConsumeCrafter;
import xx.world.blocks.power.ElectricityPylon;
import xx.world.blocks.production.text_crafter;

import static mindustry.type.ItemStack.with;

public class xx_Blocks {

    public static Block
            electricityPylon,consumeCrafter;

    public static Block text_crafter,text_node,text_production,text_powerNode;

    public static void load(){

        electricityPylon = new ElectricityPylon("electricity-pylon"){{
            requirements(Category.power, with(Items.beryllium, 16));
            health = 90;
            range = 18;
            laserRange = 6;
            maxNodes = 8;
            crushFragile = true;
            underBullets = true;
            //consumePower(0.01f);
            //researchCost = with(Items.beryllium, 5);
            consumePowerBuffered(1000f);

        }};

        consumeCrafter = new ConsumeCrafter("consume-crafter"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.silicon, 1);
            craftTime = 240f;
            size = 2;
            hasPower = true;
            hasLiquids = false;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame(Color.valueOf("ffef99")));
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;

            consumeItems(with(Items.coal, 1, Items.sand, 2));
            consumePower(0.50f);
        }};

        text_crafter = new text_crafter("text_crafter"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.silicon, 1);
            craftTime = 240f;
            size = 6;
            hasPower = true;
            hasLiquids = false;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault());
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;

            maxVoltage = 20;
            maxCurrent = 20;

            consumeItems(with(Items.coal, 1, Items.sand, 2));
            consumePower(5f, 10,1);
            //consumePower(0.50f);
        }};

        text_node = new text_node("text_node"){{
            requirements(Category.power, with(Items.beryllium, 16));
            health = 90;
            range = 18;
            crushFragile = true;
            underBullets = true;
            //consumePower(0.01f);
            //researchCost = with(Items.beryllium, 5);
            //consumePowerBuffered(1000f);

        }};

        text_production = new xx_ConsumeGenerator("text_production"){{
            requirements(Category.power, with(Items.graphite, 100, Items.carbide, 60, Items.oxide, 60f, Items.silicon, 100));
            powerProduction = 10;
            protentionVoltage = 10;//发电电压


            consumeLiquids(LiquidStack.with(Liquids.slag, 20f / 60f, Liquids.arkycite, 40f / 60f));
            size = 3;

            liquidCapacity = 30f * 5;

            outputLiquid = new LiquidStack(Liquids.water, 20f / 60f);

            generateEffect = Fx.none;

            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.06f;

            researchCostMultiplier = 0.4f;
        }};

        text_powerNode = new text_node2("text_powerNode"){{
            requirements(Category.power, with(Items.copper, 2, Items.lead, 6));
            maxNodes = 10;
            laserRange = 6;
            underBullets = true;
            crushFragile = true;
        }};







    }


}
