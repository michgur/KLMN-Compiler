package klmn.nodes;

import ast.AST;
import jvm.Opcodes;
import jvm.classes.FieldInfo;
import klmn.TypeException;
import klmn.writing.MethodWriter;
import klmn.writing.ModuleWriter;
import klmn.writing.SymbolTable;
import klmn.writing.TypeEnv;
import lang.Token;

public class VarNode extends StmtNode implements ModuleNode.BodyNode, Opcodes
{
    private boolean init, constant = false;
    private TypeEnv.Type type;

    public VarNode(Token name, AST modifiers, AST type) { this(name, false, modifiers, type); }
    public VarNode(Token name, AST modifiers, AST type, AST value) { this(name, true, modifiers, type, value); }
    private VarNode(Token name, boolean init, AST... c) {
        super(name, c);
        this.init = init;
        for (AST ast : c[0].getChildren())
            if (ast.getValue().getValue().equals("final")) constant = true;
    }

    @Override
    public void write(ModuleWriter writer) {
        String name = getValue().getValue(),
                type = ((TypeNode) getChild(1)).get(writer).getDescriptor();

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
    public void write(MethodWriter writer) {
        String name = getValue().getValue();
        TypeEnv.Type type = ((TypeNode) getChild(1)).get(writer);
        typeCheck(writer);

        writer.getSymbolTable().addSymbol(name, type);
        if (init) ((ExpNode) getChild(2)).write(writer);
        else switch (type.getDescriptor()) {
            case "I": writer.pushInt(0); break;
            case "F": writer.pushFloat(0); break;
            default: writer.pushNull();
        }
        writer.popToLocal(name);

        for (AST m : getChild(0).getChildren())
            switch (m.getValue().getValue()) {
                case "final": break; // currently not used
                case "static":
                case "public":
                case "private":
                    throw new RuntimeException("invalid modifier '" + m.getValue().getValue() + "' for local variable");
            }
    }

    @Override
    public TypeEnv.Type getType(ModuleWriter writer) { return ((TypeNode) getChild(1)).get(writer); }

    // fixme currently only works for local vars since MethodWriters & ModuleWriters don't have common parent
    private void typeCheck(MethodWriter writer) {
        if (getChildren().size() < 3) return;
        if (!((TypeNode) getChild(1)).get(writer).equals(((ExpNode) getChild(2)).getType(writer)))
            throw new TypeException();
    }
}
