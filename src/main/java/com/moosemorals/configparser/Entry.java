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
import com.moosemorals.configparser.values.Prompt;
import com.moosemorals.configparser.values.Range;
import com.moosemorals.configparser.values.Default;
import com.moosemorals.configparser.values.Imply;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Entry {

    private static final Logger log = LoggerFactory.getLogger(Entry.class);

    private final String symbol;
    private final List<Default> defaults;
    private final List<Select> selects;
    private final List<Imply> implies;
    private final List<Range> ranges;
    private String type;
    private String value;
    private Prompt prompt;
    private String help;
    private String depends;

    Entry(String symbol) {
        this.symbol = symbol;
        this.defaults = new LinkedList<>();
        this.selects = new LinkedList<>();
        this.implies = new LinkedList<>();
        this.ranges = new LinkedList<>();
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

    public Prompt getPrompt() {
        return prompt;
    }

    public void setPrompt(Prompt prompt) {
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

    public void addDepends(String depends) {
        if (this.depends == null) {
            this.depends = depends;
        } else {
            this.depends += " && " + depends;
        }
    }

    public void addDefault(Default def) {
        defaults.add(def);
    }

    public List<Default> getDefaults() {
        return defaults;
    }

    public void addSelect(Select select) {
        selects.add(select);
    }

    public List<Select> getSelects() {
        return selects;
    }

    public void addImplies(Imply select) {
        implies.add(select);
    }

    public List<Imply> getImplies() {
        return implies;
    }
    
    public void addRange(Range range) {
        ranges.add(range);
    }
    
    public List<Range> getRanges() {
        return ranges;
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
        
        if (!defaults.isEmpty()) {
            result.append(" (def: ");
            for (int i = 0; i < defaults.size(); i += 1) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append(defaults.get(i));
            }
        }

        if (!ranges.isEmpty()) {
            result.append(" (range: ");
            for (int i = 0; i < ranges.size(); i += 1) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append(ranges.get(i));
            }
        }
        
        
        result.append("]");

        return result.toString();

    }

}
