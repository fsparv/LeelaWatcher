package leelawatcher.goboard;

import leelawatcher.goboard.move.Move;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static leelawatcher.goboard.move.Move.*;


/**
 * @author Barry Becker
 */
public class MarkablePositionTest {


    @Test
    public void testConstructionWithEmptyPosition() {
        MarkablePosition pos = new MarkablePosition(new Position());
        assertFalse(pos.isMarked(new PointOfPlay(2, 2)));
    }

    @Test
    public void testSetMark() {
        MarkablePosition pos = new MarkablePosition(new Position());
        pos.setMark(new PointOfPlay(2, 2));
        assertTrue(pos.isMarked(new PointOfPlay(2, 2)));
    }

    @Test
    public void testSetMarkTwice() {
        MarkablePosition pos = new MarkablePosition(new Position());
        pos.setMark(new PointOfPlay(2, 2));
        pos.setMark(new PointOfPlay(2, 2));
        assertTrue(pos.isMarked(new PointOfPlay(2, 2)));
    }

    @Test
    public void testClearMark() {
        MarkablePosition pos = new MarkablePosition(new Position());
        PointOfPlay p1 = new PointOfPlay(2, 3);
        pos.setMark(p1);
        assertTrue(pos.isMarked(p1));
        pos.clearMark(p1);
        assertFalse(pos.isMarked(p1));
    }

    @Test
    public void testClearMarks() {
        MarkablePosition pos = new MarkablePosition(new Position());
        PointOfPlay p1 = new PointOfPlay(2, 2);
        PointOfPlay p2 = new PointOfPlay(2, 3);
        pos.setMark(p1);
        pos.setMark(p2);
        assertTrue(pos.isMarked(p1));
        assertTrue(pos.isMarked(p2));
        pos.clearMarks();
        assertFalse(pos.isMarked(p1));
        assertFalse(pos.isMarked(p2));
    }

    @Test
    public void testGetGroup1Set() {

        Position pos = new Position();
        Move root = new Move();
        pos = new Position(pos, new Move(2, 2, MOVE_BLACK, root));
        MarkablePosition mp = new MarkablePosition(pos);

        Set members = mp.getGroupSet(new PointOfPlay(2, 2), null, 9);
        assertEquals("Unexpected number of members",1, members.size());
    }

    @Test
    public void testGetGroup3Set() {
        Position pos = new Position();
        Move root = new Move();
        pos = new Position(pos, new Move(2, 2, MOVE_BLACK, root));
        pos = new Position(pos, new Move(2, 5, MOVE_WHITE, root));
        pos = new Position(pos, new Move(2, 3, MOVE_BLACK, root));
        pos = new Position(pos, new Move(6, 5, MOVE_WHITE, root));
        pos = new Position(pos, new Move(3, 3, MOVE_BLACK, root));
        MarkablePosition mp = new MarkablePosition(pos);

        Set members = mp.getGroupSet(new PointOfPlay(2, 2), null, 9);
        assertEquals("Unexpected number of members",3, members.size());
    }

}
