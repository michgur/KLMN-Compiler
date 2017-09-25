package jvm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/20/2017.
 */
public class ClassFile
{
    public static final short ACC_PUBLIC = 0x0001;
    public static final short ACC_PRIVATE = 0x0002;
    public static final short ACC_PROTECTED = 0x0004;
    public static final short ACC_STATIC = 0x0008;
    public static final short ACC_FINAL = 0x0010;
    public static final short ACC_SUPER = 0x0020;
    public static final short ACC_SYNCHRONIZED = 0x0020;
    public static final short ACC_BRIDGE = 0x0040;
    public static final short ACC_VOLATILE = 0x0040;
    public static final short ACC_VARARGS = 0x0080;
    public static final short ACC_TRANSIENT = 0x0080;
    public static final short ACC_NATIVE = 0x0100;
    public static final short ACC_INTERFACE = 0x0200;
    public static final short ACC_ABSTRACT = 0x0400;
    public static final short ACC_STRICT = 0x0400;
    public static final short ACC_SYNTHETIC = 0x1000;
    public static final short ACC_ANNOTATION = 0x2000;
    public static final short ACC_ENUM = 0x4000;

    public static final byte CONSTANT_Utf8 = 1;
    public static final byte CONSTANT_Integer = 3;
    public static final byte CONSTANT_Float = 4;
    public static final byte CONSTANT_Long = 5;
    public static final byte CONSTANT_Double = 6;
    public static final byte CONSTANT_Class = 7;
    public static final byte CONSTANT_String = 8;
    public static final byte CONSTANT_Fieldref = 9;
    public static final byte CONSTANT_Methodref = 10;
    public static final byte CONSTANT_InterfaceMethodref = 11;
    public static final byte CONSTANT_NameAndType = 12;
    public static final byte CONSTANT_MethodHandle = 15;
    public static final byte CONSTANT_MethodType = 16;
    public static final byte CONSTANT_InvokeDynamic = 18;

    public static final String TYPE_VOID = "V";
    public static final String TYPE_BYTE = "B";
    public static final String TYPE_CHAR = "A";
    public static final String TYPE_DOUBLE = "D";
    public static final String TYPE_FLOAT = "F";
    public static final String TYPE_INT = "I";
    public static final String TYPE_LONG = "J";
    public static final String TYPE_SHORT = "S";
    public static final String TYPE_BOOLEAN = "Z";
    public static String objectType(String name) { return "L" + name.replace('.', '/') + ";"; }
    public static String objectType(Class c) { return "L" + c.getName().replace('.', '/') + ";"; }
    public static String arrayType(String type, int dim) { return String.join("", Collections.nCopies(dim, "[")) + type; }
    public static String arrayType(String type) { return arrayType(type, 1); }
    public static String methodDesc(String type, String... params) { return String.format("(%s)%s", String.join("", params), type);  }

    private static final int MAGIC = 0xCAFEBABE;
    private static final short V_MINOR = 0x0000;
    private static final short V_MAJOR = 0x0034; // Java SE 8

    private String name;
    private short cpSize = 1;
    private ByteList constPool = new ByteList();
    private short accFlags = ACC_PUBLIC;
    private short thisRef, superRef;
    private short interfaceCount = 0, fieldCount = 0, methodCount = 0, attribCount = 0;
    private ByteList interfaces = new ByteList(),
    fields = new ByteList(), methods = new ByteList(), attribs = new ByteList();

    public ClassFile(String name) {
        this.name = name;
        thisRef = addClassConst(addStringConst(name));
        superRef = addClassConst(addStringConst("java/lang/Object"));
    }

