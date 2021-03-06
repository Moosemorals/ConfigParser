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

import com.moosemorals.configparser.SourceFile;
import com.moosemorals.configparser.SourceFile.Location;
import com.moosemorals.configparser.XML;
import com.moosemorals.configparser.XMLable;
import com.moosemorals.configparser.values.Default;
import com.moosemorals.configparser.values.Prompt;

import javax.xml.stream.XMLStreamException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Entry implements XMLable {

    protected final SourceFile.Location location;
    protected final String symbol;
    protected final List<Default> defaults;
    protected final List<Condition> depends;
    protected final List<String> options;
    protected String type;
    protected String env;
    protected String prompt;
    protected String help;

    public Entry(Location location, String symbol) {
        this.location = location;
        this.symbol = symbol;
        this.defaults = new LinkedList<>();
        this.depends = new LinkedList<>();
        this.options = new LinkedList<>();
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(Prompt prompt) {
        this.prompt = prompt.getValue();
        Condition condition = prompt.getCondition();
        if (condition != null) {
            addDepends(prompt.getCondition());
        }
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
        if (!depends.contains(condition)) {
            depends.add(condition);
        }
    }

    public void addDefault(Default def) {
        defaults.add(def);
    }

    public void addOption(String option) {
        options.add(option);
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
        if (env != null) {
            result.append("=").append(env);
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

    protected void toXML(XML xml, String typename, XMLGenerator xmlConsumer) throws XMLStreamException {
        xml.start(typename, "file", location.getFile(), "line", location.getLine());
        xml.add("symbol", symbol);
        xml.add("type", type);
        xml.add("env", env);
        xml.add("help", help);
        xml.add("prompt", prompt);
        xml.add("defaults", defaults);
        xml.add("depends", depends);

        if (!options.isEmpty()) {
            xml.start("options");
            for (String option : options) {
                xml.add("option", option);
            }
            xml.end();
        }

        if (xmlConsumer != null) {
            xmlConsumer.generate(xml);
        }

        xml.end();
    }

    public interface XMLGenerator {
        void generate(XML xml) throws XMLStreamException;
    }

}
