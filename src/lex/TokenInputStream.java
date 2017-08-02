package lex;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class TokenInputStream extends InputStream
{
    private Queue<String> tokens = new LinkedList<>();

    public TokenInputStream(String code) {

    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}
