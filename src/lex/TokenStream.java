package lex;

import java.util.*;
import java.util.function.Consumer;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class TokenStream implements Iterator<Token>
{
    public static final Token END = new Token(Token.Type.END, "$");

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "if", "else", "for", "while", "true", "false"
    ));
    
    private String code;
    private int index, length;
    private boolean end = false;

    private Token next = null;

    public TokenStream(String code) {
        this.code = code;

        length = code.length();
        index = 0;
    }

    public Token peek() {
        if (next == null) next = readNext();
        return next;
    }

    @Override
    public Token next() {
        Token n = (next != null) ? next : readNext();
        next = null;
        return n;
    }

    @Override
    public void forEachRemaining(Consumer<? super Token> action) { while (hasNext()) action.accept(next()); }

    @Override
    public boolean hasNext() { return !end; }

    private Token readNext() {
        if (index >= length) {
            end = true;
            return END;
        }
        char c = code.charAt(index);

        if (Character.isSpaceChar(c)) {
            index++;
            return next();
        }
        if (Character.isLetter(c) || c == '_') return readIdentifier();
        if (Character.isDigit(c)) return readNumber();
        if (c == '"') return readString();

        index++;
        if (c == '+') return new Token(Token.Type.PLUS, c + "");
        if (c == '(') return new Token(Token.Type.OPEN_PAREN, c + "");
        if (c == ')') return new Token(Token.Type.CLOSE_PAREN, c + "");
        if (c == '*') return new Token(Token.Type.TIMES, c + "");


        throw new RuntimeException("Error: Can't Handle Char " + c + " at " + index);
    }

    private Token readString() {
        int end = code.indexOf('"', index + 1);
        if (end == -1) throw new RuntimeException("Lexing Error String Blah Blah Blah");
        String value = code.substring(index, end);
        index = end + 1;
        return new Token(Token.Type.STRING, value);
    }

    private Token readNumber() {
        StringBuilder value = new StringBuilder().append(code.charAt(index));
        boolean dot = false;
        while (++index < length) {
            char c = code.charAt(index);
            if (c == '.' && !dot) {
                dot = true;
                value.append('.');
            }
            else if (Character.isDigit(c)) value.append(c);
            else break;
        }
        return new Token(Token.Type.NUMBER, value.toString());
    }

    private Token readIdentifier() {
        StringBuilder value = new StringBuilder().append(code.charAt(index++));
        while (index < length) {
            char c = code.charAt(index++);
            if (Character.isLetterOrDigit(c) || c == '_') value.append(c);
            else break;
        }
        String v = value.toString();
        return new Token((KEYWORDS.contains(v)) ? Token.Type.KEYWORD : Token.Type.IDENTIFIER, v);
    }
}
