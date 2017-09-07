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

import com.moosemorals.configparser.values.Select;
import com.moosemorals.configparser.values.Range;
import com.moosemorals.configparser.values.Prompt;
import com.moosemorals.configparser.values.Default;
import com.moosemorals.configparser.values.Imply;
import java.io.IOException;
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class EntryParser extends AbstractParser {

    private final Logger log = LoggerFactory.getLogger(EntryParser.class);


    public EntryParser(Environment e) {
        super(e);
    }

    private boolean isStartWord(String word) {
        return word.equals("config") || word.equals("menuconfig") || word.equals("choice") || word.equals("menu");
    }

    public Entry parse(SourceFile t) throws IOException {

        Entry e;
        if (!isStartWord(t.getTokenString())) {
            throw new ParseError(t, "Must be called on start word");
        }

        if (t.nextToken() != StreamTokenizer.TT_WORD) {
            throw new ParseError(t, "Expecting word to follow 'config'");
        }

        e = new Entry(t.getTokenString());

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
                        case "implies":
                            readImply(t, e);
                            break;
                        case "help":
                        case "---help---":
                            readHelp(t, e);
                            break;
                        case "option":
                            readOption(t, e);
                            break;
                        case "range":
                            readRange(t, e);
                            break;
                        case "select":
                            readSelect(t, e);
                            break;

                        default:
                            log.debug("skipping {}", skip(t));
                            //skip(t);
                            break;
                    }
                    break;
            }
        }
    }
}
