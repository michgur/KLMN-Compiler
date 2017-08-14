package lex;

import java.util.*;
import java.util.function.Consumer;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class TokenStream implements Iterator<Token>
{
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
            return new Token(Token.Type.END, "$");
        }
        char c = code.charAt(index);

        if (Character.isSpaceChar(c)) {
            index++;
            return next();
        }
        if (Character.isLetter(c) || c == '_') return readIdentifier();
        if (Character.isDigit(c)) return readNumber();
        if (c == '"') return readString();
        if (c == '#') { // comment
            index = code.indexOf('\n', index) + 1;
            if (index == 0) index = length;
            return readNext();
        }

        index++;
        switch (c) {
            case '(': return new Token(Token.Type.OPEN_PAREN, c + "");
            case ')': return new Token(Token.Type.CLOSE_PAREN, c + "");
            case '{': return new Token(Token.Type.OPEN_CURLY, c + "");
            case '}': return new Token(Token.Type.CLOSE_CURLY, c + "");
            case '[': return new Token(Token.Type.OPEN_BRACKET, c + "");
            case ']': return new Token(Token.Type.CLOSE_BRACKET, c + "");
            case '+': return new Token(Token.Type.PLUS, c + "");
            case '-': return new Token(Token.Type.DASH, c + "");
            case '*': return new Token(Token.Type.ASTERISK, c + "");
            case '/': return new Token(Token.Type.SLASH, c + "");
            case '=': return new Token(Token.Type.EQUALS, c + "");
            case ';': return new Token(Token.Type.SEMICOLON, c + "");
            case ',': return new Token(Token.Type.COMMA, c + "");

            default: throw new RuntimeException("Error: Can't Handle Char " + c + " at " + index);
        }
    }

    private Token readString() {
        int end = code.indexOf('"', index + 1);
        if (end == -1) throw new RuntimeException("Lexing Error String Blah Blah Blah");
        String value = code.substring(index, end + 1);
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
        StringBuilder value = new StringBuilder().append(code.charAt(index));
        while (++index < length) {
            char c = code.charAt(index);
            if (Character.isLetterOrDigit(c) || c == '_') value.append(c);
            else break;
        }
        String v = value.toString();
        if (v.equals("class")) return new Token(Token.Type.CLASS, "class");
        if (v.equals("true")) return new Token(Token.Type.TRUE, "true");
        if (v.equals("false")) return new Token(Token.Type.FALSE, "false");

        return new Token(Token.Type.IDENTIFIER, v);
    }
}
