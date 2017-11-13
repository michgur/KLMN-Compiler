package klmn;

import lang.*;

public interface KGrammar
{
    Tokenizer TOKENIZER = new Tokenizer();
    Grammar GRAMMAR = init();
    
    Symbol M = new Symbol("Module");
    Symbol VD = new Symbol("VarDecl");
    Symbol FD = new Symbol("FuncDecl");
    Symbol MF = new Symbol("Modifiers");
    Symbol MF0 = new Symbol("Modifiers0");
    Symbol T = new Symbol("Type");
    Symbol E = new Symbol("Expression");
    Symbol SE = new Symbol("StatementExpression");
    Symbol S = new Symbol("Statement");
    Symbol FI = new Symbol("ForInit");
    Symbol B = new Symbol("Block");
    Symbol A = new Symbol("Assignment");
    Symbol P = new Symbol("Params");
    Symbol PD = new Symbol("ParamsDecl");
    Symbol PD0 = new Symbol("ParamsDecl0");
    Symbol DI = new Symbol("Decrement/Increment");

    Terminal id = new Terminal("identifier");
    Terminal assign = new Terminal("=");
    Terminal semicolon = new Terminal(";");
    Terminal openRound = new Terminal("(");
    Terminal closeRound = new Terminal(")");
    Terminal comma = new Terminal(",");
    Terminal openCurly = new Terminal("{");
    Terminal closeCurly = new Terminal("}");
    Terminal kwIf = new Terminal("if");
    Terminal kwElse = new Terminal("else");
    Terminal kwFor = new Terminal("for");
    Terminal kwPrint = new Terminal("print");
    Terminal kwFinal = new Terminal("final");
    Terminal kwStatic = new Terminal("static");
    Terminal kwPublic = new Terminal("public");
    Terminal kwPrivate = new Terminal("private");
    Terminal kwReturn = new Terminal("return");

    Symbol T0 = new Symbol("Primary");
    Symbol T1 = new Symbol("Prefixed");
    Symbol T2 = new Symbol("Mul/Div");
    Symbol T3 = new Symbol("Add/Sub");
    Symbol T4 = new Symbol("Cmp0");
    Symbol T5 = new Symbol("Cmp1");
    Symbol T6 = new Symbol("Logical Ops");
    
    Terminal plus = new Terminal("+");
    Terminal minus = new Terminal("-");
    Terminal times = new Terminal("*");
    Terminal divide = new Terminal("/");
    Terminal numberL = new Terminal("num");
    Terminal stringL = new Terminal("string");
    Terminal eq = new Terminal("==");
    Terminal ne = new Terminal("!=");
    Terminal lt = new Terminal("<");
    Terminal gt = new Terminal(">");
    Terminal le = new Terminal("<=");
    Terminal ge = new Terminal(">=");
    Terminal lAnd = new Terminal("&&");
    Terminal lOr = new Terminal("||");
    Terminal increment = new Terminal("++");
    Terminal decrement = new Terminal("--");
    Terminal kwTrue = new Terminal("true");
    Terminal kwFalse = new Terminal("false");
    
