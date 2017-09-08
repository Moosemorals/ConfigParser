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
import com.moosemorals.configparser.types.Comment;
import com.moosemorals.configparser.types.Condition;
import com.moosemorals.configparser.types.Config;
import com.moosemorals.configparser.Environment;
import com.moosemorals.configparser.ParseError;
import com.moosemorals.configparser.types.Menu;
import com.moosemorals.configparser.SourceFile;
import com.moosemorals.configparser.types.Entry;
import com.moosemorals.configparser.values.Prompt;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuParser extends AbstractParser {

    public static final Logger log = LoggerFactory.getLogger(MenuParser.class);

    private final Deque<SourceFile> fileStack;
    private final Deque<Condition> ifStack;

    public MenuParser(MenuParser parentParser, Environment environment) {
        super(parentParser, environment);
        if (parentParser == null) {
            fileStack = new LinkedList<>();
            ifStack = new LinkedList<>();
        } else {
            fileStack = parentParser.fileStack;
            ifStack = parentParser.ifStack;
        }
    }

    String replaceSymbols(String original) {
        Pattern p = Pattern.compile("\\$([A-Za-z_]+)");
        Matcher m = p.matcher(original);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String symbol = m.group(1);
            if (environment.contains(symbol)) {
                m.appendReplacement(sb, environment.get(symbol));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private SourceFile source(SourceFile current, String target) throws IOException {
        try {
            SourceFile t = new SourceFile(target);
            fileStack.push(t);
            return t;
        } catch (FileNotFoundException ex) {
            if (current != null) {
            log.warn("at {}: Can't find source {}, skipping", current.getLocation(), target);
            return current;
            } else {
                throw new IOException("Can't find top level file " + target);
            }
        }
    }

    public SourceFile source(SourceFile t) throws IOException {
        String target;
        int token = t.nextToken();
        if (token == DOUBLE_QUOTE_CHAR || token == QUOTE_CHAR) {
            target = replaceSymbols(t.getTokenString());
        } else {
            StringBuilder result = new StringBuilder();
            OUTER:
            while (true) {
                switch (token) {
                    case StreamTokenizer.TT_EOL:
                        target = result.toString();
                        t.pushBack();
                        break OUTER;
                    case StreamTokenizer.TT_WORD:
                        result.append(t.getTokenString());
                        break;
                    default:
                        result.appendCodePoint(token);
                        break;
                }
                token = t.nextToken();
            }
        }

        skip(t);
        return source(t, target);
    }

    public void pushIfStack(Condition c) {
        ifStack.push(c);
    }

    public void popIfStack() {
        ifStack.pop();
    }

    public Entry applyIfStack(Entry e) {
        ifStack.forEach(e::addDepends);
        return e;
    }

    public Menu parse(String target) throws IOException {
        return parse(source(null, target), null);
    }

    public Menu parse(SourceFile t, Menu parent) throws IOException {

        Menu m = new Menu(null);
        if (parent != null) {
            readPrompt(t, m);
        }

        while (true) {
            int token = t.nextToken();

            switch (token) {
                case StreamTokenizer.TT_EOF:
                    fileStack.pop();
                    if (fileStack.isEmpty()) {
                        log.debug("Completed parse");
                        return m;
                    } else {
                        t = fileStack.peek();
                    }
                    break;

                case StreamTokenizer.TT_WORD:
                    switch (t.getTokenString()) {
                        case "mainmenu":
                            t.nextToken();
                            if (parent == null) {
                                m.setPrompt(new Prompt(t.getTokenString(), null));
                                break;
                            }
                        case "config":
                        case "menuconfig":
                            m.addEntry(applyIfStack(new ConfigParser(this, environment).parse(t)));
                            break;
                        case "choice":
                            m.addEntry(applyIfStack(new ChoiceParser(this, environment).parse(t)));
                            break;
                        case "comment":
                            m.addEntry(applyIfStack(new CommentParser(this, environment).parse(t)));
                            break;
                        case "menu":
                            m.addEntry(applyIfStack(new MenuParser(this, environment).parse(t, m)));
                            break;
                        case "endmenu":
                            return m;
                        case "visible":
                            t.nextToken();
                            m.setVisibleIf(new ConditionParser(this, environment).parse(t));
                            break;
                        case "if":
                            pushIfStack(new Condition(readExpression(t)));
                            break;
                        case "endif":
                            popIfStack();
                            break;
                        case "source":
                            t = source(t);
                            break;
                        case "depends":
                            readDepends(t, m);
                            break;
                        default:
                            String skipped = skip(t);
                            if (skipped.length() > 0) {
                                log.debug("Menu {}", m);
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
