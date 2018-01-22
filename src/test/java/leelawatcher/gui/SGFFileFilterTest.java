package leelawatcher.gui;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Barry Becker
 */
public class SGFFileFilterTest {

    private SGFFileFilter sgfFilter = new SGFFileFilter();

    @Test
    public void testNonSGFFile() {
        assertFalse(sgfFilter.accept(new File("fff.txt")));
    }

    @Test
    public void testValidSGFFile() {
        assertTrue(sgfFilter.accept(new File("fff.sgF")));
        assertTrue(sgfFilter.accept(new File("fff.SGF")));
        assertTrue(sgfFilter.accept(new File("xyz123.SGf")));
    }
}
