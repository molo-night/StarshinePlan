package xx.expand;

import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.game.Teams.*;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.CoreBlock.*;

public class CoreInjector {

    /**
     * 创建虚拟核心并注入单位。
     */
    public static void injectVirtualCore(TeamData data, CoreUnitc unit) {
        if (unit.proxy() != null) return;

        CoreBlock block = (CoreBlock) Blocks.coreShard;
        CoreBuild virtualCore = (CoreBuild) block.newBuilding().create(block, data.team);
        virtualCore.tile = null;

        unit.proxy(virtualCore);
        data.cores.add(virtualCore);

        Log.info("虚拟核心已注入 @", unit.type().name);
    }

    /**
     * 移除虚拟核心。
     */
    public static void removeVirtualCore(TeamData data, CoreUnitc unit) {
        CoreBuild proxy = unit.proxy();
        if (proxy != null) {
            data.cores.remove(proxy);
            unit.proxy(null);
            Log.info("🗑 虚拟核心已移除");
        }
    }
}