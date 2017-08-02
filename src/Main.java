import lex.TokenStream;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    public static void main(String[] args) {
        String code = "xavier () {= 233 *  bona89 _sk__  456}";
        TokenStream t = new TokenStream(code);
        t.forEachRemaining(token -> System.out.println(token.getType() + " " + token.getValue()));
    }
}
