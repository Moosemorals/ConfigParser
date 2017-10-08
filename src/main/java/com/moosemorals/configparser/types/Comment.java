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
import com.moosemorals.configparser.values.Default;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Comment extends Entry {

    private final Logger log = LoggerFactory.getLogger(Comment.class);

    public Comment(Location location, String symbol) {
        super(location, symbol);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("[comment:").append(prompt);

        if (!depends.isEmpty()) {
            result.append(" dep: ");
            for (int i = 0; i < depends.size(); i += 1) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append(depends.get(i));
            }
        }

        result.append("]");
        return result.toString();
    }

    @Override
    public void toXML(XML xml) throws XMLStreamException {
        super.toXML(xml, "comment", null);
    }

}
