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
public class EntryParser {

    private final Logger log = LoggerFactory.getLogger(EntryParser.class);
    
    private final Environment environment;
    
    public EntryParser(Environment e) {
        this.environment = e;
    }
        
    public Entry parse(StreamTokenizer t) throws IOException {

        Entry e;
        if (!t.sval.equals("config")) {
            throw new IllegalStateException("Must be called on config");
        }

        if (t.nextToken() != StreamTokenizer.TT_WORD) {
            throw new IllegalStateException("Expecting word to follow 'config'");
        }

        e = new Entry(t.sval);

        while (true) {
            int token = t.nextToken();

            switch (token) {
                case StreamTokenizer.TT_EOF:
                    log.debug("End of file");
                    t.pushBack();
                    return e;

                case StreamTokenizer.TT_EOL:
                    token = t.nextToken();
                    if (token == StreamTokenizer.TT_WORD) {
                        switch (t.sval) {
                            case "config":
                            case "menuconfig":
                            case "choice":
                            case "comment":
                            case "menu":
                            case "if":
                            case "source":
                                log.debug("End of entry");
                                t.pushBack();
                                return e;
                            case "string":
                            case "bool":
                            case "tristate":
                            case "int":
                            case "hex":
                                log.debug("Reading Type");
                                readType(t, e);
                                break;
                            case "option":
                                log.debug("Reading option");
                                readOption(t, e);
                                break;
                            default:
                                skip(t);
                                break;
                        }
                    }
                    // else
                    t.pushBack();

                    break;
            }
        }
    }
    
    private void skip(StreamTokenizer t) throws IOException {        
        while (true) {
            int token = t.nextToken();
            if (token == StreamTokenizer.TT_EOL && token == StreamTokenizer.TT_EOF) {
                t.pushBack();
                return;
            }            
        }        
    }
    
    private  void readType(StreamTokenizer t, Entry e) throws IOException {
        e.setType(t.sval);
        if (t.nextToken() == ConfigParser.QUOTE_CHAR) {
            e.setPrompt(t.sval);
        } else {
            t.pushBack();
        }
    }

    private  void readOption(StreamTokenizer t, Entry e) throws IOException {
        int token = t.nextToken();
        if (token != StreamTokenizer.TT_WORD) {
            throw new IllegalStateException("Option without word");
        }
        
        switch (t.sval) {
            case "env":
                token = t.nextToken();
                if (token != '=') {
                    throw new IllegalStateException("option env needs an '='");
                }
                token = t.nextToken();
                if (token != ConfigParser.QUOTE_CHAR) {
                    throw new IllegalStateException("option env needs a quoted string");
                }
                
                String envName = t.sval;
                log.debug("Looking for {} in environment", envName);
                
                if (environment.contains(envName)) {
                    e.setValue(environment.get(envName));
                }
                                
                break;
            default:
                // ignore options
                log.debug("ignoring option {}", t.sval);
                skip(t);
                break;
        }
        
    }

}
