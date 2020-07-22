package jvm.classes;

import jvm.JVMType;
import jvm.Opcodes;
import util.ByteList;

public class FieldInfo implements Opcodes
{
    private short accFlags, name, type;
    public FieldInfo(ClassFile cls, String name, int accFlags, JVMType type) {
        this.accFlags = (short) accFlags;
        this.name = cls.getConstPool().addUtf8(name);
        this.type = cls.getConstPool().addUtf8(type.getDescriptor());
    }

    public ByteList toByteList() {
        ByteList bytes = new ByteList();
        bytes.addShort(accFlags);
        bytes.addShort(name);
        bytes.addShort(type);
        bytes.addShort(0x0000);
        return bytes;
    }
}
