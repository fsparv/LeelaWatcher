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

import leelawatcher.goboard.move.Move;
import leelawatcher.scorer.AbstractRules;
import leelawatcher.scorer.QuickRules;
import leelawatcher.scorer.Rules;

import java.io.*;
import java.util.*;


/**
 * Class board
 * <p>
 * This class will enforce the rules of the game, keep track of captured
 * stones, store the internal logical representation of the board, interface
 * with the GUI to represent the board and allow user input. Scoring and
 * analysis routines will be called from this class as well, as facilities for
 * saving the game in SGF FF[4].
 *
 * @author Patrick G. Heck
 * @version $Revision$
 */


public class Board {

  private Game gm;
  private List<Position> positions;
  private int currPos;
  private Rules ruleImp;
  private int whiteHasCap; // number of opponents stones white has captured.
  private int blackHasCap;

  /**
   * Create a new default board object. Default player names are "White" and
   * "Black." Also initialize a default game with no handicap and 5.5 komi.
   * This default game must be replaced using {@link #newGame} in
   * if a handicap, or different komi are required. These properties are
   * a fixed un-alterable characteristic of any game and never change
   * during or after a game.
   */

  public Board() {
    gm = new Game("White", "Black", 0, 5.5f); // for the moment stick in a
    positions = new ArrayList<>();              // default game.
    positions.add(new Position());
    currPos = 0;
    ruleImp = new QuickRules();
    whiteHasCap = 0;
    blackHasCap = 0;
  }

  /**
   * Replace the current game with a new, empty one.
   *
   * @param nameWhite The name of the player placing white stones
   * @param nameBlack The name of the player placing black stones
   * @param handi     The handicap white has given to black.
   * @param komi      The compensation points black has given to white.
   */

  public void newGame(String nameWhite, String nameBlack,
                      int handi, float komi) {
    gm = new Game(nameWhite, nameBlack, handi, komi);
    positions = new ArrayList<>();              // default game.
    positions.add(new Position());
    currPos = 0;
    whiteHasCap = 0;
    blackHasCap = 0;
  }

  /**
   * Find out how many stones have been captured by the white player.
   *
   * @return The integer number of stones captured by white.
   */
  @SuppressWarnings("unused")
  public int getWhiteHasCap() {
    return whiteHasCap;
  }

  /**
   * Find out how many stones have been captured by the black player.
   *
   * @return The integer number of stones captured by black.
   */
  @SuppressWarnings("unused")
  public int getBlackHasCap() {
    return blackHasCap;
  }

  /**
   * Get an <code>Iterator</code> for looping through the list of
   * {@link Position position}s in this variation up to this move.
   * <p>
   * The <code>Board</code> object only stores a list of the positions from
   * the start of the game through the currently displayed position.
   * Positions from other variations, or later in the game are not stored
   * anywhere in the program at this point.
   *
   * @return An iterator for positions played so far in this
   * variation.
   */

  public Iterator<Position> getPosIter() {
    return positions.iterator();
  }

  /**
   * Get the size of the board as an integer.
   * <p>
   * This method Parses the output of <code>Game.getBoardSize</code>
   * to provide a numeric board size. Non square boards are not yet
   * supported by this method, although they are legal in SGF files.
   * A more graceful way of handling the format exception will probably
   * be devised in the future. This is just a temporary solution that
   * will point out bugs in <code>Game.getBoardSize</code> in a hurry.
   *
   * @return An integer board size.
   */

  public int getBoardSize() {
    int numlines = 0;
    String temp = gm.getBoardSize();
    if (temp.indexOf(':') < 0)
      try {
        numlines = Integer.parseInt(temp);
      } catch (NumberFormatException e) {
        System.out.println("number format exception reading board size: " + temp + "\n" + e);
        System.exit(0);
      }
    else {
      System.out.println("Non-Square boards not supported yet. Size =" + temp);
      System.exit(0);
    }
    return numlines;
  }

  /**
   * Test if a point is on or off the board.
   *
   * @param p A <code>PointOfPlay</code> object to be tested.
   * @return True if the point described is on the board.
   */

  public boolean isOnBoard(PointOfPlay p) {
    return ((p.getX() < getBoardSize()) && (p.getX() >= 0)
        && (p.getY() < getBoardSize()) && (p.getY() >= 0));
  }

  /**
   * Test if it is white's turn to move
   *
   * @return True if white should play the next stone.
   */

  public boolean isWhiteMove() {
    return gm.isWMove();
  }

