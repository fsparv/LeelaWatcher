package leelawatcher.scorer;

import leelawatcher.goboard.Board;
import leelawatcher.goboard.IllegalMoveException;
import leelawatcher.goboard.move.Move;
import leelawatcher.goboard.PointOfPlay;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class QuickRulesTest {

  @Test
  public void testIsSelfCapture() {
    Board llCornerSelf = new Board();

    /* Setting up this (will play lower case letter):
    . . . . .
    B B . . .
    W B B . .
    w W B . .

    This move is not leagle for SimpleRules

     */
    List<PointOfPlay> white = new ArrayList<>();
    List<PointOfPlay> black = new ArrayList<>();

    white.add(new PointOfPlay(1,0));
    white.add(new PointOfPlay(0,1));

    black.add(new PointOfPlay(2,0));
    black.add(new PointOfPlay(1,1));
    black.add(new PointOfPlay(2,1));
    black.add(new PointOfPlay(0,2));
    black.add(new PointOfPlay(1,2));


    llCornerSelf.setUp(white,black, Collections.emptyList(), false);

    try {
      llCornerSelf.doMove(0,0);
      fail("Self capture in the lower left corner should be illegal\n" + llCornerSelf.getCurrPos());
    } catch (IllegalMoveException e) {
      // success but cam print this to validate that the message in the exception looks good.
      // e.printStackTrace();
    }
  }

  /**
   * This issue causes the wrong stones (the current players stones) to be self capture instead of a
   * neighbor's stones.
   */
  @Test
  public void testIssue4() throws IllegalMoveException {
    Board issue4 = new Board();

    /* Setting up this (will play lower case letter):

    . . . . .
    W W . . .
    B B W . .
    W B W . .
    W b W . .

    Error is this result:
     4: . . . . . . . . . . . . . . . . . . .
     3: W W . . . . . . . . . . . . . . . . .
     2: . . W . . . . . . . . . . . . . . . .
     1: W . W . . . . . . . . . . . . . . . .
     0: W . W . . . . . . . . . . . . . . . .

    Instead of:
     3: W W . . . . . . . . . . . . . . . . .
     2: B B W . . . . . . . . . . . . . . . .
     1: . B W . . . . . . . . . . . . . . . .
     0: . B W . . . . . . . . . . . . . . . .

    */
    List<PointOfPlay> white = new ArrayList<>();
    List<PointOfPlay> black = new ArrayList<>();

    white.add(new PointOfPlay(0,0));
    white.add(new PointOfPlay(0,1));
    white.add(new PointOfPlay(0,3));
    white.add(new PointOfPlay(1,3));
    white.add(new PointOfPlay(2,2));
    white.add(new PointOfPlay(2,1));
    white.add(new PointOfPlay(2,0));

    black.add(new PointOfPlay(0,2));
    black.add(new PointOfPlay(1,1));
    black.add(new PointOfPlay(1,2));

    issue4.setUp(white,black, Collections.emptyList(), true);

    issue4.doMove(1,0);

    System.out.println(issue4.getCurrPos());

    assertFalse(issue4.getCurrPos().stoneAt(new PointOfPlay(0,0)));
    assertFalse(issue4.getCurrPos().stoneAt(new PointOfPlay(0,1)));
    assertTrue(issue4.getCurrPos().colorAt(0,2) == Move.MOVE_BLACK);
    assertTrue(issue4.getCurrPos().colorAt(1,2) == Move.MOVE_BLACK);
    assertTrue(issue4.getCurrPos().colorAt(1,1) == Move.MOVE_BLACK);
    assertTrue(issue4.getCurrPos().colorAt(1,0) == Move.MOVE_BLACK);
  }
}
