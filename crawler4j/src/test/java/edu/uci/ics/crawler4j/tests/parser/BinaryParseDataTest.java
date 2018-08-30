/**
 *
 *
 * Copyright 2018 Diffblue Limited
 *
 * Diffblue Limited licenses this file to You under the Apache License, Version 2.0
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

package edu.uci.ics.crawler4j.tests.parser;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.diffblue.deeptestutils.Reflector;

import edu.uci.ics.crawler4j.parser.BinaryParseData;

public class BinaryParseDataTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    /* testedClasses: BinaryParseData */

    /*
    * Test generated by Diffblue Deeptest.
    * This test case covers:
    *  - conditional line 124 branch to line 124
    */

    @Test
    public void toStringOutputNotNull() throws InvocationTargetException {

        // Arrange
        BinaryParseData objectUnderTest =
            ((BinaryParseData)Reflector.getInstance("edu.uci.ics.crawler4j.parser.BinaryParseData"));
        Reflector.setField(objectUnderTest, "outgoingUrls", null);
        Reflector.setField(objectUnderTest, "context", null);
        Reflector.setField(objectUnderTest, "html", null);

        // Act
        String retval = objectUnderTest.toString();

        // Assert result
        Assert.assertEquals("No data parsed yet", retval);
    }

    /*
    * Test generated by Diffblue Deeptest.
    * This test case covers:
    *  - conditional line 124 branch to line 124
    */

    @Test
    public void toStringOutputNotNull2() throws InvocationTargetException {

        // Arrange
        BinaryParseData objectUnderTest =
            ((BinaryParseData)Reflector.getInstance("edu.uci.ics.crawler4j.parser.BinaryParseData"));
        Reflector.setField(objectUnderTest, "outgoingUrls", null);
        Reflector.setField(objectUnderTest, "context", null);
        Reflector.setField(objectUnderTest, "html", "");

        // Act
        String retval = objectUnderTest.toString();

        // Assert result
        Assert.assertEquals("No data parsed yet", retval);
    }

    /*
    * Test generated by Diffblue Deeptest.
    * This test case covers:
    *  - conditional line 124 branch to line 124
    */

    @Test
    public void toStringOutputNotNull3() throws InvocationTargetException {

        // Arrange
        BinaryParseData objectUnderTest =
            ((BinaryParseData)Reflector.getInstance("edu.uci.ics.crawler4j.parser.BinaryParseData"));
        Reflector.setField(objectUnderTest, "outgoingUrls", null);
        Reflector.setField(objectUnderTest, "context", null);
        Reflector.setField(objectUnderTest, "html", "        ");

        // Act
        String retval = objectUnderTest.toString();

        // Assert result
        Assert.assertEquals("        ", retval);
    }
}
