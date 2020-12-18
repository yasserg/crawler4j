package edu.uci.ics.crawler4j.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class UtilTest {

    @Rule
    public final Timeout globalTimeout = new Timeout(10000);

    /* testedClasses: Util */
    // Test written by Diffblue Cover.
    @Test
    public void long2ByteArray() {

        // Arrange
        final long l = 0x12ff34ff56ffL;

        // Act
        final byte[] actual = Util.long2ByteArray(l);

        // Assert result
        Assert.assertArrayEquals(
            new byte[] {0, 0, 0x12, -1, 0x34, -1, 0x56, -1}, actual);
    }

    // Test written by Diffblue Cover.
    @Test
    public void int2ByteArray() {

        // Arrange
        final int value = 0x12ff34ff;

        // Act
        final byte[] actual = Util.int2ByteArray(value);

        // Assert result
        Assert.assertArrayEquals(new byte[] {0x12, -1, 0x34, -1}, actual);
    }

    // Test written by Diffblue Cover.
    @Test
    public void putIntInByteArray() {

        // Arrange
        final int value = 0x12ff34ff;
        final byte[] buf = {0, 0, 0, 0, 0, 0};
        final int offset = 2;

        // Act
        Util.putIntInByteArray(value, buf, offset);

        // Assert side effects
        Assert.assertArrayEquals(new byte[] {0, 0, 0x12, -1, 0x34, -1}, buf);
    }

    // Test written by Diffblue Cover.
    @Test
    public void byteArray2Int() {

        // Arrange
        final byte[] b = {0x12, -1, 0x34, -1};

        // Act
        final int actual = Util.byteArray2Int(b);

        // Assert result
        Assert.assertEquals(0x12ff34ff, actual);
    }

    // Test written by Diffblue Cover.
    @Test
    public void byteArray2Long() {

        // Arrange
        final byte[] b = {0, 0, 0, -1, 0x34, -1, 0x56, -1};

        // Act
        final long actual = Util.byteArray2Long(b);

        // Assert result
        Assert.assertEquals(0x0ff34ff56ffL, actual);
    }

    // Test written by Diffblue Cover.
    @Test
    public void hasBinaryContent() {
        Assert.assertFalse(Util.hasBinaryContent("BAZ"));
        Assert.assertTrue(Util.hasBinaryContent("hhhYaimage"));
    }

    // Test written by Diffblue Cover.
    @Test
    public void hasPlainTextContent() {
        Assert.assertFalse(Util.hasPlainTextContent("1"));
        Assert.assertFalse(Util.hasPlainTextContent("htmlTTeXT"));
        Assert.assertTrue(Util.hasPlainTextContent("YxtEXTeXT"));
    }

    // Test written by Diffblue Cover.
    @Test
    public void hasCssTextContentOutputFalse() {
        Assert.assertFalse(Util.hasCssTextContent("1234"));
    }

}
