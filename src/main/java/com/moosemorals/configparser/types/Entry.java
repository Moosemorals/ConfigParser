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

package com.moosemorals.configparser.types;

import com.moosemorals.configparser.XMLable;
import com.moosemorals.configparser.values.Default;
import com.moosemorals.configparser.values.Prompt;
import java.util.LinkedList;
import java.util.List;

public abstract class Entry implements XMLable {

    protected final String symbol;
    protected final List<Default> defaults;
    protected final List<Condition> depends;
    protected String type;
    protected String value;
    protected Prompt prompt;
    protected String help;

    public Entry(String symbol) {
        this.symbol = symbol;
        this.defaults = new LinkedList<>();
        this.depends = new LinkedList<>();
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

    public List<Condition> getDepends() {
        return depends;
    }

    public void addDepends(Condition condition) {
        depends.add(condition);
    }

    public void addDefault(Default def) {
        defaults.add(def);
    }

    public List<Default> getDefaults() {
        return defaults;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append("[").append(symbol);
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
        if (!depends.isEmpty()) {
            result.append(" dep: ");
            for (int i = 0; i < depends.size(); i += 1) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append(depends.get(i));
            }
        }
        if (!defaults.isEmpty()) {
            result.append(" def: ");
            for (int i = 0; i < defaults.size(); i += 1) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append(defaults.get(i));
            }
        }

        result.append("]");
        return result.toString();
    }
       
}
