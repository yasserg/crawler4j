/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package edu.uci.ics.crawler4j.robotstxt;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleSet extends TreeSet<String> {

    private static final long serialVersionUID = 1L;

    public static final String WILDCARD_CHARACTER = "*";
    public static final String REGEX_BACKSLASH_REPLACEMENT = "\\/";
    public static final String BACKSLASH_CHARACTER = "/";
    public static final String REGEX_WILDCARD_CHARACTER = "\\*";
    public static final String REGEX_WILDCARD_CHARACTER_REPLACEMENT = "\\.*";

    private Set<Pattern> wildcardExpressions = new HashSet<>();

    @Override
    public boolean add(String str) {

        if (str.contains(WILDCARD_CHARACTER)) {
            return wildcardExpressions.add(Pattern.compile(str.replaceAll(BACKSLASH_CHARACTER,
                    REGEX_BACKSLASH_REPLACEMENT).replaceAll(REGEX_WILDCARD_CHARACTER,
                    REGEX_WILDCARD_CHARACTER_REPLACEMENT)));
        } else {
            SortedSet<String> sub = headSet(str);
            if (!sub.isEmpty() && str.startsWith(sub.last())) {
                // no need to add; prefix is already present
                return false;
            }
            boolean retVal = super.add(str);
            sub = tailSet(str + "\0");
            while (!sub.isEmpty() && sub.first().startsWith(str)) {
                // remove redundant entries
                sub.remove(sub.first());
            }
            return retVal;
        }
    }

    public boolean containsPrefixOf(String s) {
        SortedSet<String> sub = headSet(s);
        // because redundant prefixes have been eliminated,
        // only a test against last item in headSet is necessary
        if (!sub.isEmpty() && s.startsWith(sub.last())) {
            return true; // prefix substring exists
        }

        //check for wildcards
        for (Pattern p : wildcardExpressions) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                return true;
            }
        }

        // might still exist exactly (headSet does not contain boundary)
        return contains(s);
    }
}