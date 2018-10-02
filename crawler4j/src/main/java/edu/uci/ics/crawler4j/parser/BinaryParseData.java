/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.uci.ics.crawler4j.url.WebURL;

public class BinaryParseData implements ParseData {

    private static final Logger logger = LoggerFactory.getLogger(BinaryParseData.class);
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String DEFAULT_OUTPUT_FORMAT = "html";

    private static final Parser AUTO_DETECT_PARSER = new AutoDetectParser();
    private static final SAXTransformerFactory SAX_TRANSFORMER_FACTORY =
        (SAXTransformerFactory) TransformerFactory.newInstance();

    private final ParseContext context = new ParseContext();
    private Set<WebURL> outgoingUrls = new HashSet<>();
    private String html = null;

    public BinaryParseData() {
        context.set(Parser.class, AUTO_DETECT_PARSER);
    }

    public void setBinaryContent(byte[] data)
                throws TransformerConfigurationException, TikaException, SAXException, IOException {
        InputStream inputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            TransformerHandler handler =
                getTransformerHandler(outputStream, DEFAULT_OUTPUT_FORMAT, DEFAULT_ENCODING);
            AUTO_DETECT_PARSER.parse(inputStream, handler, new Metadata(), context);

            // Hacking the following line to remove Tika's inserted DocType
            this.html = new String(outputStream.toByteArray(), DEFAULT_ENCODING).replace(
                "http://www.w3.org/1999/xhtml", "");
        } catch (TransformerConfigurationException | TikaException | SAXException | IOException | RuntimeException e) {
            throw e;
        }
    }

    /**
     * Returns a transformer handler that serializes incoming SAX events to
     * XHTML or HTML (depending the given method) using the given output encoding.
     *
     * @param encoding output encoding, or <code>null</code> for the platform default
     */
    private static TransformerHandler getTransformerHandler(OutputStream out, String method,
                                                            String encoding)
        throws TransformerConfigurationException {

        TransformerHandler transformerHandler = SAX_TRANSFORMER_FACTORY.newTransformerHandler();
        Transformer transformer = transformerHandler.getTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, method);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        if (encoding != null) {
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        }

        transformerHandler.setResult(new StreamResult(new PrintStream(out)));
        return transformerHandler;
    }

    /** @return Parsed binary content or null */
    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public Set<WebURL> getOutgoingUrls() {
        return outgoingUrls;
    }

    @Override
    public void setOutgoingUrls(Set<WebURL> outgoingUrls) {
        this.outgoingUrls = outgoingUrls;
    }

    @Override
    public String toString() {
        return ((html == null) || html.isEmpty()) ? "No data parsed yet" : html;
    }
}