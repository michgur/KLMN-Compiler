package klmn.nodes;

import klmn.KGrammar;
import klmn.writing.MethodWriter;
import klmn.writing.types.Type;
import lang.Token;

public class NumberLiteral extends ExpNode
{
    // currently only integers / floats

    private float value;
    private boolean isInteger;
    public NumberLiteral(String value) {
        super(new Token(KGrammar.numberL, value));
        isInteger = !(value.contains(".") || value.endsWith("f") || value.endsWith("F"));
        this.value = Float.parseFloat(value);
    }
    public NumberLiteral(float value) {
        super(new Token(KGrammar.numberL, Float.toString(value)));
//        isInteger = value == (int) value; currently disables because float parameters
        this.value = value;
    }

    @Override
    protected Type typeCheck(MethodWriter writer) {
        return isInteger ? writer.getTypeEnv().getForDescriptor("I")
                : writer.getTypeEnv().getForDescriptor("F");
    }

    public float getFloatValue() { return value; }
    public boolean isInteger() { return isInteger; }

    @Override
    public void write(MethodWriter writer) {
        if (isInteger) writer.pushInt((int) value);
        else writer.pushFloat(value);
    }
}
