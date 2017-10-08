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

import com.moosemorals.configparser.SourceFile.Location;
import com.moosemorals.configparser.XML;
import com.moosemorals.configparser.values.Select;
import com.moosemorals.configparser.values.Range;
import com.moosemorals.configparser.values.Imply;
import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Config extends Entry {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    protected final List<Select> selects;
    protected final List<Imply> implies;
    protected final List<Range> ranges;

    public Config(Location location, String symbol) {
        super(location, symbol);
        this.selects = new LinkedList<>();
        this.implies = new LinkedList<>();
        this.ranges = new LinkedList<>();
    }

    public void addSelect(Select select) {
        if (!selects.contains(select)) {
            selects.add(select);
        }
    }

    public List<Select> getSelects() {
        return selects;
    }

    public void addImplies(Imply imply) {
        if (!implies.contains(imply)) {
            implies.add(imply);
        }
    }

    public List<Imply> getImplies() {
        return implies;
    }

    public void addRange(Range range) {
        if (!ranges.contains(range)) {
            ranges.add(range);
        }
    }

    public List<Range> getRanges() {
        return ranges;
    }

    @Override
    public void toXML(XML xml) throws XMLStreamException {
        xml.start("config", "file", location.getFile(), "line", location.getLine());
        xml.add("symbol", symbol);
        xml.add("type", type);
        xml.add("env", env);
        xml.add("help", help);

        if (prompt != null) {
            xml.add("prompt", prompt);
        }

        xml.add("defaults", defaults);
        xml.add("depends", depends);
        xml.add("selects", selects);
        xml.add("implies", implies);
        xml.add("ranges", ranges);

        xml.end();
    }

}
