package klmn.writing;

import util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeEnv
{
    // give each type an id, convert ID to JVM and KLMN types.

    private Map<String, Pair<Integer, String>> types = new HashMap<>();
    private List<String> ids = new ArrayList<>(); // to save stupid & time-wasting algorithms

    // todo: these will throw a null exception for non-defined types, do something nicer
    public String jvmType(int id) { return types.get(klmnType(id)).getValue(); }
    public String jvmType(String klmnType) { return types.get(klmnType).getValue(); }
    public int typeID(String klmnType) { return types.get(klmnType).getKey(); }
    public String klmnType(String jvmType) {
        for (Map.Entry<String, Pair<Integer, String>> p : types.entrySet())
            if (p.getValue().getValue().equals(jvmType)) return p.getKey();
        throw new RuntimeException("JVM type " + jvmType + " not defined");
    } // todo: a nicer, more object oriented structure (class for types & shit)
    public int jvmTypeID(String jvmType) {
        for (Pair<Integer, String> p : types.values())
            if (p.getValue().equals(jvmType)) return p.getKey();
        throw new RuntimeException("JVM type " + jvmType + " not defined");
    }
    public String klmnType(int id) { return ids.get(id); }

    public int add(String name, String jvm) {
        types.put(name, Pair.of(ids.size(), jvm));
        ids.add(name);
        return ids.size() - 1;
    }

    {
        add("int", "I");
        add("long", "J");
        add("float", "F");
        add("double", "D");
        add("string", "Ljava/lang/String;");
        add("boolean", "Z");
        add("shit", "[Ljava/lang/String;");
        add("void", "V");
    }

    private Map<Pair<Integer, Integer>, Pair<Integer, Operator>> add = new HashMap<>();
    public void putAddOp(int type0, int type1, int typeR, Operator op) { add.put(Pair.of(type0, type1), Pair.of(typeR, op)); }

    public interface Operator { void write(MethodWriter writer); }

    public Operator getAddOp(int type0, int type1) { return add.get(Pair.of(type0, type1)).getValue(); }
    public int getAddRType(int type0, int type1) { return add.get(Pair.of(type0, type1)).getKey(); }
    public Pair<Integer, Operator> getAddRTypeAndOp(int type0, int type1) { return add.get(Pair.of(type0, type1)); }
}
