# KLMN Compiler Framework

### This framework allows parsing of SLR(1) grammar languages and compiling them into JVM bytecode
### Supported features to implement into your language:
* Arithmetic expressions
* Variables
* Control-flow statements
* Classes
* Functions
* Interoprablity with other JVM languages
* **The only limit is your imagination!** (and SLR(1) grammar rules) (and JVM limitations) 
# How to create a programming language using the KLMN© Compiler Framework:
_See the klmn package in the source code for a more detailed & complex example_

**In this example we will create a simple programming language that consists of mathematical expressions, and will support the following operators: `+-*/()`. Our compiler will recieve such an expression and will create a `.class` file that calculates the expression and prints the result.**

Example: `4 * (3 / 2 + 1)`
## Step 1: Terminal symbols and Tokenizer
Terminal Symbols are the 'words' of your language. Let's define some Terminals:
```java
Terminal PLUS = new Terminal("+"); // you can pass a name for each Terminal, for debugging purposes
Terminal MINUS = new Terminal("-");
Terminal TIMES = new Terminal("*");
Terminal DIVIDE = new Terminal("/");
Terminal PAREN_OPEN = new Terminal("(");
Terminal PAREN_CLOSE = new Terminal(")");
Terminal NUMBER = new Terminal("num");
```
Next, we create a Tokenizer. Tokens are parts of the compiled code, and each of them corresponds to a Terminal.
We need to add our Terminals to the Tokenizer, and specify how to match and generate a Token from the compiled code. 
```java
Tokenizer tokenizer = new Tokenizer();
tokenizer.addTerminal(PLUS, '+'); // a terminal that corresponds to a single character in the string
tokenizer.addTerminal(MINUS, '-');
tokenizer.addTerminal(TIMES, '*');
tokenizer.addTerminal(DIVIDE, '/');
tokenizer.addTerminal(PAREN_OPEN, '(').addTerminal(PAREN_CLOSE, ')'); // can chain call
tokenizer.addTerminal(NUMBER, (src, i) -> {   // a terminal that corresponds to any number in the string
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
tokenizer.ignore((src, i) -> { // ignore whitespace
    char c = src.charAt(i);
    if (c == ' ' || c == '\n' || c == '\t' || c == '\r') return c + "";
    return null;
});
```
Now the Tokenizer is able to take a String and convert it to a TokenStream.
```java
String code = new String(Files.readAllBytes(Paths.get(YOUR_SOURCE_PATH)));
TokenStream stream = tokenizer.tokenize(code);
```
## Step 2: Grammar and Parser
Next, we define the Grammar of our language using Symbols. Symbols are parts of the language, they can be literal (Terminals), or Sentences, Statements, Expressions, etc.
```java
Symbol ROOT = new Symbol("root");
Symbol TIMES_EXP = new Symbol("*expression");
Symbol PLUS_EXP = new Symbol("+expression");
Symbol VALUE = new Symbol("value");

ROOT.addProduction(PLUS_EXP);
PLUS_EXP.addProduction(PLUS_EXP, PLUS, TIMES_EXP);
PLUS_EXP.addProduction(PLUS_EXP, MINUS, TIMES_EXP);
PLUS_EXP.addProduction(TIMES_EXP); // maintain oop
TIMES_EXP.addProduction(TIMES_EXP, TIMES, VALUE);
TIMES_EXP.addProduction(TIMES_EXP, DIVIDE, VALUE);
TIMES_EXP.addProduction(VALUE);
VALUE.addProduction(PAREN_OPEN, PLUS_EXP, PAREN_CLOSE);
VALUE.addProduction(NUMBER); // could also be different things like variables and function calls in the future

Grammar grammar = new Grammar(ROOT);
```
Now we can create a parser for our grammar, and parse the TokenStream to get an AST (abstract syntax tree), a tree representation of the structure of our code:
```java
Parser parser = new Parser(grammar);
AST ast = parser.parse(stream);
```
## Step 3: JVM bytecode generation
After we have an AST, it's much easier to compile the code. Here we will use the CodeGenerator class which provides many utilities for generating bytecode, but theoretically you could use the AST to compile to different platforms.

First, we need to create a target ClassFile, and pass it to our new CodeGenerator:
```java
ClassFile cls = new ClassFile("test");
CodeGenerator generator = new CodeGenerator(cls);
```
For the CodeGenerator to work, we need to specify how every symbol production is compiled to bytecode. To do that we bind visitor functions for every production.
Semantic analysis and code optimization could also be done here.
```java
// the signature of a visitor function is (CodeGenerator, AST[]) -> void, the AST array correlates to the items of the production.
generator.addVisitor(ROOT, new Symbol[] { PLUS_EXP }, (g, asts) -> {
  g.addMethod("main", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, JVMType.VOID, JVMType.arrayType(JVMType.refType("java/lang/String"))); // add the main method to the class file for the JVM to run
  g.editMethod("main"); // following instructions will be added to this method
  
  // the PLUS_EXP AST will push a float value onto the stack. let's print it
  g.pushField("java/lang/System", "out", JVMType.refType("java.io.PrintStream"), true);
  g.apply(asts[0]); 
  g.call("java/io/PrintStream", "println", JVMType.VOID, JVMType.FLOAT);
  g.ret(); // add a return instruction
});
generator.addVisitor(PLUS_EXP, new Symbol[] { PLUS_EXP, PLUS, TIMES_EXP }, (g, asts) -> binaryOp(g, asts, Opcodes.FADD));
generator.addVisitor(PLUS_EXP, new Symbol[] { PLUS_EXP, MINUS, TIMES_EXP }, (g, asts) -> binaryOp(g, asts, Opcodes.FSUB));
generator.addVisitor(PLUS_EXP, new Symbol[] { TIMES_EXP }, (g, asts) -> g.apply(asts[0]));
generator.addVisitor(TIMES_EXP, new Symbol[] { TIMES_EXP, TIMES, VALUE }, (g, asts) -> binaryOp(g, asts, Opcodes.FMUL));
generator.addVisitor(TIMES_EXP, new Symbol[] { TIMES_EXP, DIVIDE, VALUE }, (g, asts) -> binaryOp(g, asts, Opcodes.FDIV));
generator.addVisitor(TIMES_EXP, new Symbol[] { VALUE }, (g, asts) -> g.apply(asts[0]));  // for productions with one child, the generator will do this automatically if a visitor is not present. I put this here for clarity.
generator.addVisitor(VALUE, new Symbol[] { PAREN_OPEN, PLUS_EXP, PAREN_CLOSE }, (g, asts) -> g.apply(asts[1]));
generator.addVisitor(VALUE, new Symbol[] { NUMBER }, (g, asts) -> g.pushFloat(Float.parseFloat(asts[0].getText())));
...
// helper function to generate arithmetic binary operator code
void binaryOp(CodeGenerator g, AST[] asts, byte opcode) {
  g.apply(asts[0]);
  g.apply(asts[2]);
  g.useOperator(opcode);
}
```
Here we pass the AST to the CodeGenerator:
```java
generator.apply(ast);
```
Now our ClassFile has the generated bytecode. We can use the `toByteArray()` method to save its data in a `.class` file:
```java
Path path = Paths.get("./test.class");
Files.write(path, cls.toByteArray());
```
Run the `.class` file:
```cmd
java -cp ./ test
```
In our example, we get `10`!
