package jvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/20/2017.
 */
public class ByteList extends ArrayList<Byte>
{
    public ByteList addByte(int o) {
        add((byte)o);
        return this;
    }
    public ByteList addBytes(byte[] a) {
        for (byte b : a) add(b);
        return this;
    }
    public ByteList addBytes(Byte[] a) {
        addAll(Arrays.asList(a));
        return this;
    }
    public ByteList addBytes(Collection<? extends Byte> c) {
        addAll(c);
        return this;
    }
    public ByteList addShort(int o) {
        add((byte)((short)o >> 8 & 0xFF));
        add((byte)((short)o & 0xFF));
        return this;
    }
    public ByteList addInt(int o) {
        add((byte)(o >> 24 & 0xFF));
        add((byte)(o >> 16 & 0xFF));
        add((byte)(o >> 8 & 0xFF));
        add((byte)(o & 0xFF));
        return this;
    }
    public ByteList addLong(long o) {
        long l = (long) o;
        for (int i = 8 * 7; i >= 0; i -= 8) add((byte) ((l >> i) & 0xFF));
        return this;
    }
}
