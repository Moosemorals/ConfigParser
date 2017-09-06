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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Entry {

    private static final Logger log = LoggerFactory.getLogger(Entry.class);

    private final String symbol;
    private String type;
    private String value;
    private String prompt;
    private String help;
    private String depends;

    Entry(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public boolean hasValue() {
        return value != null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder()
                .append("[")
                .append(symbol);

        if (type != null) {
            result.append("(").append(type).append(")");
        }

        if (value != null) {
            result.append("=").append(value);
        }
        
        if (prompt != null) {
            result.append(" '").append(prompt).append("'");
        }
        
        if (help != null) {
            result.append(" (help: ").append(help.length()).append(")");
        }

        if (depends != null) {
            result.append(" (depends: ").append(depends).append(")");
        }
        
        result.append("]");

        return result.toString();

    }

}
