package klmn.nodes;

import ast.AST;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import jvm.methods.MethodInfo;
import klmn.writing.MethodWriter;
import klmn.writing.ModuleWriter;
import lang.Token;

import java.util.List;

public class MethodNode extends ModuleNode.BodyNode implements Opcodes
{
    public MethodNode(Token name, AST modifiers, AST type, AST params, AST body) { super(name, modifiers, type, params, body); }

    @Override public void write(ModuleWriter writer) {
        int acc = ACC_STATIC;
        for (AST m : getChild(0).getChildren())
            switch (m.getValue().getValue()) {
                case "static": throw new RuntimeException("invalid modifier 'static' for module function");
                case "final": throw new RuntimeException("invalid modifier 'final' for module function");
                case "public":
                    if ((acc & ACC_PRIVATE) != 0)
                        throw new RuntimeException("conflicting function modifiers 'private' and 'public'");
                    acc += ACC_PUBLIC;
                    break;
                case "private":
                    if ((acc & ACC_PUBLIC) != 0)
                        throw new RuntimeException("conflicting function modifiers 'public' and 'private'");
                    acc += ACC_PRIVATE;
                    break;
            }
        AST p = getChild(2);
        String[] params = new String[p.getChildren().size()];
        String[] pNames = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = ((TypeNode) p.getChild(i).getChild(0)).getJVM(writer);
            pNames[i] = p.getChild(i).getValue().getValue();
        }
        String moduleName = writer.getModule().getValue().getValue();

        String type = ((TypeNode) getChild(1)).getJVM(writer);
        ClassFile cf = writer.getModule().getClassFile();
        MethodWriter mw = new MethodWriter(moduleName, writer, true, cf.getConstPool(),
                new MethodInfo(cf, getValue().getValue(), acc, type, params), pNames);
        ((StmtNode) getChild(3)).write(mw);
//        mw.ret();
    }

    @Override
    public int getType(ModuleWriter writer) { return ((TypeNode) getChild(1)).getID(writer); }

    public static abstract class BodyNode extends AST {
        public BodyNode(Token value, AST... children) { super(value, children); }
        public BodyNode(Token value, List<AST> children) { super(value, children); }
        public abstract void write(MethodWriter writer);
    }
}
