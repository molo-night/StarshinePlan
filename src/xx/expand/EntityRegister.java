package xx.expand;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;

public class EntityRegister {

    private static final ObjectMap<Class<?>, Prov<?>> pending = new ObjectMap<>();
    private static final ObjectMap<Class<?>, Integer> ids = new ObjectMap<>();

    public static void put(Class<?> c, Prov<?> prov) {
        pending.put(c, prov);
    }

    public static void load() {
        for (var entry : pending) {
            int id = EntityMapping.register(entry.key.getName(), entry.value);
            ids.put(entry.key, id);
        }
        pending.clear();
    }

    public static int getID(Class<?> c) {
        return ids.get(c, -1);
    }
}