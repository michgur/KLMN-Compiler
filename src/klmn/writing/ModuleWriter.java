package klmn.writing;

import jvm.Opcodes;
import klmn.nodes.ModuleNode;
import klmn.writing.types.TypeEnv;

public class ModuleWriter implements Opcodes
{
    private SymbolTable st = new SymbolTable();
    private TypeEnv te = new TypeEnv();

    private ModuleNode module;

    public ModuleWriter(ModuleNode module) {
        this.module = module;
        st.enterScope(SymbolTable.ScopeType.MODULE);
    }

    public SymbolTable getSymbolTable() { return st; }
    public TypeEnv getTypeEnv() { return te; }
    public ModuleNode getModule() { return module; }
}
