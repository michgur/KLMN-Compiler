import jvm.Opcodes;
import klmn.KLMN;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main implements Opcodes
{
    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        String src = String.join("\n", Files.readAllLines(Paths.get(args[0])));
        KLMN.compile("TEMP", src);
    }
}
