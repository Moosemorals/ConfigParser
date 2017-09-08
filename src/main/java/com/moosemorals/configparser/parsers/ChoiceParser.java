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

import com.moosemorals.configparser.types.Choice;
import com.moosemorals.configparser.Environment;
import com.moosemorals.configparser.ParseError;
import com.moosemorals.configparser.SourceFile;
import com.moosemorals.configparser.types.Condition;
import java.io.IOException;
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class ChoiceParser extends AbstractParser {

    private final Logger log = LoggerFactory.getLogger(ChoiceParser.class);

    public ChoiceParser(MenuParser parentParser, Environment e) {
        super(parentParser, e);
    }

    public Choice parse(SourceFile t) throws IOException {        
        Choice c;
        if (!"choice".equals(t.getTokenString())) {
            throw new ParseError(t, "Must be called on choice");
        }

        String symbol = null;
        if (t.nextToken() == StreamTokenizer.TT_WORD) {
            symbol = t.getTokenString();
        } else {
            t.pushBack();
        }

        c = new Choice(symbol);

        while (true) {
            int token = t.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                    t.pushBack();
                    return c;
                case StreamTokenizer.TT_WORD:
                    switch (t.getTokenString()) {
                        case "menuconfig":
                        case "config":
                            c.addEntry(parentMenu.applyIfStack(new ConfigParser(parentMenu, environment).parse(t)));
                            break;
                        case "endchoice":                            
                            t.pushBack();
                            return c;
                        case "comment":
                            c.addEntry(parentMenu.applyIfStack(new CommentParser(parentMenu, environment).parse(t)));
                            break;
                        case "source":
                            t = parentMenu.source(t);
                            break;
                        case "if":
                            parentMenu.pushIfStack(new Condition(readExpression(t)));
                            break;
                        case "endif":
                            parentMenu.popIfStack();
                            break;
                        case "string":
                        case "bool":
                        case "tristate":
                        case "int":
                        case "hex":
                            readType(t, c);
                            break;
                        case "def_bool":
                        case "def_tristate":
                            readTypeWithDef(t, c);
                            break;
                        case "default":
                            readDefault(t, c);
                            break;
                        case "depends":
                            readDepends(t, c);
                            break;
                        case "help":
                        case "---help---":
                            readHelp(t, c);
                            break;
                        case "option":
                            readOption(t, c);
                            break;
                        case "prompt":
                            readPrompt(t, c);
                            break;

                        default:
                            String skipped = skip(t);
                            if (skipped.length() > 0) {
                                log.debug("Choice {}", c);
                                throw new ParseError(t, "Skipping stuff [" + skipped + "]");
                            }
                            //skip(t);
                            break;
                    }
                    break;
            }
        }
    }

}
