package lang;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/21/2017.
 */
class ReadingException extends RuntimeException {
    ReadingException(char c, int i) { super("Unsupported Character '" + c + "' at Location " + i + " " + (int)c); }
}
