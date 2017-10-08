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

import java.io.Writer;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class XML {

    private final Logger log = LoggerFactory.getLogger(XML.class);
    private final XMLStreamWriter xml;

    public XML(Writer out) throws XMLStreamException {
        xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        xml.writeStartDocument();
    }

    private void writeAttributes(String[] attr) throws XMLStreamException {
        if (attr != null) {
            if (attr.length % 2 != 0) {
                throw new IllegalArgumentException("Attributes must come in pairs");
            }
            for (int i = 0; i < attr.length; i += 2) {
                xml.writeAttribute(attr[i], attr[i + 1]);
            }
        }
    }

    public void start(String name, String... attr) throws XMLStreamException {
        xml.writeStartElement(name);
        writeAttributes(attr);
    }

    public void add(String name, String content, String... attr) throws XMLStreamException {
        if (content != null) {
            xml.writeStartElement(name);
        } else {
            xml.writeEmptyElement(name);
        }
        writeAttributes(attr);
        if (content != null) {
            xml.writeCharacters(content);
            xml.writeEndElement();
        }

    }

    public void add(String name, String content) throws XMLStreamException {
        if (content != null) {
            xml.writeStartElement(name);
            xml.writeCharacters(content);
            xml.writeEndElement();
        }
    }

    public void add(XMLable other) throws XMLStreamException {
        other.toXML(this);
    }

    public void add(String name, XMLable other) throws XMLStreamException {
        if (other != null) {
            xml.writeStartElement(name);
            other.toXML(this);
            xml.writeEndElement();
        }
    }

    public void add(String name) throws XMLStreamException {
        xml.writeEmptyElement(name);
    }

    public void add(String name, List<? extends XMLable> l) throws XMLStreamException {
        if (!l.isEmpty()) {
            xml.writeStartElement(name);
            for (XMLable a : l) {
                a.toXML(this);
            }
            xml.writeEndElement();
        }
    }

    public void end() throws XMLStreamException {
        xml.writeEndElement();
    }

    public void endDocument() throws XMLStreamException {
        xml.writeEndDocument();
    }

}
