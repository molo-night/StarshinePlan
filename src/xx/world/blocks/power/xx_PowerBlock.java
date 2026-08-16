package xx.world.blocks.power;

import mindustry.world.meta.BlockGroup;
import xx.world.xx_Block;

public class xx_PowerBlock extends xx_Block {

    public xx_PowerBlock(String name){
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        group = BlockGroup.power;
    }
}
