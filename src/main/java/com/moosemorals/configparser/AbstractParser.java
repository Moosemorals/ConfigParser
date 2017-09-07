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
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public abstract class AbstractParser {

    public static final int COMMENT_CHAR = '#';
    public static final int QUOTE_CHAR = '"';

    private final Logger log = LoggerFactory.getLogger(AbstractParser.class);

    /**
     * Skip to the end of the line (or file) leaving the EOL/EOF on
     * the stack.
     * @param t
     * @return String skipped text
     * @throws IOException 
     */
    protected String skip(SourceFile t) throws IOException {
        StringBuilder skipped = new StringBuilder();
        while (true) {
            int token = t.nextToken();
            if (token == StreamTokenizer.TT_EOL || token == StreamTokenizer.TT_EOF) {
                t.pushBack();
                return skipped.toString();
            } else {
                if (token == StreamTokenizer.TT_WORD) {
                    skipped.append(t.getTokenString()).append(" ");
                } else {
                    skipped.appendCodePoint(token);
                }
            }
        }
    }

    protected String readExpression(SourceFile t) throws IOException {
        StringBuilder result = new StringBuilder();

        String symbol;
        while (true) {
            int token = t.nextToken();
            switch (token) {
                case '(':
                    result.append("(");
                    result.append(readExpression(t));
                    break;
                case ')':
                    result.append(")");
                    break;
                case '!':
                    token = t.nextToken();
                    if (token == '=') {
                        result.append("!=");                        
                    } else {
                        t.pushBack();
                        result.append("!");
                    }                        
                    result.append(readExpression(t));
                    break;
                case '=':
                    result.append("=");
                    result.append(readExpression(t));
                    break;
                case StreamTokenizer.TT_WORD:
                    symbol = t.getTokenString();
                    if (symbol.equals("if")) {
                        t.pushBack();
                        return result.toString();
                    }
                    result.append(symbol);
                    break;
                case QUOTE_CHAR:                    
                    result.append('"');
                    result.append(t.getTokenString());
                    result.append('"');
                    break;
                case '&':
                    t.nextToken(); // should be '&';
                    result.append("&&");
                    result.append(readExpression(t));
                    break;
                case '|':
                    t.nextToken();
                    result.append("||");
                    result.append(readExpression(t));
                    break;
                case StreamTokenizer.TT_EOL:
                default:
                    t.pushBack();
                    return result.toString();
            }
        }
    }

}
