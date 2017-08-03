import lex.TokenStream;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    public static void main(String[] args) {
        String code = "12 * (34 + 2.2)";
        TokenStream t = new TokenStream(code);
        t.forEachRemaining(token -> System.out.println(token.getType() + " " + token.getValue()));
    }
}
