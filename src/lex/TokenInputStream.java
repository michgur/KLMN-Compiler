package lex;

import org.intellij.lang.annotations.Language;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class TokenInputStream
{
    private enum TokenType {
        SPACE("\\s+"),
        IDENTIFIER("[_A-Za-z][_0-9A-Za-z]*"),
        STRING("\".*?\""),
        NUMBER("[0-9]+(\\.[0-9]+)?"),
        PUNCTUATION("[]\\[,;{}()]"),
        OPERATOR("[+=*/!<>&|%-]"),
        KEYWORD("/^"); // not using this pattern
        
        private Pattern pattern; // todo: something else (not regex) (check incoming char and determine type)
        TokenType(@Language("regexp") String regex) { pattern = Pattern.compile(String.format("^(%s)", regex)); }

        public Pattern getPattern() { return pattern; }
    }

    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "if", "else", "for", "while", "true", "false"
    ));
    
    private Queue<String> tokens = new LinkedList<>();

    public TokenInputStream(String code) {
        int begin = 0;
        while (!(code = code.substring(begin)).isEmpty()) {
            Matcher matcher;
            String match = null;
            for (TokenType t : TokenType.values())
                if ((matcher = t.getPattern().matcher(code)).find()) {
                    match = matcher.group(1); // since we surround patterns with ^()
                    if (t == TokenType.IDENTIFIER)
                        if (KEYWORDS.contains(match)) {} // match is a keyword, do something
                    if (t != TokenType.SPACE) tokens.add(match);
                    begin = match.length();
                }
            if (match == null) throw new RuntimeException("Error: Can't Handle Code (around " + begin + ')');
        }
    }
}
