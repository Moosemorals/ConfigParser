/*
 * The MIT License
 *
 * Copyright 2017 Osric Wilkinson (osric@fluffypeople.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.moosemorals.configparser;

import com.moosemorals.configparser.parsers.AbstractParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SourceFile {

    private static File root = new File("/");
    private final Logger log = LoggerFactory.getLogger(SourceFile.class);
    private final static int PUSHBACK_BUFFER_SIZE = 8 * 1024; // Probably overkill.
    private final StreamTokenizer t;
    private final ConfigFileReader in;
    private final String target;
    private int lines = 0;

    public static void setRoot(File base) {
        SourceFile.root = base;
    }

    public SourceFile(String target) throws FileNotFoundException {
        this.target = target;
        in = new ConfigFileReader(new PushbackReader(new FileReader(new File(root, target)), PUSHBACK_BUFFER_SIZE));
        t = new StreamTokenizer(in);
        setupTokenizer();
    }

    private void setupTokenizer() {
        t.resetSyntax();
        t.eolIsSignificant(true);
        t.slashSlashComments(false);
        t.slashStarComments(false);
        t.quoteChar(AbstractParser.QUOTE_CHAR);
        t.quoteChar(AbstractParser.DOUBLE_QUOTE_CHAR);
        t.wordChars('a', 'z');
        t.wordChars('A', 'Z');
        t.wordChars('0', '9');
        t.wordChars('-', '-');
        t.wordChars('_', '_');
        t.whitespaceChars('\u0000', '\u0020');
    }

    public int nextToken() throws IOException {
        return t.nextToken();
    }

    public int currentToken() {
        return t.ttype;
    }

    public String getTokenString() {
        return t.sval;
    }

    public double tokenDouble() {
        return t.nval;
    }

    public void pushBack() {
        t.pushBack();
    }

    public String readLine() throws IOException {
        lines += 1;

        StringBuilder line = new StringBuilder();
        int c;
        boolean inComment = false;
        while ((c = in.read()) != -1) {
            if (c == AbstractParser.COMMENT_CHAR) {
                inComment = true;
            } else if (c != '\n') {
                if (!inComment) {
                    line.appendCodePoint(c);
                }
            } else {
                break;
            }
        }

        if (c == -1) {
            return null;
        }

        return line.toString();
    }

    public void unreadLine(String line) throws IOException {
        lines -= 1;
        line = line + '\n';
        in.unread(line.toCharArray());
    }

    @Override
    public String toString() {
        return "KconfigFile{" + "t=" + t + ", target=" + target + '}';
    }

    public int getLineNumber() {
        if (t != null) {
            return t.lineno() + lines;
        } else {
            return 0;
        }
    }

    public Location getLocation() {
        return new Location(target, getLineNumber());        
    }

    /**
     *
     * @author Osric Wilkinson (osric@fluffypeople.com)
     */
    public static class Location {

        private final Logger log = LoggerFactory.getLogger(Location.class);
        private final String file;
        private final String line;
        
        public Location(String file, int line) {
            this.file = file;
            this.line = String.format("%d", line);
        }

        public String getFile() {
            return file;
        }

        public String getLine() {
            return line;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s", file, line);
        }
        
    }
}
