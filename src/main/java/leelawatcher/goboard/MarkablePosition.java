/*
    Copyright 2017 Patrick G. Heck

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package leelawatcher.goboard;

import java.util.HashSet;
import java.util.Set;


/**
 * This class adds markup features to a position.
 * <p>
 * <p>Mark up features are supported by an array of bitfields mirroring those
 * used in the parent class. This class is useful for marking locations
 * already visited, evaluated, or counted by a routine that traverses groups
 * or scans a position multiple times.
 * <p>
 * <p><b>PLEASE NOTE:</b><br>
 * MarkablePosition objects inherit the equals() method from the parent
 * class unmodified, and thus two MarkablePositions can be equal even if the
 * values in the markup fields differ. This is subject to change however, so
 * storing Positions and MarkablePositions mixed in the same utility class is
 * strongly discouraged.
 *
 * @author Patrick G. Heck
 */
public class MarkablePosition extends Position {

  // an array that marks up the position
  private int[] marks = new int[19];

  /**
   * Instantiate a markable position based on a preexisting position object.
   *
   * @param p A position on which to base this instance.
   */
  public MarkablePosition(Position p) {
    super(p);
    clearMarks();
  }

  /**
   * Clear all marked locations.
   * <p>
   * All mark up bits are set to 0 completely erasing all marks.
   */
  public void clearMarks() {
    for (int i = 0; i < marks.length; i++) {
      marks[i] = 0;
    }
  }

  /**
   * Test for the presence of a marker at a given point.
   * <p>
   * This method depends on colMasks in the specification of the
   * x coordinate.
   *
   * @param p The point to be tested
   * @return True if a mark has been set false otherwise
   */
  public boolean isMarked(PointOfPlay p) {
    return ((marks[p.getY()] & colMasks[p.getX()]) > 0);
  }

  /**
   * Apply a marker to a given point on the board.
   * <p>
   * This method depends on colMasks in the specification of the x
   * coordinate.
   *
   * @param p The point to be marked.
   */
  public void setMark(PointOfPlay p) {
    if (!isMarked(p)) {
      marks[p.getY()] += colMasks[p.getX()];
    }
  }

  /**
   * Remove a marker from a specific point.
   * <p>
   * This method depends on colMasks in the specification of the x
   * coordinate.
   *
   * @param p The point to be unmarked.
   */
  public void clearMark(PointOfPlay p) {
    if (isMarked(p)) {
      marks[p.getY()] ^= colMasks[p.getX()];
    }
  }

  /**
   * Check that a point would be on a board of a given size.
   * <p>
   * This method seems like it should be in a more general location and
   * will likely be moved up to {@link Position} in the near future.
   *
   * @param p         The point to verify
   * @param boardSize The size of the board we are verifying against.
   * @return True if the point is on the board false otherwise.
   */
  private boolean isOnBoard(PointOfPlay p, int boardSize) {
    return ((p.getX() < boardSize) && (p.getX() >= 0)
        && (p.getY() < boardSize) && (p.getY() >= 0));
  }

  /**
   * Generate a HashSet describing a group of stones.
   * <p>
   * <p> This method generally should not be called directly except by
   * classes which own an instance of a <code>MarkablePosition</code>.
   * Those classes should ensure that <code>null</code> is passed to the
   * <code>members</code> parameter, and if a class containing a
   * <code>MarkablePosition</code> wishes to supply a list of members of
   * a group recorded within the <code>MarkablePosition</code> to its
   * clients, then it should implement an adapter method that guarantees
   * the clients cannot generate calls that pass a value other than null
   * to <code>members</code>.
   * <p>
   * <p><b>Please Note:</b><br>
   * This method clears all existing markers. Marks are used to indicate
   * stones that have already been counted. Only adjacent stones are
   * members of a group and thus stones diagonal to a member stone are
   * not counted as members of the same group unless they share an adjacent
   * stone. This is a universal minimal standard in all forms of Go, and
   * can thus be safely encoded here.
   *
   * @param p         Start counting with the stone at this point
   * @param members   <b>Always <code>null</code></b>. Non-null values are
   *                  <i>only valid in recursive calls</i>. (Write it
   *                  across the top of your monitor and don't forget it!)
   * @param boardSize The size of the board
   * @return A HashSet of PointOfPlay objects describing the group
   */
  public Set getGroupSet(PointOfPlay p, Set<PointOfPlay> members, int boardSize) {
    if (members == null) {
      members = new HashSet<>();
      clearMarks();                // NOTE that this clears all marks
    }

    if (isOnBoard(p, boardSize)) {
      setMark(p);

      if (stoneAt(p)) {
        PointOfPlay dir;
        members.add(p);
        dir = new PointOfPlay(p.getX(), p.getY() + 1);
        if (isOnBoard(dir, boardSize) && !isMarked(dir)
            && ((blackAt(p) && blackAt(dir))
            || (whiteAt(p) && whiteAt(dir)))) {
          getGroupSet(dir, members, boardSize);
        }
        dir = new PointOfPlay(p.getX() + 1, p.getY());
        if (isOnBoard(dir, boardSize) && !isMarked(dir)
            && ((blackAt(p) && blackAt(dir))
            || (whiteAt(p) && whiteAt(dir)))) {
          getGroupSet(dir, members, boardSize);
        }
        dir = new PointOfPlay(p.getX(), p.getY() - 1);
        if (isOnBoard(dir, boardSize) && !isMarked(dir)
            && ((blackAt(p) && blackAt(dir))
            || (whiteAt(p) && whiteAt(dir)))) {
          getGroupSet(dir, members, boardSize);
        }
        dir = new PointOfPlay(p.getX() - 1, p.getY());
        if (isOnBoard(dir, boardSize) && !isMarked(dir)
            && ((blackAt(p) && blackAt(dir))
            || (whiteAt(p) && whiteAt(dir)))) {
          getGroupSet(dir, members, boardSize);
        }

      }
    }
    return members;
  }

}
