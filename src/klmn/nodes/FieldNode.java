package klmn.nodes;

import ast.AST;
import jvm.Opcodes;
import jvm.classes.FieldInfo;
import klmn.writing.MethodWriter;
import klmn.writing.ModuleWriter;
import lang.Token;

public class FieldNode extends ModuleNode.BodyNode implements Opcodes
{
    private boolean init;
    public FieldNode(Token name, AST modifiers, AST type) {
        super(name, modifiers, type);
        init = false;
    }
    public FieldNode(Token name, AST modifiers, AST type, AST value) {
        super(name, modifiers, type, value);
        init = true;
    }

    @Override
    public void write(ModuleWriter writer) {
        String name = getValue().getValue(),
                type = ((TypeNode) getChild(1)).getJVM(writer);

        if (init) {
            MethodWriter initializer = writer.getModule().getInitializer();
            ((ExpNode) getChild(2)).write(initializer);
            initializer.popToStaticField(writer.getModule().getValue().getValue(), name, type);
        }

        int acc = ACC_STATIC;
        for (AST m : getChild(0).getChildren())
            switch (m.getValue().getValue()) {
                case "static": throw new RuntimeException("invalid modifier 'static' for module variable");
                case "final": acc += ACC_FINAL; break;
                case "public":
                    if ((acc & ACC_PRIVATE) != 0)
                        throw new RuntimeException("conflicting variable modifiers 'private' and 'public'");
                    acc += ACC_PUBLIC;
                    break;
                case "private":
                    if ((acc & ACC_PUBLIC) != 0)
                        throw new RuntimeException("conflicting variable modifiers 'public' and 'private'");
                    acc += ACC_PRIVATE;
                    break;
            }
        writer.getModule().getClassFile().getFields().add(new FieldInfo(writer.getModule().getClassFile(), name, acc, type));
    }

    @Override
    public int getType(ModuleWriter writer) { return ((TypeNode) getChild(1)).getID(writer); }
}
