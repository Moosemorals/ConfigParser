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
public class EntryParser extends AbstractParser {

    private final Logger log = LoggerFactory.getLogger(EntryParser.class);

    private final Environment environment;

    public EntryParser(Environment e) {
        this.environment = e;
    }

    private boolean isStartWord(String word) {
        return word.equals("config") || word.equals("menuconfig") || word.equals("choice") || word.equals("menu");
    }

    public Entry parse(KconfigFile t) throws IOException {

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
                        case "option":
                            readOption(t, e);
                            break;
                        case "help":
                        case "---help---":
                            readHelp(t, e);
                            break;
                        case "default":
                            readDefault(t, e);
                            break;
                        case "depends":
                            readDepends(t, e);
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

    private void readPrompt(KconfigFile t, Entry e) throws IOException {        
        int token = t.nextToken();
        if (token == QUOTE_CHAR) {
            String prompt = t.getTokenString();

            token = t.nextToken();
            Condition c = null;
            if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
                c = new ConditionParser().parse(t);
            }

            e.setPrompt(new Prompt(prompt, c));
        }        
    }

    private void readType(KconfigFile t, Entry e) throws IOException {        
        e.setType(t.getTokenString());
        int token = t.nextToken();
        if (token == QUOTE_CHAR) {
            t.pushBack();
            readPrompt(t, e);
        }
    }

    private void readOption(KconfigFile t, Entry e) throws IOException {
        int token = t.nextToken();
        if (token != StreamTokenizer.TT_WORD) {
            throw new ParseError(t, "Option without word");
        }

        switch (t.getTokenString()) {
            case "env":
                token = t.nextToken();
                if (token != '=') {
                    throw new ParseError(t, "option env needs an '='");
                }
                token = t.nextToken();
                if (token != QUOTE_CHAR) {
                    throw new ParseError(t, "option env needs a quoted string");
                }

                String envName = t.getTokenString();
                if (environment.contains(envName)) {
                    e.setValue(environment.get(envName));
                }

                break;
            default:
                // ignore options                
                skip(t);
                t.nextToken(); // throw away EOL
                break;
        }
    }

    private void readHelp(KconfigFile t, Entry e) throws IOException {
        if (t.currentToken() != StreamTokenizer.TT_EOL) {
            skip(t);
        }
        t.nextToken();

        String line = t.readLine();
        if (line == null) {
            // EOF
            return;
        }
        while (line.length() == 0) {
            line = t.readLine();
            if (line == null) {
                // EOF
                return;
            }
        }
        line = line.replace("\t", "        ");

        int indent = line.indexOf(line.trim());

        if (indent == 0) {
            // No help
            return;
        }

        StringBuilder help = new StringBuilder();
        help.append(line);

        while ((line = t.readLine()) != null) {
            int offset = 0;

            if (line.length() > 0) {
                line = line.replace("\t", "        ");
                offset = line.indexOf(line.trim());
            }

            if (offset < indent) {
                if (line.length() > 0) {
                    t.unreadLine(line);
                    break;
                } else {
                    help.append("\n");
                }
            } else {
                help.append(line);
            }
        }
        e.setHelp(help.toString());
        return;
    }

    private void readDepends(KconfigFile t, Entry e) throws IOException {
        int token = t.nextToken();
        if (token != StreamTokenizer.TT_WORD || !t.getTokenString().equals("on")) {
            throw new ParseError(t, "'on' must follow depends");
        }

        e.addDepends(readExpression(t));
    }

    private void readDefault(KconfigFile t, Entry e) throws IOException {
        String def = readExpression(t);

        int token = t.nextToken();
        Condition c = null;
        if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
            c = new ConditionParser().parse(t);
        }

        e.addDefault(new Default(def, c));
    }

    private void readSelect(KconfigFile t, Entry e) throws IOException {
        String select = readExpression(t);

        int token = t.nextToken();
        Condition c = null;
        if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
            c = new ConditionParser().parse(t);
        }

        e.addSelect(new Select(select, c));
    }

    private void readTypeWithDef(KconfigFile t, Entry e) throws IOException {
        String type = t.getTokenString();
        
        e.setType(type.substring("dev_".length()));
        readDefault(t, e);        
    }

}