    public void write() {
        ByteList bytes = new ByteList();
        bytes.addInt(MAGIC)
            .addShort(V_MINOR)
            .addShort(V_MAJOR)
            .addShort(cpSize)
            .addBytes(constPool)
            .addShort(accFlags)
            .addShort(thisRef)
            .addShort(superRef)
            .addShort(interfaceCount)
            .addBytes(interfaces)
            .addShort(fieldCount)
            .addBytes(fields)
            .addShort(methodCount)
            .addBytes(methods)
            .addShort(attribCount)
            .addBytes(attribs);
        byte[] b = new byte[bytes.size()];
        for (int i = 0; i < b.length; i++) b[i] = bytes.get(i);
        try { Files.write(Paths.get(name + ".class"), b); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public short addConst(byte cType, String s) {
        constPool.addByte(cType).addShort((short) s.length());
        constPool.addBytes(s.getBytes());
        return cpSize++;
    }
    public short addConst(byte cType, short s) {
        constPool.addByte(cType).addShort(s);
        return cpSize++;
    }
    public short addConst(byte cType, int i) {
        constPool.addByte(cType).addInt(i);
        return cpSize++;
    }
    public short addConst(byte cType, long l) {
        constPool.addByte(cType).addLong(l);
        return (short) ((cpSize += 2) - 1);
    }
    public short addConst(byte cType, float f) {
        constPool.addByte(cType).addInt(Float.floatToIntBits(f));
        return cpSize++;
    }
    public short addConst(byte cType, double d) { // 😏😏😏
        constPool.addByte(cType).addLong(Double.doubleToLongBits(d));
        return (short) ((cpSize += 2) - 1);
    }
    public short addConst(byte cType, short s0, short s1) {
        constPool.addByte(cType).addShort(s0).addShort(s1);
        return cpSize++;
    }
    public short addConst(byte cType, byte b, short s) {
        constPool.addByte(cType).addByte(b).addShort(s);
        return cpSize++;
    }

    public short addStringConst(String s) { return addConst(CONSTANT_Utf8, s); }
    public short addIntConst(int i) { return addConst(CONSTANT_Integer, i); }
    public short addFloatConst(float f) { return addConst(CONSTANT_Float, f); }
    public short addLongConst(long l) { return addConst(CONSTANT_Long, l); }
    public short addDoubleConst(double d)  { return addConst(CONSTANT_Double, d); }
    public short addClassConst(short i) { return addConst(CONSTANT_Class, i); }
    public short addStringRefConst(short i) { return addConst(CONSTANT_String, i); }
    public short addFieldRefConst(short classRef, short nameTypeRef) { return addConst(CONSTANT_Fieldref, classRef, nameTypeRef); }
    public short addMethodRefConst(short classRef, short nameTypeRef) { return addConst(CONSTANT_Methodref, classRef, nameTypeRef); }
    public short addInterfaceMethodRefConst(short classRef, short nameTypeRef) { return addConst(CONSTANT_InterfaceMethodref, classRef, nameTypeRef); }
    public short addNameTypeConst(short nameRef, short typeRef) { return addConst(CONSTANT_NameAndType, nameRef, typeRef); }
    public short addMethodHandleConst(byte type, short i) { return addConst(CONSTANT_MethodHandle, type, i); }
    public short addMethodTypeConst(short i) { return addConst(CONSTANT_MethodType, i); }
//    public int addInvokeDynamicConst() TODO

    public void addField(int accFlags, String name, String desc) { addField(accFlags, addStringConst(name), addStringConst(desc)); }
    public void addField(int accFlags, short nameRef, short descRef) {
        fieldCount++;
        fields.addShort(accFlags).addShort(nameRef).addShort(descRef);
    }

    public void addMethod(int accFlags, String name, String desc) { addMethod(accFlags, name, desc, null);}
    public void addMethod(int accFlags, String name, String desc, ByteList body) { addMethod(accFlags, addStringConst(name), addStringConst(desc), body); }
    public void addMethod(int accFlags, short nameRef, short descRef, ByteList body) {
        methodCount++;
        methods.addShort(accFlags).addShort(nameRef).addShort(descRef);
        if (body == null || body.size() == 0) {
            methods.addShort(0);
            return;
        } methods.addShort(1);
        methods.addShort(addStringConst("Code")); // todo: replace addConst methods with getConst methods, that keep track of existing constants
        methods.addInt(12 + body.size());
        methods.addShort(256); // todo: figure out stack size calculations
        methods.addShort(100); // todo: figure out amount of required local variables
        methods.addInt(body.size());
        methods.addBytes(body);
        methods.addShort(0); // todo: support exceptions
        methods.addShort(0); // todo: add support for attributes
    }
}
