package jvm;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/25/2017.
 */
public class ClassCode
{
    private ByteList code = new ByteList();

    public void add(byte opcode) { code.addByte(opcode); }
    public void add(byte opcode, byte o0) { code.addByte(opcode).addByte(o0); }
    public void add(byte opcode, short o0) { code.addByte(opcode).addShort(o0); }

    public ByteList getRawCode() { return code; }
}
