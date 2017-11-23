package jvm.classes;

import jvm.AttributeInfo;
import util.ByteList;

import java.util.HashSet;
import java.util.Set;

public class InnerClasses extends AttributeInfo
{
    private Set<InnerClass> classes = new HashSet<>();
    private ConstPool constPool;
    private String className;
    public InnerClasses(ClassFile cls) {
        super(cls, "InnerClasses");
        className = cls.getName();
        constPool = cls.getConstPool();
    }

    public void add(String name, int acc) {
        classes.add(new InnerClass(constPool.addClass(className + "$" + name),
                constPool.addClass(className), constPool.addUtf8(name), (short) acc));
    }

    @Override
    public boolean include() { return !classes.isEmpty(); }

    @Override
    public ByteList toByteList() {
        info.addShort(classes.size());
        for (InnerClass cls : classes) {
            info.addShort(cls.index);
            info.addShort(cls.outer);
            info.addShort(cls.name);
            info.addShort(cls.acc);
        }
        return super.toByteList();
    }

    private static class InnerClass {
        private short index, outer, name, acc;
        public InnerClass(short index, short outer, short name, short acc) {
            this.index = index;
            this.outer = outer;
            this.name = name;
            this.acc = acc;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InnerClass that = (InnerClass) o;
            return index == that.index && outer == that.outer && name == that.name && acc == that.acc;
        }
        @Override
        public int hashCode()
        { return 31 * (31 * (31 * (int) index + (int) outer) + (int) name) + (int) acc; }
    }
}
