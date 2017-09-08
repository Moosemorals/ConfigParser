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
import com.moosemorals.configparser.types.Config;
import com.moosemorals.configparser.Environment;
import com.moosemorals.configparser.ParseError;
import com.moosemorals.configparser.SourceFile;
import com.moosemorals.configparser.values.Imply;
import com.moosemorals.configparser.values.Range;
import com.moosemorals.configparser.values.Select;
import java.io.IOException;
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class ConfigParser extends AbstractParser {

    private final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    public ConfigParser(Environment e) {
        super(e);
    }

    public Config parse(SourceFile t) throws IOException {

        Config e;
        if (!("config".equals(t.getTokenString()) || "menuconfig".equals(t.getTokenString()))) {
            throw new ParseError(t, "Must be called on config");
        }

        if (t.nextToken() != StreamTokenizer.TT_WORD) {
            throw new ParseError(t, "Expecting word to follow 'config'");
        }

        e = new Config(t.getTokenString());

        while (true) {
            int token = t.nextToken();

            switch (token) {
                case StreamTokenizer.TT_EOF:
                    t.pushBack();
                    return e;
                case StreamTokenizer.TT_WORD:
                    switch (t.getTokenString()) {
                        case "config":
                        case "menuconfig":
                        case "choice":
                        case "endchoice":
                        case "comment":
                        case "menu":
                        case "endmenu":
                        case "if":
                        case "endif":
                        case "source":
                            t.pushBack();
                            return e;
                        case "string":
                        case "bool":
                        case "boolean":
                        case "tristate":
                        case "int":
                        case "hex":
                            readType(t, e);
                            break;
                        case "def_bool":
                        case "def_tristate":
                            readTypeWithDef(t, e);
                            break;
                        case "default":
                            readDefault(t, e);
                            break;
                        case "depends":
                            readDepends(t, e);
                            break;
                        case "imply":
                            readImply(t, e);
                            break;
                        case "help":
                        case "---help---":
                            readHelp(t, e);
                            break;
                        case "option":
                            readOption(t, e);
                            break;
                        case "prompt":
                            readPrompt(t, e);
                            break;
                        case "range":
                            readRange(t, e);
                            break;
                        case "select":
                            readSelect(t, e);
                            break;

                        default:                            
                            String skipped = skip(t);
                            if (skipped.length() > 0) {
                                log.debug("Entry {}", e);
                                throw new ParseError(t, "Skipping stuff [" + skipped + "]");
                            }                            
                            //skip(t);
                            break;
                    }
                    break;
            }
        }
    }

    protected void readRange(SourceFile t, Config conf) throws IOException {
        t.nextToken();
        String value1 = t.getTokenString();
        t.nextToken();
        String value2 = t.getTokenString();
        Condition c = null;
        int token = t.nextToken();
        if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
            c = new ConditionParser(environment).parse(t);
        }
        conf.addRange(new Range(value1, value2, c));
    }

    protected void readSelect(SourceFile t, Config conf) throws IOException {
        String select = readExpression(t);
        int token = t.nextToken();
        Condition c = null;
        if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
            c = new ConditionParser(environment).parse(t);
        }
        conf.addSelect(new Select(select, c));
    }

    protected void readImply(SourceFile t, Config conf) throws IOException {
        String imply = readExpression(t);
        int token = t.nextToken();
        Condition c = null;
        if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
            c = new ConditionParser(environment).parse(t);
        }
        conf.addImplies(new Imply(imply, c));
    }

}
