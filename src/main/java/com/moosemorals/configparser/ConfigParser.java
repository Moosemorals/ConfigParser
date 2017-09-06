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

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigParser extends AbstractParser {

    public static final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    public static final int QUOTE_CHAR = '"';
    private static final int HASH_CHAR = '#';

    private final Deque<KconfigFile> fileStack;
    private final Environment environment;
    private final Map<String, Entry> entries;

    public ConfigParser(Environment environment) {
        this.environment = environment;
        this.entries = new HashMap<>();
        fileStack = new LinkedList<>();
    }

    String replaceSymbols(String original) {
        Pattern p = Pattern.compile("\\$([A-Za-z_]+)");
        Matcher m = p.matcher(original);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String symbol = m.group(1);
            if (entries.containsKey(symbol)) {
                Entry e = entries.get(symbol);
                if (e.hasValue()) {
                    m.appendReplacement(sb, e.getValue());
                }
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    void addEntry(Entry e) {
        entries.put(e.getSymbol(), e);
    }

    private KconfigFile source(File target) throws IOException {

        KconfigFile t = new KconfigFile(target);
        fileStack.push(t);
        log.debug("Source: {} Changing to {}", getDepth(), t);
        return t;
    }

    private KconfigFile source(KconfigFile t) throws IOException {
        String target;
        int token = t.nextToken();
        if (token == QUOTE_CHAR) {
            target = replaceSymbols(t.getTokenString());
        } else {
            StringBuilder result = new StringBuilder();
            OUTER: while (true) {
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

        log.debug("skip source  {}", skip(t));

        log.debug("Opening {} from {}:{}", target, t.getPath(), t.getLineNumber());
        return source(new File(Main.SOURCE_FOLDER, target));
    }

    private int getDepth() {
        return ((LinkedList) fileStack).size();
    }

    public void parse(File target) throws IOException {

        KconfigFile t = source(target);

        int token;
        OUTER:
        while (true) {
            token = t.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                    fileStack.pop();
                    if (fileStack.isEmpty()) {
                        log.debug("Completed parse");
                        break OUTER;
                    } else {                        
                        t = fileStack.peek();
                        log.debug("Back to {}", t.getPath());
                    }
                    break;

                case HASH_CHAR:
                    log.debug("skip comment  {}", skip(t));
                    break;
                case StreamTokenizer.TT_EOL:
                    // ignored
                    break;
                case StreamTokenizer.TT_WORD:
                    switch (t.getTokenString()) {
                        case "config":
                        case "menuconfig":
                            log.debug("Starting entry");
                            Entry e = new EntryParser(environment).parse(t);
                            log.debug("Entry complete {}", e);
                            addEntry(e);
                            break;
                        case "source":
                            t = source(t);
                            break;
                        case "if":
                            log.debug("skip if {}", skip(t));
                            break;
                        default:
                            log.debug("skip default {}", skip(t));
                            break;
                    }

                    break;
                default:

                    break;

            }
        }
    }

}