    private static Grammar init() {
        M.addProduction();
        M.addProduction(M, VD, semicolon);
        M.addProduction(M, FD);

        VD.addProduction(MF, T, id);
        VD.addProduction(MF, T, A);
        VD.addProduction(T, id);
        VD.addProduction(T, A);

        FD.addProduction(MF, T, id, openRound, PD, closeRound, openCurly, B, closeCurly);
        FD.addProduction(T, id, openRound, PD, closeRound, openCurly, B, closeCurly);
        
        PD0.addProduction(T, id);
        PD0.addProduction(PD0, comma, T, id);
        PD.addProduction();
        PD.addProduction(PD0);
        
        MF0.addProduction(kwFinal);
        MF0.addProduction(kwPublic);
        MF0.addProduction(kwPrivate);
        MF0.addProduction(kwStatic);
        MF.addProduction(MF0);
        MF.addProduction(MF, MF0);

        T.addProduction(id);

        A.addProduction(id, assign, E);

        SE.addProduction(A);
        SE.addProduction(id, openRound, P, closeRound); // func call, add proper id symbol
        SE.addProduction(id, openRound, closeRound); // func call, add proper id symbol
        SE.addProduction(DI);

        S.addProduction(SE, semicolon);
        S.addProduction(VD, semicolon);
        S.addProduction(kwPrint, E, semicolon);
        S.addProduction(kwIf, openRound, E, closeRound, S);
        S.addProduction(kwIf, openRound, E, closeRound, S, kwElse, S);

        FI.addProduction(SE);
        FI.addProduction(VD);
        S.addProduction(kwFor, openRound, FI, semicolon, E, semicolon, SE, closeRound, S);
        S.addProduction(openCurly, B, closeCurly);
        S.addProduction(kwReturn, semicolon);
        S.addProduction(kwReturn, E, semicolon);

        B.addProduction();
        B.addProduction(S);
        B.addProduction(B, S);

        P.addProduction(E);
        P.addProduction(P, comma, E);

        //<editor-fold desc="Expressions">
        E.addProduction(E, lOr, T6);
        E.addProduction(T6); // E -> T6
        T6.addProduction(T6, lAnd, T5);
        T6.addProduction(T5); // T6 -> T5
        T5.addProduction(T5, eq, T4);
        T5.addProduction(T5, ne, T4);
        T5.addProduction(T4); // T5 -> T4
        T4.addProduction(T4, lt, T3);
        T4.addProduction(T4, gt, T3);
        T4.addProduction(T4, le, T3);
        T4.addProduction(T4, ge, T3);
        T4.addProduction(T3); // T4 -> T3
        T3.addProduction(T3, plus, T2);
        T3.addProduction(T3, minus, T2);
        T3.addProduction(T2); // T3 -> T
        T2.addProduction(T2, times, T1);
        T2.addProduction(T2, divide, T1);
        T2.addProduction(T1);
        T1.addProduction(DI);
        T1.addProduction(plus, T1);
        T1.addProduction(minus, T1);
        T1.addProduction(T0);
        DI.addProduction(T0, decrement);
        DI.addProduction(T0, increment);
        DI.addProduction(increment, T0);
        DI.addProduction(decrement, T0);
        T0.addProduction(openRound, E, closeRound);
        T0.addProduction(numberL);
        T0.addProduction(stringL);
        T0.addProduction(kwTrue);
        T0.addProduction(kwFalse);
        T0.addProduction(id);
        T0.addProduction(SE);
        //</editor-fold>

        //<editor-fold desc="tokenizing">
        TOKENIZER.addTerminal(assign, '=').addTerminal(plus, '+').addTerminal(semicolon, ';')
                .addTerminal(minus, '-').addTerminal(times, '*').addTerminal(lAnd, "&&").addTerminal(lOr, "||")
                .addTerminal(divide, '/').addTerminal(openRound, '(').addTerminal(closeRound, ')')
                .addTerminal(increment, "++").addTerminal(decrement, "--")
                .addTerminal(kwPrint, "print").addTerminal(kwIf, "if").addTerminal(comma, ",")
                .addTerminal(openCurly, '{').addTerminal(closeCurly, '}').addTerminal(kwFor, "for")
                .addTerminal(kwTrue, "true").addTerminal(kwFalse, "false")
                .addTerminal(eq, "==").addTerminal(ne, "!=").addTerminal(lt, '<')
                .addTerminal(gt, '>').addTerminal(le, "<=").addTerminal(ge, ">=")
                .addTerminal(kwElse, "else").addTerminal(kwFinal, "final").addTerminal(kwStatic, "static")
                .addTerminal(kwPrivate, "private").addTerminal(kwPublic, "public").addTerminal(kwReturn, "return")
                .addTerminal(numberL, (src, i) -> {
                    if (!Character.isDigit(src.charAt(i))) return null;
                    StringBuilder value = new StringBuilder().append(src.charAt(i));
                    boolean dot = false;
                    while (++i < src.length()) {
                        char c = src.charAt(i);
                        if (c == '.' && !dot) {
                            dot = true;
                            value.append('.');
                        }
                        else if (Character.isDigit(c)) value.append(c);
                        else {
                            if (c == 'f') value.append(c);
                            break;
                        }
                    }
                    return value.toString();
                })
                .addTerminal(stringL, (src, i) -> {
                    if (src.charAt(i) != '"') return null;
                    StringBuilder value = new StringBuilder();
                    while (++i < src.length()) {
                        char c = src.charAt(i);
                        if (c == '"') break;
                        value.append(c);
                    }
                    return "  " + value.toString();
                })
                .addTerminal(id, (src, i) -> {
                    if (!(Character.isLetter(src.charAt(i)) || src.charAt(i) == '_')) return null;
                    StringBuilder value = new StringBuilder().append(src.charAt(i));
                    while (++i < src.length()) {
                        char c = src.charAt(i);
                        if (Character.isLetterOrDigit(c) || c == '_') value.append(c);
                        else break;
                    }
                    return value.toString();
                });
        TOKENIZER.ignore((src, i) -> { // ignore spaces
            char c = src.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t' && c != '\r') return null;
            StringBuilder value = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                c = src.charAt(i);
                if (c == ' ' || c == '\n' || c == '\t' || c == '\r') value.append(c);
                else break;
            }
            return value.toString();
        });
        TOKENIZER.ignore((src, i) -> { // ignore comments
            if (src.charAt(i) != '#') return null;
            int end = src.indexOf('\n', i);
            if (end == -1) return src.substring(i);
            else return src.substring(i, end + 1);
        });
        //</editor-fold>

        return new Grammar(M);
    }
}
