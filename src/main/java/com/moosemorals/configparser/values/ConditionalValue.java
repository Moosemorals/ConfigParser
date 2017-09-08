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
package com.moosemorals.configparser.values;

import com.moosemorals.configparser.types.Condition;
import com.moosemorals.configparser.XML;
import com.moosemorals.configparser.XMLable;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public abstract class ConditionalValue implements XMLable {

    private final Logger log = LoggerFactory.getLogger(ConditionalValue.class);

    protected final String value;
    protected final Condition condition;

    public ConditionalValue(String value, Condition condition) {
        this.value = value;
        this.condition = condition;
    }

    public String getValue() {
        return value;
    }

    public boolean evaluate() {
        return condition.evaluate();
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[").append(value);
        if (condition != null) {
            result.append(" if ").append(condition.toString());
        }        
        result.append("]");
        return result.toString();
    }

    @Override
    public void toXML(XML xml) throws XMLStreamException {
        
        
        if (condition != null) {
            xml.add(getClass().getSimpleName().toLowerCase(), value, "if", condition.toString());            
        }        else {
            xml.add(getClass().getSimpleName().toLowerCase(), value);
        }
        
    }

}
