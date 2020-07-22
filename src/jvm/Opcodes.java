package jvm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Opcodes
{
    byte NOP = (byte) 0; 
    byte ACONST_NULL = (byte) 1; 
    byte ICONST_M1 = (byte) 2; 
    byte ICONST_0 = (byte) 3; 
    byte ICONST_1 = (byte) 4; 
    byte ICONST_2 = (byte) 5; 
    byte ICONST_3 = (byte) 6; 
    byte ICONST_4 = (byte) 7; 
    byte ICONST_5 = (byte) 8; 
    byte LCONST_0 = (byte) 9; 
    byte LCONST_1 = (byte) 10; 
    byte FCONST_0 = (byte) 11; 
    byte FCONST_1 = (byte) 12; 
    byte FCONST_2 = (byte) 13; 
    byte DCONST_0 = (byte) 14; 
    byte DCONST_1 = (byte) 15; 
    byte BIPUSH = (byte) 16;
    byte SIPUSH = (byte) 17; 
    byte LDC = (byte) 18; 
    byte LDC_W = (byte) 19; 
    byte LDC2_W = (byte) 20; 
    byte ILOAD = (byte) 21; 
    byte LLOAD = (byte) 22; 
    byte FLOAD = (byte) 23; 
    byte DLOAD = (byte) 24; 
    byte ALOAD = (byte) 25; 
    byte ILOAD_0 = (byte) 26; 
    byte ILOAD_1 = (byte) 27; 
    byte ILOAD_2 = (byte) 28; 
    byte ILOAD_3 = (byte) 29; 
    byte LLOAD_0 = (byte) 30; 
    byte LLOAD_1 = (byte) 31; 
    byte LLOAD_2 = (byte) 32; 
    byte LLOAD_3 = (byte) 33; 
    byte FLOAD_0 = (byte) 34; 
    byte FLOAD_1 = (byte) 35; 
    byte FLOAD_2 = (byte) 36; 
    byte FLOAD_3 = (byte) 37; 
    byte DLOAD_0 = (byte) 38; 
    byte DLOAD_1 = (byte) 39; 
    byte DLOAD_2 = (byte) 40; 
    byte DLOAD_3 = (byte) 41; 
    byte ALOAD_0 = (byte) 42; 
    byte ALOAD_1 = (byte) 43; 
    byte ALOAD_2 = (byte) 44; 
    byte ALOAD_3 = (byte) 45; 
    byte IALOAD = (byte) 46; 
    byte LALOAD = (byte) 47; 
    byte FALOAD = (byte) 48; 
    byte DALOAD = (byte) 49; 
    byte AALOAD = (byte) 50; 
    byte BALOAD = (byte) 51; 
    byte CALOAD = (byte) 52; 
    byte SALOAD = (byte) 53; 
    byte ISTORE = (byte) 54; 
    byte LSTORE = (byte) 55; 
    byte FSTORE = (byte) 56; 
    byte DSTORE = (byte) 57; 
    byte ASTORE = (byte) 58; 
    byte ISTORE_0 = (byte) 59; 
    byte ISTORE_1 = (byte) 60; 
    byte ISTORE_2 = (byte) 61; 
    byte ISTORE_3 = (byte) 62; 
    byte LSTORE_0 = (byte) 63; 
    byte LSTORE_1 = (byte) 64; 
    byte LSTORE_2 = (byte) 65; 
    byte LSTORE_3 = (byte) 66; 
    byte FSTORE_0 = (byte) 67; 
    byte FSTORE_1 = (byte) 68; 
    byte FSTORE_2 = (byte) 69; 
    byte FSTORE_3 = (byte) 70; 
    byte DSTORE_0 = (byte) 71; 
    byte DSTORE_1 = (byte) 72; 
    byte DSTORE_2 = (byte) 73; 
    byte DSTORE_3 = (byte) 74; 
    byte ASTORE_0 = (byte) 75; 
    byte ASTORE_1 = (byte) 76; 
    byte ASTORE_2 = (byte) 77; 
    byte ASTORE_3 = (byte) 78; 
    byte IASTORE = (byte) 79; 
    byte LASTORE = (byte) 80; 
    byte FASTORE = (byte) 81; 
    byte DASTORE = (byte) 82; 
    byte AASTORE = (byte) 83; 
    byte BASTORE = (byte) 84; 
    byte CASTORE = (byte) 85; 
    byte SASTORE = (byte) 86; 
    byte POP = (byte) 87; 
    byte POP2 = (byte) 88; 
    byte DUP = (byte) 89; 
    byte DUP_X1 = (byte) 90; 
    byte DUP_X2 = (byte) 91; 
    byte DUP2 = (byte) 92; 
    byte DUP2_X1 = (byte) 93; 
    byte DUP2_X2 = (byte) 94; 
    byte SWAP = (byte) 95; 
    byte IADD = (byte) 96; 
    byte LADD = (byte) 97; 
    byte FADD = (byte) 98; 
    byte DADD = (byte) 99; 
    byte ISUB = (byte) 100; 
    byte LSUB = (byte) 101; 
    byte FSUB = (byte) 102; 
    byte DSUB = (byte) 103; 
    byte IMUL = (byte) 104; 
    byte LMUL = (byte) 105; 
    byte FMUL = (byte) 106; 
    byte DMUL = (byte) 107; 
    byte IDIV = (byte) 108; 
    byte LDIV = (byte) 109; 
    byte FDIV = (byte) 110; 
    byte DDIV = (byte) 111; 
    byte IREM = (byte) 112; 
    byte LREM = (byte) 113; 
    byte FREM = (byte) 114; 
    byte DREM = (byte) 115; 
    byte INEG = (byte) 116; 
    byte LNEG = (byte) 117; 
    byte FNEG = (byte) 118; 
    byte DNEG = (byte) 119; 
    byte ISHL = (byte) 120; 
    byte LSHL = (byte) 121; 
    byte ISHR = (byte) 122; 
    byte LSHR = (byte) 123; 
    byte IUSHR = (byte) 124; 
    byte LUSHR = (byte) 125; 
    byte IAND = (byte) 126; 
    byte LAND = (byte) 127; 
    byte IOR = (byte) 128; 
    byte LOR = (byte) 129; 
    byte IXOR = (byte) 130; 
    byte LXOR = (byte) 131; 
    byte IINC = (byte) 132; 
    byte I2L = (byte) 133; 
    byte I2F = (byte) 134; 
    byte I2D = (byte) 135; 
    byte L2I = (byte) 136; 
    byte L2F = (byte) 137; 
    byte L2D = (byte) 138; 
    byte F2I = (byte) 139; 
    byte F2L = (byte) 140; 
    byte F2D = (byte) 141; 
    byte D2I = (byte) 142; 
    byte D2L = (byte) 143; 
    byte D2F = (byte) 144; 
    byte I2B = (byte) 145; 
    byte I2C = (byte) 146; 
    byte I2S = (byte) 147; 
    byte LCMP = (byte) 148; 
    byte FCMPL = (byte) 149; 
    byte FCMPG = (byte) 150; 
    byte DCMPL = (byte) 151; 
    byte DCMPG = (byte) 152; 
    byte IFEQ = (byte) 153; 
    byte IFNE = (byte) 154; 
    byte IFLT = (byte) 155; 
    byte IFGE = (byte) 156; 
    byte IFGT = (byte) 157; 
    byte IFLE = (byte) 158; 
    byte IF_ICMPEQ = (byte) 159; 
    byte IF_ICMPNE = (byte) 160; 
    byte IF_ICMPLT = (byte) 161; 
    byte IF_ICMPGE = (byte) 162; 
    byte IF_ICMPGT = (byte) 163; 
    byte IF_ICMPLE = (byte) 164; 
    byte IF_ACMPEQ = (byte) 165; 
    byte IF_ACMPNE = (byte) 166; 
    byte GOTO = (byte) 167; 
    byte JSR = (byte) 168; 
    byte RET = (byte) 169; 
    byte TABLESWITCH = (byte) 170; 
    byte LOOKUPSWITCH = (byte) 171; 
    byte IRETURN = (byte) 172; 
    byte LRETURN = (byte) 173; 
    byte FRETURN = (byte) 174; 
    byte DRETURN = (byte) 175; 
    byte ARETURN = (byte) 176; 
    byte RETURN = (byte) 177; 
    byte GETSTATIC = (byte) 178; 
    byte PUTSTATIC = (byte) 179; 
    byte GETFIELD = (byte) 180; 
    byte PUTFIELD = (byte) 181; 
    byte INVOKEVIRTUAL = (byte) 182; 
    byte INVOKESPECIAL = (byte) 183; 
    byte INVOKESTATIC = (byte) 184; 
    byte INVOKEINTERFACE = (byte) 185; 
    byte INVOKEDYNAMIC = (byte) 186; 
    byte NEW = (byte) 187; 
    byte NEWARRAY = (byte) 188; 
    byte ANEWARRAY = (byte) 189; 
    byte ARRAYLENGTH = (byte) 190; 
    byte ATHROW = (byte) 191; 
    byte CHECKCAST = (byte) 192; 
    byte INSTANCEOF = (byte) 193; 
    byte MONITORENTER = (byte) 194; 
    byte MONITOREXIT = (byte) 195; 
    byte WIDE = (byte) 196; 
    byte MULTIANEWARRAY = (byte) 197; 
    byte IFNULL = (byte) 198; 
    byte IFNONNULL = (byte) 199; 
    byte GOTO_W = (byte) 200; 
    byte JSR_W = (byte) 201;

    Set<Byte> binaryOperators = new HashSet<>(Arrays.asList( // todo: fill
            IADD, ISUB, IDIV, IMUL, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPLE, IF_ICMPGT, IF_ICMPGE, IF_ACMPEQ, IF_ACMPNE,
            FADD, FSUB, FDIV, FMUL, DADD, DSUB, DDIV, DMUL, LADD, LSUB, LDIV, LMUL, IREM, FREM, DREM, LREM,
            FCMPG, FCMPL, DCMPG, DCMPL, LCMP, IAND, IOR
    )), unaryOperators = new HashSet<>(Arrays.asList(
            INEG, FNEG, DNEG, LNEG, IFEQ, IFNE, IFLT, IFLE, IFGT, IFGE,
            I2F, I2B, I2C, I2D, I2L, I2S, F2I, F2D, F2L, ARRAYLENGTH
    )), jmpOperators = new HashSet<>(Arrays.asList(
            IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPLE, IF_ICMPGT, IF_ICMPGE, IF_ACMPEQ, IF_ACMPNE,
            GOTO, GOTO_W, IFEQ, IFNE, IFLT, IFLE, IFGT, IFGE
    )), intOperators = new HashSet<>(Arrays.asList( // operators that push int onto the stack
            IADD, ISUB, IDIV, IMUL, IREM, INEG, DCMPL, DCMPG, LCMP, FCMPG, FCMPL, ARRAYLENGTH, IAND, IOR
    )), floatOperators = new HashSet<>(Arrays.asList(FADD, FSUB, FDIV, FMUL, FNEG))
    , doubleOperators = new HashSet<>(Arrays.asList(DADD, DSUB, DDIV, DMUL, DREM, DNEG))
    , longOperators = new HashSet<>(Arrays.asList(LADD, LSUB, LDIV, LMUL, LREM, LNEG));
    static JVMType getType(byte opcode) {
        if (intOperators.contains(opcode)) return JVMType.INTEGER;
        if (longOperators.contains(opcode)) return JVMType.LONG;
        if (floatOperators.contains(opcode)) return JVMType.FLOAT;
        if (doubleOperators.contains(opcode)) return JVMType.DOUBLE;
        throw new RuntimeException("cannot find return type for opcode " + opcode);
    }

    static byte getLoadOpcode(int index, JVMType type) {
        int typeOffset = switch (type.getDescriptor()) {
            case "I" -> 0;
            case "J" -> 1;
            case "F" -> 2;
            case "D" -> 3;
            default -> 4;
        };
        if (index < 4) return (byte) (ILOAD_0 + index + typeOffset * 4);
        else return (byte) (ILOAD + typeOffset);
    }
    static byte getStoreOpcode(int index, JVMType type) {
        int typeOffset = switch (type.getDescriptor()) {
            case "I" -> 0;
            case "J" -> 1;
            case "F" -> 2;
            case "D" -> 3;
            default -> 4;
        };
        if (index < 4) return (byte) (ISTORE_0 + index + typeOffset * 4);
        else return (byte) (ISTORE + typeOffset);
    }
    static byte getConvertOpcode(JVMType from, JVMType to) {
        return (byte) switch (from.getDescriptor()) {
            case "I" -> I2L + switch (to.getDescriptor()) {
                case "J" -> 0;
                case "F" -> 1;
                case "D" -> 2;
                case "B" -> 12;
                case "C" -> 13;
                case "S" -> 14;
                default -> throw new RuntimeException("unsupported JVM type conversion " + from + " -> " + to);
            };
            case "J" -> L2I + switch (to.getDescriptor()) {
                case "I" -> 0;
                case "F" -> 1;
                case "D" -> 2;
                default -> throw new RuntimeException("unsupported JVM type conversion " + from + " -> " + to);
            };
            case "F" -> F2I + switch (to.getDescriptor()) {
                case "I" -> 0;
                case "J" -> 1;
                case "D" -> 2;
                default -> throw new RuntimeException("unsupported JVM type conversion " + from + " -> " + to);
            };
            case "D" -> D2I + switch (to.getDescriptor()) {
                case "I" -> 0;
                case "J" -> 1;
                case "F" -> 2;
                default -> throw new RuntimeException("unsupported JVM type conversion " + from + " -> " + to);
            };
            default -> throw new RuntimeException("unsupported JVM type conversion " + from + " -> " + to);
        };
    }
    static byte getArrayStoreOpcode(JVMType type) {
        return (byte) (IASTORE + switch (type.getDescriptor()) {
            case "[I" -> 0;
            case "[J" -> 1;
            case "[F" -> 2;
            case "[D" -> 3;
            case "[B" -> 5;
            case "[Z" -> 5;
            case "[C" -> 6;
            case "[S" -> 7;
            default -> 4;
        });
    }
    static byte getArrayLoadOpcode(JVMType type) {
        return (byte) (IALOAD + switch (type.getDescriptor()) {
            case "[I" -> 0;
            case "[J" -> 1;
            case "[F" -> 2;
            case "[D" -> 3;
            case "[B" -> 5;
            case "[Z" -> 5;
            case "[C" -> 6;
            case "[S" -> 7;
            default -> 4;
        });
    }

    short ACC_PUBLIC = 0x0001;
    short ACC_PRIVATE = 0x0002;
    short ACC_PROTECTED = 0x0004;
    short ACC_STATIC = 0x0008;
    short ACC_FINAL = 0x0010;
    short ACC_SUPER = 0x0020;
    short ACC_INTERFACE = 0x0200;
    short ACC_SYNCHRONIZED = 0x0020;
    short ACC_BRIDGE = 0x0040;
    short ACC_VARARGS = 0x0080;
    short ACC_NATIVE = 0x0100;
    short ACC_ABSTRACT = 0x0400;
    short ACC_STRICT = 0x0800;
    short ACC_SYNTHETIC = 0x1000;
    short ACC_ANNOTATION = 0x2000;
    short ACC_ENUM = 0x4000;
}
