package klmn.nodes;

import ast.AST;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import jvm.methods.MethodInfo;
import klmn.writing.ModuleWriter;
import lang.Token;
import klmn.writing.MethodWriter;

import java.util.List;

public class ModuleNode extends AST implements Opcodes
{
    private ClassFile classFile;
    private MethodWriter initializer;
    private ModuleWriter writer = new ModuleWriter(this);
    public ModuleNode(String name) {
        super(new Token(name));

        classFile = new ClassFile(name);
        MethodInfo init = new MethodInfo(classFile, "<clinit>", ACC_STATIC, "V");
        initializer = new MethodWriter(name, writer, false, classFile.getConstPool(), init);
    }

    public void run() {
        // order of operations: first, save all symbols, then write methods, then compute vars in order
        for (AST c : getChildren()) // first pass, write symbols
            writer.getSymbolTable().addSymbol(c.getValue().getValue(), ((BodyNode) c).getType(writer));
        for (AST c : getChildren()) ((BodyNode) c).write(writer);

        initializer.ret();
        try { classFile.run(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public ClassFile getClassFile() { return classFile; }
    public MethodWriter getInitializer() { return initializer; }

    public static abstract class BodyNode extends AST {
        public BodyNode(Token value, AST... children) { super(value, children); }
        public BodyNode(Token value, List<AST> children) { super(value, children); }
        public abstract void write(ModuleWriter writer);
        public abstract int getType(ModuleWriter writer);
//        public abstract void registerSymbol(ModuleWriter writer);
    }
}
