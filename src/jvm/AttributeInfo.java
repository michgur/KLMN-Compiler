package jvm;

import jvm.classes.ClassFile;
import util.ByteList;

public class AttributeInfo
{
    private short nameIndex;
    protected ByteList info = new ByteList();
    protected ClassFile cls;
    private boolean converted = false;
    private ByteList cnvrtd;

    public AttributeInfo(ClassFile cls, String name) {
        this.cls = cls;
        nameIndex = cls.getConstPool().addUtf8(name);
    }

    public ByteList getInfo() { return info; }

    public ByteList toByteList() {
        if (converted) return cnvrtd;//throw new RuntimeException("can only convert AttributeInfo to ByteList once!");
        converted = true;
        ByteList data = new ByteList();
        data.addShort(nameIndex);   // attribute_name_index
        data.addInt(info.size());   // attribute_length
        data.addAll(info);          // info[attribute_length]
        cnvrtd = data;
        return data;
    }
}
