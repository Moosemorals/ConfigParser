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

package com.moosemorals.configparser.parsers;

import com.moosemorals.configparser.types.Condition;
import com.moosemorals.configparser.Environment;
import com.moosemorals.configparser.ParseError;
import com.moosemorals.configparser.SourceFile;
import java.io.IOException;
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class ConditionParser extends BaseParser {

    private final Logger log = LoggerFactory.getLogger(ConditionParser.class);

    public ConditionParser(MenuParser parentParser, Environment e) {
        super(parentParser, e);
    }

    Condition parse(SourceFile t) throws IOException {
        if (t.currentToken() != StreamTokenizer.TT_WORD && !t.getTokenString().equals("if")) {
            throw new ParseError(t, "Must start parsing condition on an 'if'");
        }

        return new Condition(readExpression(t));
    }

}
