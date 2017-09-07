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
import java.io.StringReader;
import org.junit.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class ConfigReaderNGTest {

    public ConfigReaderNGTest() {
    }

    @Test
    public void test_basicRead() throws IOException {
        String test = "Hello, world";
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        for (int i = 0; i < test.length(); i += 1) {
            assertEquals(in.read(), test.charAt(i));
        }
        assertEquals(in.read(), -1);
    }

    @Test
    public void test_ReadNewline() throws IOException {
        String test = "\n";
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        assertEquals(in.read(), '\n');
    }

    @Test
    public void test_ReadArray() throws IOException {
        String test = "Hello, world";
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        char[] result = new char[test.length()];
        int read = in.read(result, 0, test.length());

        Assert.assertArrayEquals(test.toCharArray(), result);
        assertEquals(read, test.length());
    }

    @Test
    public void test_ReadArrayLong() throws IOException {
        String test = "Hello, world";
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        char[] result = new char[test.length() + 4];
        int read = in.read(result, 0, result.length);

        assertEquals(read, test.length());
        for (int i = 0; i < test.length(); i += 1) {
            assertEquals(result[i], test.toCharArray()[i]);
        }

        for (int i = test.length(); i < result.length; i += 1) {
            assertEquals(result[i], '\u0000');
        }
    }

    @Test
    public void test_ReadComment() throws IOException {
        String before = "Hello ";
        String after = "# world";
        String test = before + after;
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        char[] result = new char[before.length()];
        int read = in.read(result, 0, result.length);

        assertEquals(read, before.length());
        for (int i = 0; i < before.length(); i += 1) {
            assertEquals(result[i], before.toCharArray()[i]);
        }
    }

    @Test
    public void test_ReadCommentAndNewline() throws IOException {
        String test = "a#b\nc";
        String expected = "a\nc";
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        char[] result = new char[test.length()];
        int read = in.read(result, 0, result.length);

        assertEquals(read, expected.length());
        for (int i = 0; i < expected.length(); i += 1) {
            assertEquals(expected.toCharArray()[i], result[i]);
        }
    }

    @Test
    public void test_Continuation() throws IOException {
        String test = "a\\\nb";
        String expected = "ab";
        ConfigReader in = new ConfigReader(new PushbackReader(new StringReader(test)));

        char[] result = new char[test.length()];
        int read = in.read(result, 0, result.length);

        assertEquals(read, expected.length());
        for (int i = 0; i < expected.length(); i += 1) {
            assertEquals(expected.toCharArray()[i], result[i]);
        }
    }
}
