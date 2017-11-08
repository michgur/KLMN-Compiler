package klmn.nodes;

import ast.AST;
import jvm.classes.ClassFile;
import lang.Token;

public class ClassNode extends AST
{
    private ClassFile cf;
    public ClassNode(String name, AST body) {
        super(new Token(name), body);

        ClassFile cls = new ClassFile(name);


        try { cls.run(); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
