package test;

import ast.AST;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/8/2017. 💩
 *
 * An idiot once said: "Every toy programming language should have its very own toy virtual machine".
 * That idiot is me, and this was said in the context of working on this project.
 *
 * this class is just for testing some concepts of code generation.
 * everything here will be deleted, as KLMN won't have a specialized virtual Machine
 */
public class KVM
{
    private static Map<String, Command> commands = new HashMap<>();
    private static Map<String, Double> vars = new HashMap<>();
    private static Stack<Double> stack = new Stack<>();

    static {
        commands.put("print", (String... args) -> {
            for (int i = 0; i < args.length - 1; i++) System.out.print(parseArg(args[i]) + ", ");
            System.out.println(parseArg(args[args.length - 1]));
        });
        commands.put("put", (String... args) -> vars.put(args[0], parseArg(args[1])));
        commands.put("add", (String... args) -> vars.put(args[0], vars.get(args[0]) + parseArg(args[1])));
        commands.put("sub", (String... args) -> vars.put(args[0], vars.get(args[0]) - parseArg(args[1])));
        commands.put("mul", (String... args) -> vars.put(args[0], vars.get(args[0]) * parseArg(args[1])));
        commands.put("div", (String... args) -> vars.put(args[0], vars.get(args[0]) / parseArg(args[1])));
        commands.put("del", (String... args) -> vars.remove(args[0]));
        commands.put("push", (String... args) -> stack.push(parseArg(args[0])));
        commands.put("pop", (String... args) -> {
            double i = stack.pop();
            if (args.length > 0) vars.put(args[0], i);
        });
    }

    private static double parseArg(String arg) {
        if (arg.startsWith("#")) return vars.get(arg);
        return Integer.parseInt(arg);
    }

    private KVM() {}

    public static void run(String code) {
        for (String line : code.split("\n")) {
            String[] words = line.split(" ");
            try { commands.get(words[0]).run(Arrays.copyOfRange(words, 1, words.length)); }
            catch (Exception e) { throw new RuntimeException("KVM Error", e); }
        }
    }

    private interface Command { void run(String... args); }
}
