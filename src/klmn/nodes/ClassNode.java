package klmn.nodes;

import ast.AST;
import jvm.classes.ClassFile;
import klmn.writing.ModuleWriter;
import klmn.writing.types.Type;
import lang.Token;

public class ClassNode extends ModuleNode implements ModuleNode.BodyNode
{
    private Type type;
    public ClassNode(String name, AST modifiers, ModuleNode body) {
        super(name);
    }

    @Override
    public void write(ModuleWriter writer) {
        getType(writer);

    }

    @Override
    public Type getType(ModuleWriter writer)
    { return type == null ? (type = writer.getTypeEnv().add(getValue().getValue(), "L" + getValue().getValue() + ";")) : type; }
}
