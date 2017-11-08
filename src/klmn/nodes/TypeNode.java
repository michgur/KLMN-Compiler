package klmn.nodes;

import ast.AST;
import klmn.writing.ModuleWriter;
import lang.Token;

public class TypeNode extends AST
{
    public TypeNode(Token value) { super(value); } // simple type

    public int getID(ModuleWriter writer) { return writer.getTypeEnvironment().typeID(getValue().getValue()); }
    public String getJVM(ModuleWriter writer) { return writer.getTypeEnvironment().jvmType(getValue().getValue()); }
}
