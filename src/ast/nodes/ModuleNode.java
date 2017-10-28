package ast.nodes;

import ast.AST;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import jvm.methods.MethodInfo;
import lang.Token;
import test.MethodWriter;

public class ModuleNode extends AST implements Opcodes
{
    public ModuleNode(String name, AST body) { super(new Token(name), body); }

    public void run() {
        String name = getValue().getValue();
        ClassFile cf = new ClassFile(name);
        MethodInfo main = new MethodInfo(cf, "main", ACC_PUBLIC | ACC_STATIC, "V", "[Ljava/lang/String;");
        ((StmtNode) getChild(0)).write(new MethodWriter(cf.getConstPool(), main));
        cf.addMethod(main);

        try { cf.run(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
