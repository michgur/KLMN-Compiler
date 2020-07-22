package codegen;

import jvm.JVMType;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/17/2017.
 */
public class SymbolTable
{
    private final Deque<Scope> table = new ArrayDeque<>();
    private int index = 0; // args[] is at index 0

    public void resetIndex() { index = 0; }

    public void addSymbol(String cls, String field, JVMType type, boolean isStatic) {
        for (Scope scope : table)
            if (scope.context == Context.CLASS) {
                scope.symbols.put(field, new SymbolInfo(cls, field, type, isStatic, Context.CLASS, null));
                return;
            }
        throw new RuntimeException("No valid context for defining field " + cls + "." + field + "!");
    }
    public void addSymbol(String local, JVMType type) {
        if (getContext() == Context.CLASS) throw new RuntimeException("FieldInfo required when defining variables in class!");
        table.peek().symbols.put(local, new SymbolInfo(null, local, type, null, getContext(), index++));
    }
    public SymbolInfo findSymbol(String symbol) {
        for (Scope scope : table)
            if (scope.symbols.containsKey(symbol)) return scope.symbols.get(symbol);
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public JVMType typeOf(String symbol) {
        for (Scope scope : table)
            if (scope.symbols.containsKey(symbol)) return scope.symbols.get(symbol).getType();
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public Context contextOf(String symbol) {
        for (Scope scope : table)
            if (scope.symbols.containsKey(symbol)) return scope.context;
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public boolean isDefinedLocally(String symbol) { return table.peek().symbols.containsKey(symbol); }

    public void addType(JVMType type) { table.peek().types.add(type); }

    public void enterScope(Context context) { table.push(new Scope(context)); }
    public void exitScope() { table.pop().symbols.size(); }
    public Context getContext() { return table.peek().context; }

    public enum Context { CLASS, BLOCK, CONDITION }

    public static class SymbolInfo
    {
        private final String cls;
        private final String name;
        private final JVMType type;
        private final Boolean isStatic;
        private final Context context;
        private final Integer index;

        private SymbolInfo(String cls, String name, JVMType type, Boolean isStatic, Context context, Integer index) {
            this.cls = cls;
            this.name = name;
            this.type = type;
            this.isStatic = isStatic;
            this.context = context;
            this.index = index;
        }

        public String getName() { return name; }
        public JVMType getType() { return type; }
        public Context getContext() { return context; }

        public boolean isLocal() { return context != Context.CLASS; }
        public boolean isField() { return context == Context.CLASS; }

        public int getLocalIndex() { return index; }

        public String getDeclaringClassName() { return cls; }
        public boolean isStaticField() { return isStatic; }
    }

    private static class Scope
    {
        private final Context context;
        private final Map<String, SymbolInfo> symbols = new HashMap<>();
        private final Set<JVMType> types = new HashSet<>();

        private Scope(Context context) { this.context = context; }
    }
}
