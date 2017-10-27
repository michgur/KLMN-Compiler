package jvm;

import jvm.classes.ClassFile;
import util.ByteList;

public class AttributeInfo
{
    private short nameIndex;
    protected ByteList info = new ByteList();
    protected ClassFile cls;

    public AttributeInfo(ClassFile cls, String name) {
        this.cls = cls;
        nameIndex = cls.getConstPool().addUtf8(name);
    }

    public ByteList getInfo() { return info; }

    public ByteList toByteList() {
        ByteList data = new ByteList();
        data.addShort(nameIndex);   // attribute_name_index
        data.addInt(info.size());   // attribute_length
        data.addAll(info);          // info[attribute_length]
        return data;
    }
}
