package test;

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
    // TODO: add jumps & conditions
    private static Map<String, Command> commands = new HashMap<>();
    private static Map<String, Integer> labels = new HashMap<>();
    private static Map<Integer, Double> vars = new HashMap<>();
    private static Stack<Double> stack = new Stack<>();
    private static int instruction = -1; // instruction pointer (<("0")> JUST LIKE ASM!!! <("0")>)

    static {
        commands.put("print", (String... args) -> {
            for (int i = 0; i < args.length - 1; i++) System.out.print(parseArg(args[i]) + ", ");
            System.out.println(parseArg(args[args.length - 1]));
        });
        commands.put("put", (String... args) -> setVar(args[0], parseArg(args[1])));
        commands.put("add", (String... args) -> setVar(args[0], getVar(args[0]) + parseArg(args[1])));
        commands.put("sub", (String... args) -> setVar(args[0], getVar(args[0]) - parseArg(args[1])));
        commands.put("rsub", (String... args) -> setVar(args[0], parseArg(args[1]) - getVar(args[0])));
        commands.put("mul", (String... args) -> setVar(args[0], getVar(args[0]) * parseArg(args[1])));
        commands.put("div", (String... args) -> setVar(args[0], getVar(args[0]) / parseArg(args[1])));
        commands.put("rdiv", (String... args) -> setVar(args[0], parseArg(args[1]) / getVar(args[0])));
        commands.put("del", (String... args) -> vars.remove(Integer.parseInt(args[0].substring(1))));
        commands.put("push", (String... args) -> stack.push(parseArg(args[0])));
        commands.put("pop", (String... args) -> {
            double i = stack.pop();
            if (args.length > 0) setVar(args[0], i);
        });
        commands.put("jmp", (String... args) -> instruction = labels.get(args[0]));
        commands.put("je", (String... args) -> { if (parseArg(args[0]) == parseArg(args[1])) instruction = labels.get(args[2]); });
        commands.put("jne", (String... args) -> { if (parseArg(args[0]) != parseArg(args[1])) instruction = labels.get(args[2]); });
        commands.put("jl", (String... args) -> { if (parseArg(args[0]) > parseArg(args[1])) instruction = labels.get(args[2]); });
        commands.put("jle", (String... args) -> { if (parseArg(args[0]) >= parseArg(args[1])) instruction = labels.get(args[2]); });
        commands.put("js", (String... args) -> { if (parseArg(args[0]) < parseArg(args[1])) instruction = labels.get(args[2]); });
        commands.put("jse", (String... args) -> { if (parseArg(args[0]) < parseArg(args[1])) instruction = labels.get(args[2]); });
        commands.put("eq", (String... args) -> setVar(args[0], parseArg(args[0]) == parseArg(args[1]) ? 1.0 : 0.0));
        commands.put("neq", (String... args) -> setVar(args[0], parseArg(args[0]) != parseArg(args[1]) ? 1.0 : 0.0));
        commands.put("lt", (String... args) -> setVar(args[0], parseArg(args[0]) < parseArg(args[1]) ? 1.0 : 0.0));
        commands.put("gt", (String... args) -> setVar(args[0], parseArg(args[0]) > parseArg(args[1]) ? 1.0 : 0.0));
        commands.put("leq", (String... args) -> setVar(args[0], parseArg(args[0]) <= parseArg(args[1]) ? 1.0 : 0.0));
        commands.put("geq", (String... args) -> setVar(args[0], parseArg(args[0]) >= parseArg(args[1]) ? 1.0 : 0.0));
    }

    private static double getVar(String arg) { return vars.get(Integer.parseInt(arg.substring(1))); }
    private static void setVar(String arg, double value) { vars.put(Integer.parseInt(arg.substring(1)), value); }
    private static double parseArg(String arg) {
        if (arg.startsWith("#")) return getVar(arg);
        return Integer.parseInt(arg);
    }

    private KVM() {}

    public static void run(String code) {
        System.out.println(">>> " + code.replace("\n", "\n>>> "));
        instruction = -1;
        String[] instructions = code.toLowerCase().split("\n");

        for (int i = 0; i < instructions.length; i++)
            if (instructions[i].startsWith(":")) labels.put(instructions[i].substring(1), i);
        while (++instruction < instructions.length) {
            if (instructions[instruction].startsWith(":")) continue;
            String[] words = instructions[instruction].split(" ");
            try { commands.get(words[0]).run(Arrays.copyOfRange(words, 1, words.length)); }
            catch (Exception e) { throw new RuntimeException("KVM Error", e); }
        }
    }

    private interface Command { void run(String... args); }
}
