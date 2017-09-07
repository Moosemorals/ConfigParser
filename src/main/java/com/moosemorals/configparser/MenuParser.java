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

import com.moosemorals.configparser.values.Prompt;
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuParser extends AbstractParser {

    public static final Logger log = LoggerFactory.getLogger(MenuParser.class);

    private final Deque<SourceFile> fileStack;
    private final Deque<Condition> ifStack;

    public MenuParser(Environment environment) {
        super(environment);
        fileStack = new LinkedList<>();
        ifStack = new LinkedList<>();
    }

    private MenuParser(Environment environment, Deque<Condition> ifStack, Deque<SourceFile> fileStack) {
        super(environment);
        this.ifStack = ifStack;
        this.fileStack = fileStack;
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

    private SourceFile source(File target) throws IOException {

        SourceFile t = new SourceFile(target);
        fileStack.push(t);
        return t;
    }

    private SourceFile source(SourceFile t) throws IOException {
        String target;
        int token = t.nextToken();
        if (token == QUOTE_CHAR) {
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

        log.debug("Depth {}: Opening {} from {}:{}", getDepth(), target, t.getPath(), t.getLineNumber());
        return source(new File(Main.SOURCE_FOLDER, target));
    }

    private int getDepth() {
        return ((LinkedList) fileStack).size();
    }

    public Menu parse(File target) throws IOException {
        return parse(source(target), null);
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
                        log.debug("Back to {}", t.getPath());
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
                            Config config = new ConfigParser(environment).parse(t);
                            ifStack.forEach(config::addDepends);
                            m.addEntry(config);
                            break;
                        case "choice":
                            Choice choice = new ChoiceParser(environment).parse(t);
                            ifStack.forEach(choice::addDepends);
                            m.addEntry(choice);
                            break;
                        case "comment":
                            Comment comment = new CommentParser(environment).parse(t);
                            ifStack.forEach(comment::addDepends);
                            m.addEntry(comment);
                            break;
                        case "menu":
                            Menu menu = new MenuParser(environment, ifStack, fileStack).parse(t, m);
                            ifStack.forEach(menu::addDepends);
                            m.addEntry(menu);
                            break;
                        case "endmenu":
                            return m;
                        case "if":
                            ifStack.push(new Condition(readExpression(t)));
                            break;
                        case "endif":
                            ifStack.pop();
                            break;
                        case "source":
                            t = source(t);
                            break;
                        case "depends":
                            readDepends(t, m);
                            break;
                        default:
                            log.debug("menu skipping {}", skip(t));
                            //skip(t);
                            break;
                    }
                    break;
            }
        }

    }

}
