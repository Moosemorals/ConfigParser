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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class Main {

    public static final File SOURCE_FOLDER = new File("/home/osric/src/linux-4.13/");

    public static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        Environment environment = new Environment();
        environment.put("SRCARCH", "x86");
        environment.put("ARCH", "x86");
        environment.put("KERNELVERSION", "4.13");

        File topLevel = new File(SOURCE_FOLDER, "Kconfig");

        Menu top = new MenuParser(environment).parse(topLevel);

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
        JsonGenerator jg = jgf.createGenerator(System.out);

        dumpMenu(top, jg);
        System.out.flush();

    }

    private static void dumpMenu(Menu m, JsonGenerator json) {

        json.writeStartObject()
                .write("type", "menu")
                .write("prompt", m.getPrompt().getValue())
                .writeStartArray("entries");

        
        for (Entry e : m.getEntries()) {
            if (e instanceof Menu) {
                dumpMenu((Menu) e, json);
            } else if (e instanceof Config) {
                dumpConfig((Config) e, json);
            }
        }

        json.writeEnd().writeEnd();
    }

    private static void dumpConfig(Config config, JsonGenerator json) {
        json.writeStartObject();
        json.write("type", "config");
        json.write("symbol",config.getSymbol());
        
        if (config.getHelp() != null) {
            json.write("help", config.getHelp());
        }
        
       
        List<Condition> depends = config.getDepends();
        if (!depends.isEmpty()) {
            json.writeStartArray("depends");
            for (Condition c : depends) {
                json.write(c.toString());
            }
            json.writeEnd();
        }
        json.writeEnd();

    }

}
