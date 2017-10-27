package util;

import java.util.ArrayList;

public class ByteList extends ArrayList<Byte>
{
    private void add(int b) { super.add((byte) b); }
    private void add(long b) { super.add((byte) b); }
    public void addAll(byte[] bytes) { for (byte b : bytes) super.add(b); }
    public void addByte(int b) { addByte((byte) b); }
    public void addByte(byte b) { add(b); }
    public void addShort(int s) { addShort((short) s); }
    public void addShort(short s) {
        add((s >> 8) & 0xFF);
        add(s & 0xFF);
    }
    public void addInt(int i) {
        add((i >> 24) & 0xFF);
        add((i >> 16) & 0xFF);
        add((i >> 8) & 0xFF);
        add(i & 0xFF);
    }
    public void addLong(long l) {
        add((l >> 56) & 0xFF);
        add((l >> 48) & 0xFF);
        add((l >> 40) & 0xFF);
        add((l >> 32) & 0xFF);
        add((l >> 24) & 0xFF);
        add((l >> 16) & 0xFF);
        add((l >> 8) & 0xFF);
        add(l & 0xFF);
    }
    public void addFloat(float f) { addInt(Float.floatToIntBits(f)); }
    public void addDouble(double d) { addLong(Double.doubleToLongBits(d)); }

    public byte[] toByteArray() {
        byte bytes[] = new byte[size()];
        for (int i = 0; i < bytes.length; i++) bytes[i] = get(i);
        return bytes;
    }
}
