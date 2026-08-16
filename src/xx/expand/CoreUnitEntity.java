package xx.expand;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.UnitEntity;
import mindustry.world.blocks.storage.CoreBlock;

public class CoreUnitEntity extends UnitEntity implements CoreUnitc{
    private CoreBlock.CoreBuild proxy = null;//虚拟核心

    protected boolean
            worldLoadingFlag = false,//是否在读档
            flag = !dead;//标记单位状态，原版dead会在读档时改变

    @Override
    public CoreBlock.CoreBuild proxy() { return proxy; }

    @Override
    public void proxy(CoreBlock.CoreBuild core) { this.proxy = core; }

    @Override
    public void add() {
        super.add();
        if(proxy == null) CoreInjector.injectVirtualCore(team.data(), this);
        if(flag) dead = false;
    }

    @Override
    public void destroy() {
        if (!worldLoadingFlag) {
            super.destroy();
            CoreInjector.removeVirtualCore(team.data(), this);
        }
        worldLoadingFlag = false;
    }

    @Override
    public void afterRead() {
        if (type != null) {
            this.worldLoadingFlag = true;
        }
        super.afterRead();
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.bool(dead);
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        flag = !read.bool();
    }
}
