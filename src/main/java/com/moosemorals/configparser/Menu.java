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

import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Menu extends Entry {

    private final Logger log = LoggerFactory.getLogger(Menu.class);

    private final List<Entry> entries;

    public Menu(String symbol) {
        super(symbol);
        this.entries = new LinkedList<>();
    }

    public void addEntry(Entry e) {
        entries.add(e);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("[menu: ").append(prompt);

        result.append(entries.size()).append(entries.size() == 1 ? " entry" : " entries");

        result.append("]");
        return result.toString();
    }

    @Override
    public void toXML(XML xml) throws XMLStreamException {
        xml.start("menu");
        xml.add(prompt);        
        xml.add("depends", depends);
        xml.add("entries", entries);
        
        xml.end();
    }

}
