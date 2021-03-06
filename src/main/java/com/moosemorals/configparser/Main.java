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

import com.moosemorals.configparser.types.Menu;
import com.moosemorals.configparser.parsers.MenuParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Main {

    public static final File SOURCE_FOLDER = new File("/home/osric/src/linux-4.13/");
    //      public static final File SOURCE_FOLDER = new File("/home/osric/src/buildroot-2017.02.5/");

    public static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        Environment environment = new Environment();
        environment.put("SRCARCH", "x86");
        environment.put("ARCH", "x86");
        environment.put("KERNELVERSION", "4.13");

        SourceFile.setRoot(SOURCE_FOLDER);
        Menu top = new MenuParser(null, environment).parse("Kconfig");

        log.debug("Saving to XML");
        try (FileWriter out = new FileWriter(new File("/tmp/config.xml"))) {
            XML xml = new XML(out);
            top.toXML(xml);
            out.flush();

        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
        log.debug("Save complete");
    }

}
