/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.url;

/**
 * Interface that transform an URL into an other
 *
 * @author Bounkong Khamphousone
 */
public interface URLTransformer {

    /**
     * @param url url to transform
     * @return url transformed
     */
    String getUrl(String url);

    /**
     * @param url url to transform
     * @param context The base URL in which to resolve the specification.
     * @return url transformed
     */
    String getUrl(String url, String context);
}