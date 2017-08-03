package lex;

import java.util.*;
import java.util.function.Consumer;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class TokenStream implements Iterator<Token>  // todo: create specialized exception for lexing
{
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "if", "else", "for", "while", "true", "false"
    ));
    private static final Set<Character> PUNCTUATIONS = new HashSet<>(Arrays.asList(
            ';', '[', ']', ')', '(', '{', '}', ','
    )), OPERATORS = new HashSet<>(Arrays.asList(
            '=', '!', '+', '-', '/', '*', '%', '&', '|', '<', '>'
    ));
    
    private String code;
    private int index, length;

    public TokenStream(String code) {
        this.code = code;

        length = code.length();
        index = 0;
    }

    @Override
    public Token next() {
        char c = code.charAt(index);

        if (Character.isSpaceChar(c)) {
            index++;
            return next();
        }
        if (Character.isLetter(c) || c == '_') return readIdentifier();
        if (Character.isDigit(c)) return readNumber();
        if (c == '"') return readString();

        index++;
        if (PUNCTUATIONS.contains(c)) return new Token(Token.Type.PUNCTUATION, "" + c);
        if (OPERATORS.contains(c)) return new Token(Token.Type.OPERATOR, "" + c);

        throw new RuntimeException("Error: Can't Handle Char " + c + " at " + index);
    }

    @Override
    public void forEachRemaining(Consumer<? super Token> action) { while (hasNext()) action.accept(next()); }

    @Override
    public boolean hasNext() {  return index < length; }

    private Token readString() {
        int end = code.indexOf('"', index + 1);
        if (end == -1) throw new RuntimeException("Lexing Error String Blah Blah Blah");
        String value = code.substring(index, end);
        index = end + 1;
        return new Token(Token.Type.STRING, value);
    }

    private Token readNumber() {
        StringBuilder value = new StringBuilder().append(code.charAt(index++));
        boolean dot = false;
        while (index < length) {
            char c = code.charAt(index++);
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
