package leelawatcher.parser;

import leelawatcher.goboard.PointOfPlay;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class AutoGtpOutputParserTest {

  @Test // issue #13
  public void testResign() {
    AutoGtpOutputParser parser = new AutoGtpOutputParser(null);
    assertNull(parser.parseMove("resign"));
  }

  @Test
  public void testPass() {
    AutoGtpOutputParser parser = new AutoGtpOutputParser(null);
    assertNull(parser.parseMove("pass"));
  }

  @Test(expected = RuntimeException.class)
  public void testBadMove() {
    AutoGtpOutputParser parser = new AutoGtpOutputParser(null);
    parser.parseMove("resign!");
    fail("'foo' should not be a valid move");
  }

  @Test
  public void testMove() {
    AutoGtpOutputParser parser = new AutoGtpOutputParser(null);
    PointOfPlay move = parser.parseMove("D3");
    assertNotNull(move);
    assertEquals(move.getX(),3);
    assertEquals(move.getY(),2);
  }
}
