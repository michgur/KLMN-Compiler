package klmn;

import codegen.CodeGenerator;
import jdk.internal.org.objectweb.asm.Opcodes;
import jvm.classes.ClassFile;
import klmn.visitors.*;
import lexing.Symbol;

public class CodeGenInit implements Opcodes, KLMNSymbols
{
    public static CodeGenerator initialize_codeGenerator(ClassFile classFile) {
        CodeGenerator generator = new CodeGenerator(classFile);

        // todo: reorganize the remaining visitors, handle types in some way
        generator.addVisitor(MODULE, Modules::apply);
        generator.addVisitor(MODULE_COMPONENTS, (g, ast) -> {
            if (ast.length == 0) return;
            g.apply(ast[0]);
            g.apply(ast[1]);
        });
        generator.addVisitor(FUNC_DECL, new MethodVisitor()::declare);
        generator.addVisitor(VAR_DECL, Variables::declareVar);
        generator.addVisitor(BLOCK, new Symbol[] { BLOCK, STATEMENT_FULL }, (g, ast) -> {
            g.apply(ast[0]);
            g.apply(ast[1]);
        });
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { STATEMENT, SEMICOLON }, (g, ast) -> g.apply(ast[0]));
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { KW_FOR, ROUND_OPEN, STATEMENT_FULL, EXPRESSION,
                SEMICOLON, STATEMENT, ROUND_CLOSE, STATEMENT_FULL }, ControlFlow::forStatement);
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { KW_FOR, ROUND_OPEN, STATEMENT_FULL, EXPRESSION,
                SEMICOLON, STATEMENT, ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE }, ControlFlow::forStatement);
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { KW_IF, ROUND_OPEN, EXPRESSION,
                ROUND_CLOSE, STATEMENT_FULL }, ControlFlow::ifStatement);
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { KW_IF, ROUND_OPEN, EXPRESSION,
                ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE }, ControlFlow::ifStatement);
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { KW_WHILE, ROUND_OPEN, EXPRESSION,
                ROUND_CLOSE, STATEMENT_FULL }, ControlFlow::whileStatement);
        generator.addVisitor(STATEMENT_FULL, new Symbol[] { KW_WHILE, ROUND_OPEN, EXPRESSION,
                ROUND_CLOSE, CURLY_OPEN, BLOCK, CURLY_CLOSE }, ControlFlow::whileStatement);
        generator.addVisitor(STATEMENT, new Symbol[] { KW_PRINT, EXPRESSION }, Expressions::print);
        generator.addVisitor(STATEMENT, new Symbol[] { VALUE_UNARY_OP }, (g, ast) -> {
            g.apply(ast[0]);
            g.pop();
        });
        generator.addVisitor(ASSIGNMENT, new Symbol[] { IDENTIFIER, ASSIGN, EXPRESSION }, Variables::assign);
        generator.addVisitor(ASSIGNMENT, new Symbol[] { IDENTIFIER, SQUARE_OPEN, EXPRESSION,
                SQUARE_CLOSE, ASSIGN, EXPRESSION }, ArrayExpressions::assign);
        generator.addVisitor(ARRAY_INIT, new Symbol[] { KW_VAR, SQUARE_OPEN, EXPRESSION,
                SQUARE_CLOSE, IDENTIFIER }, ArrayExpressions::init);
        generator.addVisitor(ARRAY_INIT, new Symbol[] { KW_VAR, SQUARE_OPEN, SQUARE_CLOSE, IDENTIFIER,
                ASSIGN, CURLY_OPEN, ARRAY_VALUE, CURLY_CLOSE }, ArrayExpressions::initWithValues);
        generator.addVisitor(EXPRESSION, new Symbol[] { EXPRESSION, LOGICAL_OR, VALUE_LOGICAL_OP }, ControlFlow::or);
        generator.addVisitor(VALUE_LOGICAL_OP, new Symbol[] { VALUE_LOGICAL_OP, LOGICAL_AND, VALUE_EQUALS_OP }, ControlFlow::and);
        generator.addVisitor(VALUE_EQUALS_OP, new Symbol[] { VALUE_EQUALS_OP, EQUALS, VALUE_COMP_OP }, ControlFlow::equals);
        generator.addVisitor(VALUE_EQUALS_OP, new Symbol[] { VALUE_EQUALS_OP, NEQUALS, VALUE_COMP_OP }, ControlFlow::notEquals);
        generator.addVisitor(VALUE_COMP_OP, new Symbol[] { VALUE_COMP_OP, LT, VALUE_PLUS_OP }, ControlFlow::lessThan);
        generator.addVisitor(VALUE_COMP_OP, new Symbol[] { VALUE_COMP_OP, GT, VALUE_PLUS_OP }, ControlFlow::greaterThan);
        generator.addVisitor(VALUE_COMP_OP, new Symbol[] { VALUE_COMP_OP, LTEQUALS, VALUE_PLUS_OP }, ControlFlow::lessThanEq);
        generator.addVisitor(VALUE_COMP_OP, new Symbol[] { VALUE_COMP_OP, GTEQUALS, VALUE_PLUS_OP }, ControlFlow::greaterThanEq);
        generator.addVisitor(VALUE_TIMES_OP, new Symbol[] { VALUE_TIMES_OP, TIMES, VALUE_SIGNED }, Expressions::mul);
        generator.addVisitor(VALUE_TIMES_OP, new Symbol[] { VALUE_TIMES_OP, DIVIDE, VALUE_SIGNED }, Expressions::div);
        generator.addVisitor(VALUE_TIMES_OP, new Symbol[] { VALUE_TIMES_OP, MODULO, VALUE_SIGNED }, Expressions::rem);
        generator.addVisitor(VALUE_PLUS_OP, new Symbol[] { VALUE_PLUS_OP, PLUS, VALUE_TIMES_OP }, Expressions::add);
        generator.addVisitor(VALUE_PLUS_OP, new Symbol[] { VALUE_PLUS_OP, MINUS, VALUE_TIMES_OP }, Expressions::sub);
        generator.addVisitor(VALUE_SIGNED, new Symbol[] { PLUS, VALUE_SIGNED }, (g, ast) -> g.apply(ast[1]));
        generator.addVisitor(VALUE_SIGNED, new Symbol[] { MINUS, VALUE_SIGNED }, Expressions::negate);
        generator.addVisitor(VALUE_UNARY_OP, new Symbol[] { VALUE, INCREMENT }, Expressions::postIncrement);
        generator.addVisitor(VALUE_UNARY_OP, new Symbol[] { VALUE, DECREMENT }, Expressions::postDecrement);
        generator.addVisitor(VALUE_UNARY_OP, new Symbol[] { INCREMENT, VALUE }, Expressions::preIncrement);
        generator.addVisitor(VALUE_UNARY_OP, new Symbol[] { DECREMENT, VALUE }, Expressions::preDecrement);
        generator.addVisitor(VALUE_UNARY_OP, new Symbol[] { KW_LENGTH, ROUND_OPEN, IDENTIFIER, ROUND_CLOSE }, Expressions::length);
        generator.addVisitor(VALUE, new Symbol[] { IDENTIFIER }, (g, ast) -> g.push(ast[0].getText()));
        generator.addVisitor(VALUE, new Symbol[] { NUMBER }, (g, ast) -> g.pushFloat(Float.parseFloat(ast[0].getText())));
        generator.addVisitor(VALUE, new Symbol[] { ROUND_OPEN, EXPRESSION, ROUND_CLOSE }, (g, ast) -> g.apply(ast[1]));
        generator.addVisitor(VALUE, new Symbol[] { IDENTIFIER, SQUARE_OPEN, EXPRESSION, SQUARE_CLOSE }, ArrayExpressions::access);

        return generator;
    }
}