  /**
   * Output the current game to a disk file in SGF version 4.
   * <p>
   * See <a href="http://www.redbean.com/sgf/">specification</a> for details
   * on the output format.
   *
   * @param filName A string name to which the file can be saved.
   */

  public void saveGame(String filName) {
    File gmfile = new File(filName);
    try {
      if (!gmfile.createNewFile()) {
        System.out.println("Did not create " + gmfile);
        return;
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    try (PrintWriter writeSGF = new PrintWriter(new BufferedWriter(new FileWriter(gmfile)))) {
      writeSGF.print(gm.toString());
      writeSGF.flush();
    } catch (IOException e) {
      System.out.println("Couldn't save game:" + e);
    }
  }

  /**
   * Take back the last move.
   * <p>
   * The move is either retained, retained with notation, or deleted
   * depending on the setting of <code>Game._markUndos</code> and
   * <code>Game.remUndo</code>.
   */
  @SuppressWarnings("unused")
  public void undoMove() {
    if (currPos > 0) {
      gm.undoMove();
      positions.remove(currPos);
      currPos--;
    }
  }

  /**
   * Resign the game.
   * <p>
   * This sets the result for the game, and prevents further move entry.
   */
  @SuppressWarnings("unused")
  public void doResign() {
    if (gm.isWMove()) {
      gm.setGameResult("B+R");
    } else {
      gm.setGameResult("W+R");
    }
    gm.setGameOver(true);
  }

  /**
   * Play a stone at the specified coordinate.
   * <p>
   * If the move is legal (determined by <code>ruleImp.isLegalMove</code>),
   * then a stone is placed at the specified coordinates and the liberties
   * of the neighboring stones of opposing color are counted. Any opposing
   * groups with zero liberties are captured, and whiteHasCap or
   * blackHasCap is incremented appropriately.
   *
   * @param x The horizontal coordinate at which to place the stone.
   * @param y The vertical coordinate at which to place the stone.
   */

  public void doMove(int x, int y) throws IllegalMoveException {
    PointOfPlay proposedMove = new PointOfPlay(x, y);
    boolean legalMove = ruleImp.isLegalMove(proposedMove, this);
    if (!legalMove) {
      throw new IllegalMoveException(proposedMove, positions.get(currPos));
    }
    if (!gm.isGameOver()) {
      boolean wmove = isWhiteMove();
      PointOfPlay dir;
      positions.add(new Position(positions.get(currPos++),
          gm.doMove(x, y)));
      Position temp = positions.get(currPos);

      dir = new PointOfPlay(x, y + 1);
      if (isOnBoard(dir) && temp.stoneAt(dir)
          && temp.blackAt(dir) == wmove
          && (countLiberties(dir) == 0)) {
        captureGroup(dir);
      }
      dir = new PointOfPlay(x + 1, y);
      if (isOnBoard(dir) && temp.stoneAt(dir)
          && temp.blackAt(dir) == wmove
          && (countLiberties(dir) == 0)) {
        captureGroup(dir);
      }
      dir = new PointOfPlay(x, y - 1);
      if (isOnBoard(dir) && temp.stoneAt(dir)
          && temp.blackAt(dir) == wmove
          && (countLiberties(dir) == 0)) {
        captureGroup(dir);
      }
      dir = new PointOfPlay(x - 1, y);
      if (isOnBoard(dir) && temp.stoneAt(dir)
          && temp.blackAt(dir) == wmove
          && (countLiberties(dir) == 0)) {
        captureGroup(dir);
      }

      // check self-capture.
      if (ruleImp.isSelfCaptureAllowed()) {
        if (countLiberties(proposedMove) == 0) {
          captureGroup(dir);
        }
      }
    } else {
      System.err.println("Warning: move after end of game ignored");
    }
  }

  /**
   * Get a <em>copy</em> of the current position.
   * <p>
   * The object returned is a clone of the object pointed to by
   * <code>currPos</code>, and therefore modifications to it will
   * not be reflected on the board.
   *
   * @return A clone of the current {@link Position position}
   */

  public Position getCurrPos() // send out a copy of the current position
  {                            // (we wouldn't want it modified directly!)

    Position temp = new Position();  // just to keep the compiler happy

    try                              // now throw it away...
    {
      temp = (Position) positions.get(currPos).clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      System.exit(0);                 // don't even try to recover from this!
    }
    return temp;
  }

  /**
   * Find out how many liberties the group occupying a given point has.
   * <p>
   * This method invokes {@link
   * AbstractRules#countLibs(PointOfPlay, int, MarkablePosition, Board)
   * AbstractRules.countLibs(PointOfPlay, int, MarkablePosition)} and
   * returns the result.
   *
   * @param p A point specifying a stone that is a member of the
   *          group in question.
   * @return The number of liberties of the group at point p
   */

  @SuppressWarnings("WeakerAccess")
  public int countLiberties(PointOfPlay p) {
    return ruleImp.countLibs(p, 0, null, this);
  }

  /**
   * Capture a single stone at point p for the opposing player.
   *
   * @param p The point on which the stone lies.
   */
  private void captureStone(PointOfPlay p) {
    Position tmp = positions.get(currPos);
    if (tmp.stoneAt(p)) {
      if (tmp.blackAt(p)) {
        ++whiteHasCap;
      } else {
        ++blackHasCap;
      }
      tmp.removeStoneAt(p);
    }

  }

  /**
   * Count the number of stones that are members of a group.
   *
   * @param p A point specifying a stone that is a member of the
   *          group in question.
   * @return The number of stones belonging to the group.
   */
  @SuppressWarnings("unused")
  public int countGroup(PointOfPlay p) {
    return enumerateGroup(p).size();
  }

  /**
   * Capture all the members of a group for the oposing player.
   * <p>
   * This method calls {@link #enumerateGroup(PointOfPlay) enumerateGroup}
   * to get a list of the stones in the group, and then iteratively
   * uses {@link #captureStone(PointOfPlay) captureStone} to capture them.
   *
   * @param p A point specifying a stone that is a member of the
   *          group in question.
   * @return The number of stones captured.
   */
  @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
  public int captureGroup(PointOfPlay p) {
    Set<PointOfPlay> groupList = enumerateGroup(p);
    int result = groupList.size();

    for (Object aGroupList : groupList) {
      captureStone((PointOfPlay) aGroupList);
    }

    return result;
  }

  /**
   * Get a <code>java.util.HashSet</code> containing all members of a group.
   * <p>
   * This method simply calls the private method <code>getGroupSet</code>
   * which to set up a call to <code>MarkablePosition.getGroupSet</code>.
   *
   * @param p A point specifying a stone that is a member of the
   *          group in question.
   * @return A reference to a <code>HashSet</code> of stones in
   * a group.
   * @see MarkablePosition#getGroupSet
   */

  private Set<PointOfPlay> enumerateGroup(PointOfPlay p) {
    return getGroupSet(p);
  }

  /**
   * Get a <code>java.util.HashSet</code> containing all members of a group.
   * <p>
   * This method simply calls <code>MarkablePosition.getGroupSet</code>.
   *
   * @param p A point specifying a stone that is a member of the
   *          group in question.
   * @see MarkablePosition#getGroupSet(PointOfPlay, Set, int)
   */

  private Set<PointOfPlay> getGroupSet(PointOfPlay p) {
    MarkablePosition temp = new MarkablePosition(getCurrPos());
    return temp.getGroupSet(p, null, getBoardSize());
  }

  /**
   * Sets up a particular position on the board. It is entirely legal to overwrite existing
   * stones, though it is bad style to overwrite an existing stone with one of the same color.
   *
   * @param white a list of points that should contain white stones
   * @param black a list of points that should contain black stones
   * @param empty a list of points that should not contain a stone
   * @param blackToMove true if black should move first in the resulting position,
   *                    false if white should move first.
   */
  public void setUp(List<PointOfPlay> white, List<PointOfPlay> black, List<PointOfPlay> empty, boolean blackToMove) {
    Move initialMove = this.gm.getCurrMove();
    white.forEach(p -> this.gm.doSetup(Move.MOVE_WHITE, p.getX(), p.getY(), blackToMove));
    black.forEach(p -> this.gm.doSetup(Move.MOVE_BLACK, p.getX(), p.getY(), blackToMove));
    empty.forEach(p -> this.gm.doSetup(Move.EMPTY, p.getX(), p.getY(), blackToMove));
    if (initialMove == this.gm.getCurrMove()) {
      // was already a setup move to which we added points, need to recreate the existing position.
      positions.remove(currPos);
      positions.add(new Position(positions.get(currPos), this.gm.getCurrMove()));
    } else {
      // we created a new setup move
      positions.add(new Position(positions.get(currPos++), this.gm.getCurrMove()));
    }
  }
}






