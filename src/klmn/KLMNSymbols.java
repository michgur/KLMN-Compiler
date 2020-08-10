package klmn;

import lexing.Grammar;
import lexing.Symbol;
import lexing.Terminal;

public interface KLMNSymbols
{
    Terminal PLUS = new Terminal("+");
    Terminal MINUS = new Terminal("-");
    Terminal TIMES = new Terminal("*");
    Terminal DIVIDE = new Terminal("/");
    Terminal MODULO = new Terminal("%");
    Terminal ROUND_OPEN = new Terminal("(");
    Terminal ROUND_CLOSE = new Terminal(")");
    Terminal SQUARE_OPEN = new Terminal("[");
    Terminal SQUARE_CLOSE = new Terminal("]");
    Terminal NUMBER = new Terminal("num");
    Terminal SEMICOLON = new Terminal(";");
    Terminal EQUALS = new Terminal("==");
    Terminal NEQUALS = new Terminal("!=");
    Terminal LT = new Terminal("<");
    Terminal GT = new Terminal(">");
    Terminal LTEQUALS = new Terminal("<=");
    Terminal GTEQUALS = new Terminal(">=");
    Terminal CURLY_OPEN = new Terminal("{");
    Terminal CURLY_CLOSE = new Terminal("}");
    Terminal LOGICAL_AND = new Terminal("&&");
    Terminal LOGICAL_OR = new Terminal("||");
    Terminal ASSIGN = new Terminal("=");
    Terminal KW_VAR = new Terminal("var");
    Terminal KW_PRINT = new Terminal("print");
    Terminal INCREMENT = new Terminal("++");
    Terminal DECREMENT = new Terminal("--");
    Terminal IDENTIFIER = new Terminal("ID");
    Terminal KW_IF = new Terminal("if");
    Terminal KW_FOR = new Terminal("for");
    Terminal KW_WHILE = new Terminal("while");
    Terminal KW_TRUE = new Terminal("true");
    Terminal KW_FALSE = new Terminal("false");
    Terminal KW_LENGTH = new Terminal("length");
    Terminal COMMA = new Terminal(",");
    Symbol MODULE = new Symbol("MDL");
    Symbol MODULE_COMPONENTS = new Symbol("MDL_COMPONENTS");
    Symbol VAR_DECL = new Symbol("VAR_DECL");
    Symbol CLS_DECL = new Symbol("CLS_DECL");
    Symbol FUNC_DECL = new Symbol("FUNC_DECL");
    Symbol TYPE = new Symbol("TYPE");
    Symbol PARAMS_DECL = new Symbol("PARAMS_DECL");
    Symbol PARAMS_DECL_NON_ZERO = new Symbol("PARAMS_DECL_NOT_EMPTY");
    Symbol BLOCK = new Symbol("BLCK");
    Symbol STATEMENT = new Symbol("STMT");
    Symbol ASSIGNMENT = new Symbol(":=");
    Symbol STATEMENT_FULL = new Symbol("STMT1");
    Symbol EXPRESSION = new Symbol("EXPR");
    Symbol VALUE = new Symbol("VALUE");
    Symbol VALUE_SIGNED = new Symbol("VALUE_SIGNED");
    Symbol VALUE_UNARY_OP = new Symbol("VALUE_UNARY_OP");
    Symbol VALUE_TIMES_OP = new Symbol("VALUE_TIMES_OP");
    Symbol VALUE_PLUS_OP = new Symbol("VALUE_PLUS_OP");
    Symbol VALUE_COMP_OP = new Symbol("VALUE_COMP_OP");
    Symbol VALUE_EQUALS_OP = new Symbol("VALUE_EQUALS_OP");
    Symbol VALUE_LOGICAL_OP = new Symbol("VALUE_LOGICAL_OP");
    Symbol ARRAY_INIT = new Symbol("ARRAY_INIT");
    Symbol ARRAY_VALUE = new Symbol("ARRAY_VALUE");

