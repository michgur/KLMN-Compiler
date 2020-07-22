package jvm.methods;

import jvm.AttributeInfo;
import jvm.JVMType;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import util.ByteList;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo implements Opcodes
{
    private short accFlags;
    private short nameIndex, descriptorIndex;
    private List<AttributeInfo> attributes = new ArrayList<>();
    private JVMType[] params;

    public MethodInfo(ClassFile cls, String name, int accFlags, JVMType type, JVMType... params) {
        this.accFlags = (short) accFlags;
        this.params = params;

        nameIndex = cls.getConstPool().addUtf8(name);
        descriptorIndex = cls.getConstPool().addUtf8(JVMType.methodDescriptor(type, params));

        addAttribute(new Code(cls, this));

        cls.getMethods().add(this);
    }

    public Code getCode() { return (Code) attributes.get(0); }

    public ByteList toByteList() {
        ByteList data = new ByteList();
        data.addShort(accFlags);                // access_flags
        data.addShort(nameIndex);               // name_index
        data.addShort(descriptorIndex);         // descriptor_index
        data.addShort(attributes.size());       // attributes_count
        for (AttributeInfo attrib : attributes)
            data.addAll(attrib.toByteList());   // attributes[attributes_count]
        return data;
    }

    public void addAttribute(AttributeInfo attribute) { attributes.add(attribute); }

    public JVMType[] getParams() { return params; }
}
