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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class ConfigParser {

    public static final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    public static final int QUOTE_CHAR = '"';
    private static final int HASH_CHAR = '#';

    private final Deque<StreamTokenizer> tokenizerStack;
    private final Environment environment;

    private final Map<String, Entry> entries;

    public ConfigParser(Environment environment) {
        this.environment = environment;
        this.entries = new HashMap<>();
        tokenizerStack = new LinkedList<>();
    }

    String replaceSymbols(String original) {
        log.debug("Looking for stuff in {}", original);
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

    private StreamTokenizer source(File target) throws IOException {
        StreamTokenizer t = new StreamTokenizer(new BufferedReader(new FileReader(target)));
        t.eolIsSignificant(true);
        t.slashSlashComments(false);
        t.slashStarComments(false);
        t.quoteChar(QUOTE_CHAR);

        log.info("Changing source to {}", target);
        tokenizerStack.push(t);
        return t;
    }

    private StreamTokenizer source(StreamTokenizer t) throws IOException {

        int token = t.nextToken();
        if (token != QUOTE_CHAR) {
            throw new IllegalStateException("Source without quoted string");
        }

        String target = replaceSymbols(t.sval);

        return source(new File(Main.SOURCE_FOLDER, target));
    }

    public void parse(File target) throws IOException {

        StreamTokenizer t = source(target);

        boolean inComment = false;
        Entry e;

        int token;
        OUTER:
        while (true) {
            token = t.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                    if (tokenizerStack.isEmpty()) {
                        break OUTER;
                    } else {                        
                        t = tokenizerStack.pop();
                    }
                case HASH_CHAR:
                    inComment = true;

                case StreamTokenizer.TT_WORD:
                    if (inComment) {
                        continue;
                    }

                    switch (t.sval) {
                        case "config":
                            e = new EntryParser(environment).parse(t);
                            log.debug("Read entry {}", e);
                            addEntry(e);
                            break;
                        case "source":
                            log.debug("Reading source");
                            t = source(t);
                            break;
                        default:
                            log.debug("skipping {}", t.sval);
                            break;
                    }

                    break;
                case StreamTokenizer.TT_NUMBER:
                    log.debug("Skipping number {}", t.nval);
                    break;
                case StreamTokenizer.TT_EOL:
                    if (inComment) {
                        inComment = false;
                    }
                    break;
                default:
                    if (!inComment) {
                        log.warn(String.format("Unexpected Word: %c", (char) token));
                    }
                    break;

            }
        }
    }


}
