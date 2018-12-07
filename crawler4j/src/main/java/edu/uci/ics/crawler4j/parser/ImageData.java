/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser;

import java.util.Map;

/**
 * Information about images on a page.
 * Can be used to e.g. detect images without an 'alt' tag.
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class ImageData {

    private final String src;
    private final Map<String, String> attrVals;

    public ImageData(String src, Map<String, String> attrVals) {
        this.src = src;
        this.attrVals = attrVals;
    }

    public String getSrc() {
        return src;
    }

    public Map<String, String> getAttrVals() {
        return attrVals;
    }
}
