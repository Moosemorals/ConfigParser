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
package com.moosemorals.configparser.parsers;

import com.moosemorals.configparser.types.Comment;
import com.moosemorals.configparser.Environment;
import com.moosemorals.configparser.ParseError;
import com.moosemorals.configparser.SourceFile;
import java.io.IOException;
import java.io.StreamTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Osric Wilkinson (osric@fluffypeople.com)
 */
public class CommentParser extends BaseParser {

    private final Logger log = LoggerFactory.getLogger(CommentParser.class);

    public CommentParser(MenuParser parentParser, Environment e) {
        super(parentParser, e);
    }

    public Comment parse(SourceFile t) throws IOException {
        if (!"comment".equals(t.getTokenString())) {
            throw new ParseError(t, "Must be called on comment");
        }

        Comment c = new Comment(t.getLocation(), null);

        readPrompt(t, c);

        while (true) {
            int token = t.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                    t.pushBack();
                    return c;
                case StreamTokenizer.TT_WORD:
                    switch (t.getTokenString()) {
                        case "config":
                        case "menuconfig":
                        case "choice":
                        case "endchoice":
                        case "comment":
                        case "menu":
                        case "endmenu":
                        case "if":
                        case "endif":
                        case "source":
                            t.pushBack();
                            return c;
                        case "depends":
                            readDepends(t, c);
                            break;

                        default:
                            log.debug("skipping {}", skip(t));
                            //skip(t);
                            break;
                    }
                    break;
            }
        }
    }

}