    Symbol ROOT = new Symbol("ROOT") {
        {
            MODULE.addProduction(MODULE_COMPONENTS);

            MODULE_COMPONENTS.addProduction();
            MODULE_COMPONENTS.addProduction(MODULE_COMPONENTS, VAR_DECL, SEMICOLON);
            // MODULE_COMPONENTS.addProduction(MODULE_COMPONENTS, CLS_DECL);
            MODULE_COMPONENTS.addProduction(MODULE_COMPONENTS, FUNC_DECL);

            VAR_DECL.addProduction(TYPE, IDENTIFIER);
            VAR_DECL.addProduction(TYPE, IDENTIFIER, ASSIGN, EXPRESSION);

            FUNC_DECL.addProduction(TYPE, IDENTIFIER, ROUND_OPEN, PARAMS_DECL, ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE);

            TYPE.addProduction(IDENTIFIER);
            TYPE.addProduction(TYPE, SQUARE_OPEN, SQUARE_CLOSE);

            PARAMS_DECL_NON_ZERO.addProduction(TYPE, IDENTIFIER);
            PARAMS_DECL_NON_ZERO.addProduction(PARAMS_DECL_NON_ZERO, COMMA, TYPE, IDENTIFIER);
            PARAMS_DECL.addProduction(PARAMS_DECL_NON_ZERO);
            PARAMS_DECL.addProduction();

            BLOCK.addProduction(STATEMENT_FULL);
            BLOCK.addProduction(BLOCK, STATEMENT_FULL);
            STATEMENT_FULL.addProduction(STATEMENT, SEMICOLON);
            STATEMENT_FULL.addProduction(KW_FOR, ROUND_OPEN, STATEMENT_FULL, EXPRESSION, SEMICOLON, STATEMENT, ROUND_CLOSE, STATEMENT_FULL);
            STATEMENT_FULL.addProduction(KW_FOR, ROUND_OPEN, STATEMENT_FULL, EXPRESSION, SEMICOLON, STATEMENT, ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE);
            STATEMENT_FULL.addProduction(KW_IF, ROUND_OPEN, EXPRESSION, ROUND_CLOSE, STATEMENT_FULL);
            STATEMENT_FULL.addProduction(KW_IF, ROUND_OPEN, EXPRESSION, ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE);
            STATEMENT_FULL.addProduction(KW_WHILE, ROUND_OPEN, EXPRESSION, ROUND_CLOSE, STATEMENT_FULL);
            STATEMENT_FULL.addProduction(KW_WHILE, ROUND_OPEN, EXPRESSION, ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE);
            STATEMENT.addProduction(ASSIGNMENT);
            STATEMENT.addProduction(ARRAY_INIT);
            ASSIGNMENT.addProduction(VAR_DECL);
            ASSIGNMENT.addProduction(IDENTIFIER, ASSIGN, EXPRESSION);
            ASSIGNMENT.addProduction(IDENTIFIER, SQUARE_OPEN, EXPRESSION, SQUARE_CLOSE, ASSIGN, EXPRESSION);
            ARRAY_INIT.addProduction(IDENTIFIER, SQUARE_OPEN, EXPRESSION, SQUARE_CLOSE, IDENTIFIER);
            ARRAY_INIT.addProduction(IDENTIFIER, SQUARE_OPEN, SQUARE_CLOSE, IDENTIFIER, ASSIGN, CURLY_OPEN, ARRAY_VALUE, CURLY_CLOSE);
            STATEMENT.addProduction(KW_PRINT, EXPRESSION);
            EXPRESSION.addProduction(EXPRESSION, LOGICAL_OR, VALUE_LOGICAL_OP);
            EXPRESSION.addProduction(VALUE_LOGICAL_OP); // expression -> VALUE_LOGICAL_OP
            VALUE_LOGICAL_OP.addProduction(VALUE_LOGICAL_OP, LOGICAL_AND, VALUE_EQUALS_OP);
            VALUE_LOGICAL_OP.addProduction(VALUE_EQUALS_OP); // VALUE_LOGICAL_OP -> VALUE_EQUALS_OP
            VALUE_EQUALS_OP.addProduction(VALUE_EQUALS_OP, EQUALS, VALUE_COMP_OP);
            VALUE_EQUALS_OP.addProduction(VALUE_EQUALS_OP, NEQUALS, VALUE_COMP_OP);
            VALUE_EQUALS_OP.addProduction(VALUE_COMP_OP); // VALUE_EQUALS_OP -> VALUE_COMP_OP
            VALUE_COMP_OP.addProduction(VALUE_COMP_OP, LT, VALUE_PLUS_OP);
            VALUE_COMP_OP.addProduction(VALUE_COMP_OP, GT, VALUE_PLUS_OP);
            VALUE_COMP_OP.addProduction(VALUE_COMP_OP, LTEQUALS, VALUE_PLUS_OP);
            VALUE_COMP_OP.addProduction(VALUE_COMP_OP, GTEQUALS, VALUE_PLUS_OP);
            VALUE_COMP_OP.addProduction(VALUE_PLUS_OP); // VALUE_COMP_OP -> VALUE_PLUS_OP
            VALUE_PLUS_OP.addProduction(VALUE_PLUS_OP, PLUS, VALUE_TIMES_OP);
            VALUE_PLUS_OP.addProduction(VALUE_PLUS_OP, MINUS, VALUE_TIMES_OP);
            VALUE_PLUS_OP.addProduction(VALUE_TIMES_OP); // VALUE_PLUS_OP -> T
            VALUE_TIMES_OP.addProduction(VALUE_TIMES_OP, TIMES, VALUE_SIGNED);
            VALUE_TIMES_OP.addProduction(VALUE_TIMES_OP, DIVIDE, VALUE_SIGNED);
            VALUE_TIMES_OP.addProduction(VALUE_TIMES_OP, MODULO, VALUE_SIGNED);
            VALUE_TIMES_OP.addProduction(VALUE_SIGNED);
            VALUE_SIGNED.addProduction(VALUE_UNARY_OP);
            VALUE_SIGNED.addProduction(PLUS, VALUE_SIGNED);
            VALUE_SIGNED.addProduction(MINUS, VALUE_SIGNED);
            VALUE_SIGNED.addProduction(VALUE);
            STATEMENT.addProduction(VALUE_UNARY_OP);
            EXPRESSION.addProduction(VALUE_UNARY_OP);
            VALUE_UNARY_OP.addProduction(KW_LENGTH, ROUND_OPEN, IDENTIFIER, ROUND_CLOSE);
            VALUE_UNARY_OP.addProduction(VALUE, DECREMENT);
            VALUE_UNARY_OP.addProduction(VALUE, INCREMENT);
            VALUE_UNARY_OP.addProduction(INCREMENT, VALUE);
            VALUE_UNARY_OP.addProduction(DECREMENT, VALUE);
            VALUE.addProduction(ROUND_OPEN, EXPRESSION, ROUND_CLOSE);
            VALUE.addProduction(NUMBER);
            VALUE.addProduction(KW_TRUE);
            VALUE.addProduction(KW_FALSE);
            VALUE.addProduction(IDENTIFIER);
            VALUE.addProduction(IDENTIFIER, SQUARE_OPEN, EXPRESSION, SQUARE_CLOSE);
            ARRAY_VALUE.addProduction(EXPRESSION);
            ARRAY_VALUE.addProduction(ARRAY_VALUE, COMMA, EXPRESSION);
        }
    };
    Grammar GRAMMAR = new Grammar(MODULE);
}
