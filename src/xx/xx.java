package xx;

import arc.*;
import arc.util.*;
import mindustry.Vars;
import xx.content.xx_UnitTypes;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import xx.content.xx_Blocks;
import xx.expand.EntityRegister;
import xx.expand.xx_HUD;
import xx.expand.F_CompositeUnitEntity;
import xx.world.meta.xx_Stat;
import xx.world.meta.xx_StatUnit;

public class xx extends Mod{


    private xx_HUD xx_HUD;

    static {
        EntityRegister.put(F_CompositeUnitEntity.class, F_CompositeUnitEntity::new);
    }

    @Override
    public void init() {
        // 分配 ID
        EntityRegister.load();
        xx_HUD = new xx_HUD();
        xx_HUD.build();

        Vars.renderer.minZoom = 0.01f;   // 默认 1.5，越小能缩越远
        Vars.renderer.maxZoom = 60f;    // 默认 15，越大能放越大



    }






    public xx(){
//        Log.info("Loaded ExampleJavaMod constructor.");
//
//        //listen for game load event
//        Events.on(ClientLoadEvent.class, e -> {
//            //show dialog upon startup
//            Time.runTask(10f, () -> {
//                BaseDialog dialog = new BaseDialog("frog");
//                dialog.cont.add("behold").row();
//                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
//                dialog.cont.image(Core.atlas.find("xx-java-mod-frog")).pad(20f).row();
//                dialog.cont.button("I see", dialog::hide).size(100f, 50f);
//                dialog.show();
//            });
//        });


        Events.run(Trigger.update, () -> {
            if (xx_HUD != null) {
                xx_HUD.update();
            }
        });






    }

    @Override
    public void loadContent(){
        Log.info("Loading some xx content.");
        xx_UnitTypes.load();
        EntityRegister.load();
        xx_Blocks.load();
    }


}
