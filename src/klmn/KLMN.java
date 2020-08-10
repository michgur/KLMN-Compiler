package klmn;

import codegen.CodeGenerator;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import lexing.Tokenizer;
import lexing.TokenStream;
import parsing.AST;
import parsing.Parser;

import java.util.regex.Pattern;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/27/2017.
 */
public class KLMN implements Opcodes, KLMNSymbols
{
    private static Tokenizer initializeTokenizer() {
        Tokenizer klmn = new Tokenizer();
        klmn.addTerminal(ASSIGN, '=');
        klmn.addTerminal(PLUS, '+');
        klmn.addTerminal(SEMICOLON, ';');
        klmn.addTerminal(MINUS, '-');
        klmn.addTerminal(TIMES, '*');
        klmn.addTerminal(LOGICAL_AND, "&&");
        klmn.addTerminal(LOGICAL_OR, "||");
        klmn.addTerminal(DIVIDE, '/');
        klmn.addTerminal(MODULO, '%');
        klmn.addTerminal(ROUND_OPEN, '(');
        klmn.addTerminal(ROUND_CLOSE, ')');
        klmn.addTerminal(SQUARE_OPEN, '[');
        klmn.addTerminal(SQUARE_CLOSE, ']');
        klmn.addTerminal(INCREMENT, "++");
        klmn.addTerminal(DECREMENT, "--");
        klmn.addTerminal(KW_VAR, "var");
        klmn.addTerminal(KW_PRINT, "print");
        klmn.addTerminal(KW_LENGTH, "length");
        klmn.addTerminal(KW_IF, "if");
        klmn.addTerminal(CURLY_OPEN, '{');
        klmn.addTerminal(CURLY_CLOSE, '}');
        klmn.addTerminal(KW_FOR, "for");
        klmn.addTerminal(KW_WHILE, "while");
        klmn.addTerminal(KW_TRUE, "true");
        klmn.addTerminal(KW_FALSE, "false");
        klmn.addTerminal(EQUALS, "==");
        klmn.addTerminal(NEQUALS, "!=");
        klmn.addTerminal(LT, '<');
        klmn.addTerminal(GT, '>');
        klmn.addTerminal(LTEQUALS, "<=");
        klmn.addTerminal(GTEQUALS, ">=");
        klmn.addTerminal(COMMA, ',');
        klmn.addTerminal(NUMBER, (src, i) -> {
            if (!Character.isDigit(src.charAt(i))) return null;
            StringBuilder result = new StringBuilder().append(src.charAt(i));
            boolean dot = false;
            while (++i < src.length()) {
                char c = src.charAt(i);
                if (c == '.' && !dot) {
                    dot = true;
                    result.append('.');
                } else if (Character.isDigit(c)) result.append(c);
                else break;
            }
            return result.toString();
        });
        klmn.addTerminal(IDENTIFIER, (src, i) -> {
            if (!(Character.isLetter(src.charAt(i)) || src.charAt(i) == '_')) return null;
            StringBuilder result = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                char c = src.charAt(i);
                if (Character.isLetterOrDigit(c) || c == '_') result.append(c);
                else break;
            }
            return result.toString();
        });
        klmn.ignore((src, i) -> { // ignore spaces
            char c = src.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t' && c != '\r') return null;
            StringBuilder result = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                c = src.charAt(i);
                if (c == ' ' || c == '\n' || c == '\t' || c == '\r') result.append(c);
                else break;
            }
            return result.toString();
        });
        klmn.ignore((src, i) -> { // ignore comments
            if (src.charAt(i) != '#') return null;
            int end = src.indexOf('\n', i);
            if (end == -1) return src.substring(i);
            else return src.substring(i, end + 1);
        });
        return klmn;
    }

    public static ClassFile compile(String code) {
        Tokenizer klmn = initializeTokenizer();
        TokenStream t = klmn.tokenize(code);

        AST ast = new Parser(KLMNSymbols.GRAMMAR).parse(t);
        ClassFile classFile = new ClassFile("Poop");
        CodeGenerator writer = CodeGenInit.initialize_codeGenerator(classFile);
        writer.apply(ast);

        return classFile;
    }
}
