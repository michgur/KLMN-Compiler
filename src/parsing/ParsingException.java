package parsing;

import lexing.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/21/2017.
 */
class ParsingException extends RuntimeException {
    ParsingException(Token t) { super("Unexpected " + t); }
}
