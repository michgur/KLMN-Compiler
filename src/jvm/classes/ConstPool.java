package jvm.classes;

import jvm.JVMType;
import jvm.Opcodes;
import util.ByteList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConstPool implements Opcodes
{

    public static final byte CONSTANT_Class                 = 7;
    public static final byte CONSTANT_Fieldref              = 9;
    public static final byte CONSTANT_Methodref             = 10;
    public static final byte CONSTANT_InterfaceMethodref    = 11;
    public static final byte CONSTANT_String	            = 8;
    public static final byte CONSTANT_Integer               = 3;
    public static final byte CONSTANT_Float                 = 4;
    public static final byte CONSTANT_Long                  = 5;
    public static final byte CONSTANT_Double                = 6;
    public static final byte CONSTANT_NameAndType           = 12;
    public static final byte CONSTANT_Utf8                  = 1;
    public static final byte CONSTANT_MethodHandle          = 15;
    public static final byte CONSTANT_MethodType	        = 16;
    public static final byte CONSTANT_InvokeDynamic         = 18;
    
    public static final byte REF_getField = 1;
    public static final byte REF_getStatic = 2;
    public static final byte REF_putField = 3;
    public static final byte REF_putStatic = 4;
    public static final byte REF_invokeVirtual = 5;
    public static final byte REF_invokeStatic = 6;
    public static final byte REF_invokeSpecial = 7;
    public static final byte REF_newInvokeSpecial = 8;
    public static final byte REF_invokeInterface = 9;
    
    private Set<ConstInfo> consts = new LinkedHashSet<>();
    private short count = 1;

    public int getCount() { return count; }
    public ByteList toByteList() {
        ByteList data = new ByteList();
        for (ConstInfo c : consts) {
            data.addByte(c.tag);
            data.addAll(c.info);
        }
        return data;
    }

    private short addConstInfo(byte tag, byte[] info) {
        ConstInfo c = new ConstInfo(-1, tag, info);
        if (consts.contains(c)) return (short) c.index;
        c.index = count;
        consts.add(c);
        return count++;
    }
    public short addClass(short nameIndex) {
        return addConstInfo(CONSTANT_Class, new byte[] {
                (byte)((nameIndex >> 8) & 0xFF), (byte) (nameIndex & 0xFF) });
    }
    public short addClass(String name) { return addClass(addUtf8(name)); }
    public short addClass(JVMType type) { return addClass(addUtf8(type.getDescriptor())); }
    public short addMethodref(short classIndex, short nameTypeIndex) {
        return addConstInfo(CONSTANT_Methodref, new byte[] {
            (byte)((classIndex >> 8) & 0xFF), (byte) (classIndex & 0xFF),
            (byte)((nameTypeIndex >> 8) & 0xFF), (byte) (nameTypeIndex & 0xFF) });
    }
    public short addMethodref(String className, String name, String descriptor)
    { return addMethodref(addClass(className), addNameAndType(name, descriptor)); }
    public short addMethodref(String className, String name, JVMType type)
    { return addMethodref(addClass(className), addNameAndType(name, type.getDescriptor())); }
    public short addMethodref(Method method) {
        return addMethodref(method.getDeclaringClass().getName().replace('.', '/'),
            method.getName(), JVMType.methodDescriptor(method));
    }
    public short addMethodref(Class cls, String method, Class ret, Class... params) {
        return addMethodref(cls.getName().replace('.', '/'),
                method, JVMType.methodDescriptor(ret, params));
    }
    public short addFieldref(short classIndex, short nameTypeIndex) {
        return addConstInfo(CONSTANT_Fieldref, new byte[] {
                (byte)((classIndex >> 8) & 0xFF), (byte) (classIndex & 0xFF),
                (byte)((nameTypeIndex >> 8) & 0xFF), (byte) (nameTypeIndex & 0xFF) });
    }
    public short addFieldref(String className, String name, String descriptor)
    { return addFieldref(addClass(className), addNameAndType(name, descriptor)); }
    public short addFieldref(String className, String name, JVMType type)
    { return addFieldref(addClass(className), addNameAndType(name, type.getDescriptor())); }
    public short addFieldref(Field field) {
        return addFieldref(field.getDeclaringClass().getName().replace('.', '/'),
                field.getName(), JVMType.typeDescriptor(field.getType()));
    }
    public short addFieldref(Class cls, String name, Class type) {
        return addFieldref(cls.getName().replace('.', '/'),
                name, JVMType.typeDescriptor(type));
    }
    public short addInterfaceMethodref(short classIndex, short nameTypeIndex) {
        return addConstInfo(CONSTANT_InterfaceMethodref, new byte[] {
                (byte)((classIndex >> 8) & 0xFF), (byte) (classIndex & 0xFF),
                (byte)((nameTypeIndex >> 8) & 0xFF), (byte) (nameTypeIndex & 0xFF) });
    }
    public short addString(short stringIndex) {
        return addConstInfo(CONSTANT_String, new byte[] {
                (byte)((stringIndex >> 8) & 0xFF), (byte) (stringIndex & 0xFF) });
    }
    public short addString(String string) { return addString(addUtf8(string)); } // not to be confused with addUtf8()
    public short addInteger(int i) {
        return addConstInfo(CONSTANT_Integer, new byte[] {
                (byte)((i >> 24) & 0xFF), (byte) ((i >> 16) & 0xFF),
                (byte)((i >> 8) & 0xFF), (byte) (i & 0xFF) });
    }
    public short addFloat(float f) {
        int i = Float.floatToIntBits(f);
        return addConstInfo(CONSTANT_Float, new byte[] {
                (byte)((i >> 24) & 0xFF), (byte) ((i >> 16) & 0xFF),
                (byte)((i >> 8) & 0xFF), (byte) (i & 0xFF) });
    }
    public short addLong(long l) {
        short i = addConstInfo(CONSTANT_Long, new byte[] {
                (byte)((l >> 56) & 0xFF), (byte) ((l >> 48) & 0xFF),
                (byte)((l >> 40) & 0xFF), (byte) ((l >> 32) & 0xFF),
                (byte)((l >> 24) & 0xFF), (byte) ((l >> 16) & 0xFF),
                (byte)((l >> 8) & 0xFF), (byte) (l & 0xFF) });
        count++;
        return i;
    }
    public short addDouble(double d) {
        long l = Double.doubleToLongBits(d);
        short i = addConstInfo(CONSTANT_Double, new byte[] {
                (byte)((l >> 56) & 0xFF), (byte) ((l >> 48) & 0xFF),
                (byte)((l >> 40) & 0xFF), (byte) ((l >> 32) & 0xFF),
                (byte)((l >> 24) & 0xFF), (byte) ((l >> 16) & 0xFF),
                (byte)((l >> 8) & 0xFF), (byte) (l & 0xFF) });
        count++;
        return i;
    }
    public short addNameAndType(short nameIndex, short descriptorIndex) {
        return addConstInfo(CONSTANT_NameAndType, new byte[] {
                (byte)((nameIndex >> 8) & 0xFF), (byte) (nameIndex & 0xFF),
                (byte)((descriptorIndex >> 8) & 0xFF), (byte) (descriptorIndex & 0xFF) });
    }
    public short addNameAndType(String name, String descriptor)
    { return addNameAndType(addUtf8(name), addUtf8(descriptor)); }
    public short addUtf8(String s) {
        byte[] bytes = s.getBytes();
        byte[] info = new byte[bytes.length + 2];
        System.arraycopy(bytes, 0, info, 2, bytes.length);
        info[0] = (byte) ((s.length() >> 8) & 0xFF);
        info[1] = (byte) (s.length() & 0xFF);
        return addConstInfo(CONSTANT_Utf8, info);
    }
    public short addMethodHandle(byte referenceType, short referenceIndex) {
        return addConstInfo(CONSTANT_MethodHandle, new byte[] {
                referenceType, (byte)((referenceIndex >> 8) & 0xFF), (byte) (referenceIndex & 0xFF) });
    }
    public short addMethodType(short descriptorIndex) {
        return addConstInfo(CONSTANT_MethodType, new byte[] {
                (byte)((descriptorIndex >> 8) & 0xFF), (byte) (descriptorIndex & 0xFF) });
    }
    public short addMethodType(String descriptor) { return addMethodType(addUtf8(descriptor)); }
    public short addInvokeDynamic(short bootstrapMethodAttrIndex, short nameTypeIndex) {
        return addConstInfo(CONSTANT_InvokeDynamic, new byte[] {
                (byte)((bootstrapMethodAttrIndex >> 8) & 0xFF), (byte) (bootstrapMethodAttrIndex & 0xFF),
                (byte)((nameTypeIndex >> 8) & 0xFF), (byte) (nameTypeIndex & 0xFF) });
    }

    private static class ConstInfo {
        private int index;
        private byte tag;
        private byte[] info;
        ConstInfo(int index, byte tag, byte[] info) {
            this.index = index;
            this.tag = tag;
            this.info = info;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (! (o instanceof ConstInfo)) return false;
            ConstInfo constInfo = (ConstInfo) o;
            if (tag != constInfo.tag || !Arrays.equals(info, constInfo.info)) return false;
            index = constInfo.index; // really bad, but who cares
            return true;
        }
        @Override
        public int hashCode() { return 31 * (int) tag + Arrays.hashCode(info); }
    }
}
