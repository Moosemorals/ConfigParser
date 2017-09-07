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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class ConfigReader extends Reader {

    private final Logger log = LoggerFactory.getLogger(ConfigReader.class);

    private final PushbackReader in;

    public ConfigReader(PushbackReader in) {
        super();
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int c = in.read();

        if (c == '#') {
            while (c != '\n') {
                c = in.read();
            }
        } else if (c == '\\') {
            c = in.read();
            if (c == '\n') {
                c = in.read();
                return c;
            } else {
                in.unread(c);
            }
            return '\\';
        }
        return c;
    }

    @Override
    public int read(char[] chars, int offset, int len) throws IOException {
        int c, read = 0;
        for (int i = 0; i < len; i += 1) {
            c = read();
            if (c == -1) {
                return read;
            } else {
                chars[i + offset] = (char)c;
                read += 1;
            }
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public void unread(int i) throws IOException {
        in.unread(i);
    }

    public void unread(char[] chars, int i, int i1) throws IOException {
        in.unread(chars, i, i1);
    }

    public void unread(char[] chars) throws IOException {
        in.unread(chars);
    }
    
    

}
