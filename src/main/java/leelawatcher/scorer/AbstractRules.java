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

package leelawatcher.scorer;


import leelawatcher.goboard.Board;
import leelawatcher.goboard.MarkablePosition;
import leelawatcher.goboard.PointOfPlay;

public abstract class AbstractRules implements Rules {
  Board aBoard;

  protected AbstractRules(Board b) {
    aBoard = b;
  }

  public abstract boolean isSelfCapture(PointOfPlay p);

  public abstract boolean isKo(PointOfPlay p);

  public abstract boolean isLegalMove(PointOfPlay p);

  /**
   * Tests for the presence of a stone at PointOfPlay p on aBoard.
   */
  public boolean isEmpty(PointOfPlay p) {
    return !(aBoard.getCurrPos().stoneAt(p));
  }

  /**
   * Counts the number of liberties on a group.
   * <p>
   * The number of liberties is equal to the number of ajacent empty
   * PointOfPlays on the board. This method is used in determining if a group
   * should or would be captured. Groups with no liberties are capturable.
   * Counting is acheived by recursion to neighboring points not occupied by
   * stones of the opposite color. Recursion to an empty point  increments
   * the counter.
   * <p>
   * A MarkablePosition object is used to prevent recursion to the
   * same stone more than once. The order of the direction of recursion
   * is north, east, south, and west considering the board as if it were
   * a map.
   * <p>
   * This method will throw an IllegalArgumentException if the PointOfPlay p
   * is not on the board. When this method is called for the first time
   * (non-recursive calls) the second argument should always be 0, and the
   * third argument should be null. Calling this method on an empty PointOfPlay
   * will return 1.
   */
  public int countLibs(PointOfPlay p, int hasAlready, MarkablePosition pos) {
    int result = doCountLibs(p, hasAlready, pos);
    if (pos != null) // Some usages do pass null to pos.
    {
      pos.clearMarks();
    }
    return result;
  }

  // This method is wrapped by the public version coungLibs so that the
  // marks used to control recursion can be removed after all recursion is
  // completed.
  private int doCountLibs(PointOfPlay p, int hasAlready, MarkablePosition pos) {
    if (pos == null) {
      //System.out.println("InitialCall");
      hasAlready = 0;
      pos = new MarkablePosition(aBoard.getCurrPos());
    }
    if (aBoard.isOnBoard(p)) {
      //System.out.println(p);
      pos.setMark(new PointOfPlay(p.getX(), p.getY()));
      //System.out.println("On Board:" + p );
      if (pos.stoneAt(p)) {
        // recurse north if we can (plus Y)
        PointOfPlay dir = new PointOfPlay(p.getX(), p.getY() + 1);
        if (aBoard.isOnBoard(dir) && !pos.isMarked(dir)
            && !((pos.blackAt(p) && pos.whiteAt(dir))
            || (pos.whiteAt(p) && pos.blackAt(dir)))) {
          //System.out.println("north="+dir+"p="+p);
          hasAlready = doCountLibs(dir, hasAlready, pos);
        }

        // recurse east if we can (plus X)
        dir = new PointOfPlay(p.getX() + 1, p.getY());
        if (aBoard.isOnBoard(dir) && !pos.isMarked(dir)
            && !((pos.blackAt(p) && pos.whiteAt(dir))
            || (pos.whiteAt(p) && pos.blackAt(dir)))) {
          //System.out.println("east="+dir+"p="+p);
          hasAlready = doCountLibs(dir, hasAlready, pos);
        }

        // recurse south if we can (minus Y)
        dir = new PointOfPlay(p.getX(), p.getY() - 1);
        if (aBoard.isOnBoard(dir) && !pos.isMarked(dir)
            && !((pos.blackAt(p) && pos.whiteAt(dir))
            || (pos.whiteAt(p) && pos.blackAt(dir)))) {
          //System.out.println("south="+dir+"p="+p);
          hasAlready = doCountLibs(dir, hasAlready, pos);
        }

        // recurse west if we can (minus X)
        dir = new PointOfPlay(p.getX() - 1, p.getY());
        if (aBoard.isOnBoard(dir) && !pos.isMarked(dir)
            && !((pos.blackAt(p) && pos.whiteAt(dir))
            || (pos.whiteAt(p) && pos.blackAt(dir)))) {
          //System.out.println("west="+dir+"p="+p);
          hasAlready = doCountLibs(dir, hasAlready, pos);
        }
      } else {
        //System.out.println("lib at:" + p);
        return ++hasAlready;
      }
    } else {
      throw new IllegalArgumentException("p not on board!");
    }

    return hasAlready;
  }
}

/*
 * $Log$
 * Revision 1.3  2003/07/19 02:50:05  gus
 * New License based on the Apache License, Yeah open source :)
 *
 * Revision 1.2  2002/12/16 06:35:49  gus
 * Fixes that squash a bug that allowed ko to be violated
 * in certain cases.
 *
 * Revision 1.1.1.1  2002/12/15 07:02:57  gus
 * Initial import into cvs server running on Aptiva
 *
 * Revision 1.2  2002/02/27 04:08:23  togo
 * Added javadoc, and variable naming scheme to tsb.GoBoard board.java and
 * renamed it tsb.GoBoard.Board.java.
 *
 */
