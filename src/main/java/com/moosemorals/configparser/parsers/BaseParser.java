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
import com.moosemorals.configparser.types.Entry;
import com.moosemorals.configparser.Environment;
import com.moosemorals.configparser.ParseError;
import com.moosemorals.configparser.SourceFile;
import com.moosemorals.configparser.values.Default;
import com.moosemorals.configparser.values.Prompt;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public abstract class BaseParser {

    private static final int TAB_WIDTH = 8;

    public static final int COMMENT_CHAR = '#';
    public static final int DOUBLE_QUOTE_CHAR = '"';
    public static final int QUOTE_CHAR = '\'';

    private final Logger log = LoggerFactory.getLogger(BaseParser.class);
    protected final Environment environment;
    protected final MenuParser parentMenu;

    public BaseParser(MenuParser parentParser, Environment e) {
        this.parentMenu = parentParser;
        this.environment = e;
    }

    /**
     * Skip to the end of the line (or file) leaving the EOL/EOF on the stack.
     *
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
                if (skipped.length() > 0) {
                    log.warn("Skipped {}", skipped.toString());
                }
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
                case DOUBLE_QUOTE_CHAR:

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

    protected void readDefault(SourceFile t, Entry e) throws IOException {
        String def = readExpression(t);
        int token = t.nextToken();
        Condition c = null;
        if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
            c = new ConditionParser(parentMenu, environment).parse(t);
        } else {
            t.pushBack();
        }
        e.addDefault(new Default(def, c));
    }

    protected void readDepends(SourceFile t, Entry e) throws IOException {
        int token = t.nextToken();
        if (token != StreamTokenizer.TT_WORD || !t.getTokenString().equals("on")) {
            throw new ParseError(t, "'on' must follow depends");
        }
        e.addDepends(new Condition(readExpression(t)));
    }

    protected void readHelp(SourceFile t, Entry e) throws IOException {
        while (t.currentToken() != StreamTokenizer.TT_EOL) {
            t.nextToken();
        }

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
        line = tabsToSpaces(line);
        int indent = line.indexOf(line.trim());
        if (indent == 0) {
            // No help
            return;
        }

        StringBuilder help = new StringBuilder();
        help.append(line.substring(indent)).append("\n");
        while ((line = t.readLine()) != null) {
            int offset = 0;
            if (line.matches("\\s+")) {
                line = "";
            }

            if (line.length() > 0) {
                line = tabsToSpaces(line);
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
                help.append(line.substring(indent)).append("\n");
            }
        }
        e.setHelp(help.toString());
        return;
    }

    protected void readOption(SourceFile t, Entry e) throws IOException {
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
                if (token != QUOTE_CHAR && token != DOUBLE_QUOTE_CHAR) {
                    throw new ParseError(t, "option env needs a quoted string");
                }
                String envName = t.getTokenString();
                if (environment.contains(envName)) {
                    e.setEnv(environment.get(envName));
                }
                break;
            default:
                // ignore options
                skip(t);
                t.nextToken(); // throw away EOL
                break;
        }
    }

    protected void readPrompt(SourceFile t, Entry e) throws IOException {
        int token = t.nextToken();
        if (token == QUOTE_CHAR || token == DOUBLE_QUOTE_CHAR) {
            String prompt = t.getTokenString();
            token = t.nextToken();
            Condition c = null;
            if (token == StreamTokenizer.TT_WORD && t.getTokenString().equals("if")) {
                c = new ConditionParser(parentMenu, environment).parse(t);
            }
            e.setPrompt(new Prompt(prompt, c));
        }
    }

    protected void readType(SourceFile t, Entry e) throws IOException {
        String type = t.getTokenString();
        if ("boolean".equals(type)) {
            type = "bool";
        }
        e.setType(type);
        int token = t.nextToken();
        if (token == QUOTE_CHAR || token == DOUBLE_QUOTE_CHAR) {
            t.pushBack();
            readPrompt(t, e);
        }
    }

    protected void readTypeWithDef(SourceFile t, Entry e) throws IOException {
        String type = t.getTokenString();
        e.setType(type.substring("def_".length()));
        readDefault(t, e);
    }

    private static String tabsToSpaces(String original) {
        Pattern p = Pattern.compile("\\t");
        Matcher m = p.matcher(original);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int offset = m.start() % TAB_WIDTH;
            String spaces = String.join("", Collections.nCopies(TAB_WIDTH - offset, " "));
            m.appendReplacement(sb, spaces);
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
