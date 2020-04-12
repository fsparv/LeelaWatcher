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

package leelawatcher.goboard.move;

import leelawatcher.goboard.PointOfPlay;

import java.util.List;


public interface Move {

  /**
   * <code>MOVE_WHITE</code> is the character that should be used to
   * designate a white move throughout the application.
   */
  char MOVE_WHITE = 'W';

  /**
   * <code>MOVE_BLACK</code> is the character that should be used to
   * designate a black move throughout the application.
   */
  char MOVE_BLACK = 'B';
  char EMPTY = 'E';

  /**
   * <code>MOVE_ROOT</code> is the character that should be used to
   * designate the root node of any tree of moves.
   */
  char MOVE_ROOT = 'R';

  /**
   * <code>MOVE_SETUP</code> is the character that should be used to
   * designate a setup move within any tree of moves.
   */
  char MOVE_SETUP = 'S';

  /**
   * The coordinate value that indicates a pass.
   */
  int PASS = 99;

  /**
   * Tests a move to see if it is a setup move.
   *
   * @return <code>true</code> if <code>color == Move.MOVE_SETUP</code> for
   * the calling object, <code>false</code> otherwise.
   */
  boolean isSetup();

  /**
   * Tests a move to see if it is a root move.
   *
   * @return <code>true</code> if <code>color == Move.MOVE_ROOT</code> for
   * the calling object, <code>false</code> otherwise.
   */
  boolean isRoot();

  /**
   * Tests a move to see if it is a "normal" move.
   *
   * @return <code>true</code> if <code>color</code> is either
   * <code>Move.MOVE_WHITE</code> or <code> Move.MOVE_BLACK</code>
   * for the calling object, <code>false</code> otherwise.
   */
  boolean isMove();

  /**
   * Tests if this move belongs to white.
   *
   * @return <code>true</code> if <code>color</code> is equal to
   * <code>Move.MOVE_WHITE</code>, <code>false</code> otherwise.
   */
  boolean isWhite();

  /**
   * Tests if this move belongs to black.
   *
   * @return <code>true</code> if <code>color</code> is equal to
   * <code>Move.MOVE_WHITE</code>, <code>false</code> otherwise.
   */
  boolean isBlack();

  /**
   * Test if this move is a pass.
   *
   * @return <code>true</code> if the move coordinates match
   * <code>Move.PASS</code>
   */
  boolean isPass();

  /**
   * Returns the number of this instance.
   * <p>
   * In order to keep track of how many moves are being created by
   * various operations during debugging, and to identify moves uniquely
   * this method returns a long for every move that has been created.
   * This long is a simple count of how many Move objects have been created
   * since the program was started. If more moves than can be recorded in a
   * long have been created, it is presumed that the program is failing in
   * an infinite loop anyway, and the ID number is no longer important.
   * Though there will be ways for the user to create and destroy moves,
   * through the normal functioning of the program, a user who managed to
   * create and destroy 1000 moves every second would have to continue this
   * for over a half billion years to exhaust the supply of ID numbers, so
   * I'm not too worried about this.
   *
   * @return A unique long integer for each <code>Move</code> object.
   */
  long ID();

  /**
   * Outputs the contents of all variables in the class for debugging.
   * <p>
   * This is for debugging purposes only. The output is of the form
   * variable_name=variable_value for each variable in the class.
   * The output for each variable is seperated by a newline, and all
   * output is sent directly to standard output via
   * <code>System.out.println</code>
   */
  void dPrint();

  /**
   * Returns the number of child moves (leaves) are attached at this node.
   *
   * @return An integer representing the number of variations currently
   * attached as children (leaves) to this node on the tree of
   * moves.
   */
  int numChildren();

  /**
   * Remove the specified child and its associated variations.
   * <p>
   * This method simply removes the reference to the child in the
   * <code>children</code> vector. If there are no other references to
   * the removed child, this will result in the eventual garbage
   * collection of the removed move and all attached children to which
   * there are no active external references.
   *
   * @param which A reference to the move to be removed from the
   *              <code>children</code> vector.
   */
  void removeChild(Move which);

  /**
   * Add the specified child to the <code>children</code> vector.
   * <p>
   * This method simply the reference to the <code>Move</code> object
   * specified by <code>child</code> to the <code>children</code> vector.
   *
   * @param child A reference to the move to be added to the
   *              <code>children</code> vector.
   */
  void addChild(Move child);

  /**
   * Add a {@link PointOfPlay} to be cleared of all stones in a setup node.
   * <p>
   * Setup lists (the vector fields <code>addEmpty</code>,
   * <code>addBlack</code> and <code>addWhite</code>)
   * should be exclusive. A <code>PointOfPlay</code> should
   * never occur in more than one list for any Move. Thus, adding one
   * type of <code>PointOfPlay</code> deletes any instances of that
   * <code>PointOfPlay</code> in the other two lists. Basically only
   * one list can own a <code>PointOfPlay</code> at any given time.
   * Adding a black stone where one already exists is also undesireable,
   * but that will be handled by the board object, which has knowledge
   * of the current position.
   * <p>
   * <p> Exclusivity of <code>PointOfPlays</code> in setup lists is
   * enforced by the removal of any duplicate instance of
   * <code>PointOfPlay</code> in the setup lists. This means that
   * only the most recent designation of a <code>PointOfPlay</code> as
   * an empty, white or black point on the board will be retained.
   *
   * @param x The horizontal displacement from the lower left corner
   * @param y The vertical displacement from the lower left corner
   */
  void setupEmpty(int x, int y);

  /**
   * Add a {@link PointOfPlay} where a white stone should be added to
   * the board in a setup node.
   * <p>
   * Setup lists (the vector fields <code>addEmpty</code>,
   * <code>addBlack</code> and <code>addWhite</code>)
   * should be exclusive. A <code>PointOfPlay</code> should
   * never occur in more than one list for any Move. Thus, adding one
   * type of <code>PointOfPlay</code> deletes any instances of that
   * <code>PointOfPlay</code> in the other two lists. Basically only
   * one list can own a <code>PointOfPlay</code> at any given time.
   * Adding a black stone where one already exists is also undesireable,
   * but that will be handled by the board object, which has knowledge
   * of the current position.
   * <p>
   * <p> Exclusivity of <code>PointOfPlays</code> in setup lists is
   * enforced by the removal of any duplicate instance of
   * <code>PointOfPlay</code> in the setup lists. This means that
   * only the most recent designation of a <code>PointOfPlay</code> as
   * an empty, white or black point on the board will be retained.
   *
   * @param x The horizontal displacement from the lower left corner
   * @param y The vertical displacement from the lower left corner
   */
  void setupWhite(int x, int y);

  /**
   * Add a {@link PointOfPlay} where a Black stone should be added to
   * the board in a setup node.
   * <p>
   * Setup lists (the vector fields <code>addEmpty</code>,
   * <code>addBlack</code> and <code>addWhite</code>)
   * should be exclusive. A <code>PointOfPlay</code> should
   * never occur in more than one list for any Move. Thus, adding one
   * type of <code>PointOfPlay</code> deletes any instances of that
   * <code>PointOfPlay</code> in the other two lists. Basically only
   * one list can own a <code>PointOfPlay</code> at any given time.
   * Adding a black stone where one already exists is also undesireable,
   * but that will be handled by the board object, which has knowledge
   * of the current position.
   * <p>
   * <p> Exclusivity of <code>PointOfPlays</code> in setup lists is
   * enforced by the removal of any duplicate instance of
   * <code>PointOfPlay</code> in the setup lists. This means that
   * only the most recent designation of a <code>PointOfPlay</code> as
   * an empty, white or black point on the board will be retained.
   *
   * @param x The horizontal displacement from the lower left corner
   * @param y The vertical displacement from the lower left corner
   */
  void setupBlack(int x, int y);

  /**
   * Remove accidentally added empty setup points.
   * <p>
   * This method removes a reference to a <code>PointOfPlay</code> object
   * from <code>addEmpty</code>.
   * <p>
   * <i><b>This should be refactored</b></i> to combine all the undoSetupXXX
   * methods into one undoSetup method. This is possible since the lists
   * are exclusive of eachother. undoSetupXXX methods are not likely to be
   * called iteratively, so the added overhead is negligable.
   *
   * @param x The horizontal displacement from the lower left corner
   * @param y The vertical displacement from the lower left corner
   */
  void undoSetupEmpty(int x, int y);

  /**
   * Remove accidentally added white stones in a  setup node.
   * <p>
   * This method removes a reference to a <code>PointOfPlay</code> object
   * from <code>addWhite</code>.
   * <p>
   * <i><b>This should be refactored</b></i> to combine all the undoSetupXXX
   * methods into one undoSetup method. This is possible since the lists
   * are exclusive of eachother. undoSetupXXX methods are not likely to be
   * called iteratively, so the added overhead is negligable.
   *
   * @param x The horizontal displacement from the lower left corner
   * @param y The vertical displacement from the lower left corner
   */
  void undoSetupWhite(int x, int y);

  /**
   * Remove accidentally added black stones in a  setup node.
   * <p>
   * This method removes a reference to a <code>PointOfPlay</code> object
   * from <code>addBlack</code>.
   * <p>
   * <i><b>This should be refactored</b></i> to combine all the undoSetupXXX
   * methods into one undoSetup method. This is possible since the lists
   * are exclusive of eachother. undoSetupXXX methods are not likely to be
   * called iteratively, so the added overhead is negligable.
   *
   * @param x The horizontal displacement from the lower left corner
   * @param y The vertical displacement from the lower left corner
   */
  void undoSetupBlack(int x, int y);

  /**
   * Returns the y coordinate as normally represented in english texts.
   * <p>
   * The normal representation in english texts is an integer between
   * 1 and the size of the board (inclusive). The move object has no
   * way to check on the size of the board, and thus must rely on client
   * classes to ensure that only moves that are on the board are entered.
   *
   * @return The vertical displacement from the lower left corner
   * as an <code>int</code>
   */
  int yEnglish();

  /**
   * Returns the x coordinate as normally represented in english texts.
   * <p>
   * The normal representation in english texts is an alphabetic
   * character between <code>'a'</code> and the size of the board
   * (inclusive) skipping the letter 'i'. The move object has no
   * way to check on the size of the current board, and thus must rely
   * on client classes to ensure that only moves that are on the board
   * are entered. If off board moves are entered, this method will
   * report them as any other, potentially as unprintable characters.
   * <p>
   * <p> Currently <b>board sizes over 25 are not well supported</b> by
   * this method. Though this support will likely be added in the future.
   *
   * @return The vertical displacement from the lower left corner
   * as an <code>char</code>
   */
  char xEnglish();

  /**
   * Returns the x coordinate as represented in the SGF file format.
   * <p>
   * SGF uses the upper right as the origin and alphabetic characters to
   * represent displacement from the upper right. The <code>Move</code>
   * object has no way to check on the size of the current board, and
   * thus must rely on client classes to ensure that only moves that
   * are on the board are entered.
   *
   * @param xNum An integer to be converted to an SGF x coordinate
   * @return The horizontal displacement from the <b>upper</b> left
   * corner as a <code>char</code>
   */
  char xSGF(int xNum);

  /**
   * Returns the y coordinate as represented in the SGF file format.
   * <p>
   * SGF uses the upper right as the origin and alphabetic characters to
   * represent displacement from the upper right. The <code>Move</code>
   * class has no way to check on the size of the current board, and thus
   * must rely on client classes to ensure that only moves that are on
   * the board are entered. Note that currently <b>only 19x19 size boards</b>
   * are supported by this method
   *
   * @param yNum An integer to be converted to an SGF y coordinate
   * @return The vertical displacement from the <b>upper</b> left
   * corner as a <code>char</code>
   */
  char ySGF(int yNum);

  /**
   * Returns a string representation of the <code>Move</code> object.
   * <p>
   * The representation returned by this function is in SGF format.
   * The SGF representations are as follows:
   * <ul>
   * <li> Normal Move: "W[xy]" or "B[xy]" for white or black play</li>
   * <li> Add Empty:   "AE[xy][xy]...[xy]"</li>
   * <li> Add Black:   "AB[xy][xy]...[xy]"</li>
   * <li> Add White:   "AW[xy][xy]...[xy]"</li>
   * <li> Comment:     "C[comment text]"</li>
   * </ul>
   * In all cases above, x is the character returned by the <code>xSGF</code>
   * method representing the horizontal displacement and y is the character
   * returned by the <code>ySGF</code> method representing vertical
   * displacement. For comments comment text is the contents of the coment
   * instance variable.
   * <p>
   * <p> For example, a white move at with x=3 and y=4 on a 19x19 board
   * and a comment="slow move, j8 much better." would be returned as
   * "W[dp]C[slow move, j8 much better.]"
   *
   * @return A string representing the move in SGF format.
   */
  String toString();

  /**
   * Get a reference to the primary (first added) child.
   * <p>
   * Currently the primary child is the first one. Later when GUI support
   * for variations is added the <code>Move</code> class will need to
   * have a variable specifying what the current primary variation is.
   * This method will probably be deprecated in the near future. Use
   * next(int) instead.
   *
   * @return A reference to the next move in the primary variation.
   */
  Move next();

  /**
   * Get a reference to the specified child (variation).
   * <p>
   * Returns the child move specified by variation. If there are no
   * children it returns itself, and if the requested variation does
   * not exist it currently returns the primary (first added) child.
   * This last behavior may be changed to throw an exception instead, as
   * this may become a source of odd behavior, and difficult bugs.
   *
   * @param variation Specifies the variation number to requested.
   * @return A reference to the next move in the move tree for
   * the requested variation.
   */
  Move next(int variation);

  /**
   * Get a reference to the parent of this move.
   * <p>
   * This method allows traversal back up the tree without exposing the
   * back reference to the parent move to modification. This ensures that
   * variations cannot be pruned from the move tree accidentally.
   *
   * @return A reference to the parent move one level up the move tree.
   */
  Move getParent();

  /**
   * Sets the color that should move next for root moves or setup moves.
   * <p>
   * Attempting to change which player moves next in a "normal" Move (color
   * of either <code>Move.MOVE_WHITE</code> or <code>Move.MOVE_BLACK</code>)
   * will cause an <code>UnsupportedOperationException</code> to be thrown.
   * to be thrown.
   *
   * @param nextColor Indicates which color is to be played on the next
   *                  turn of play.
   * @throws UnsupportedOperationException When it is invoked on a "normal move"
   */
  void setColorMoveNext(char nextColor);

  /**
   * Queries to find out what the next color to be played is.
   *
   * @return Either </code>Move.MOVE_WHITE</code> or
   * </code>Move.MOVE_BLACK</code>.
   */
  char getColorNextMove();

  /**
   * Queries to find out how many moves have been played in the game so far.
   * <p>
   * The depth in the tree is different from the ID num since there may
   * Setup moves do not increment move depth, and the tree structure
   * allows variations to eat up any number of ID numbers on a given level
   * of the tree, but every (non-setup) move attached at a given level in
   * the tree should have the same moveNum. It is possible that setup moves
   * will need to be allowed to change the moveNum in order to conform to
   * the SGF format, this has not been fully investigated.
   *
   * @return The move number for this move.
   */
  int getMoveNum();

  /**
   * Add a comment about this move.
   * <p>
   * Comments are where advice or notations about the game are stored.
   *
   * @param aComment The comment about the move in string form.
   */
  void setComment(String aComment);

  /**
   * Queries to get the comments for this move.
   * <p>
   * This is where we look to find out what good(?) advice has been
   * given about this move by a reviewer or observer, or one of the players.
   *
   * @return A string containing the comments for this move.
   */
  String getComment();

  /**
   * Queries to find out the x coordinate of this move.
   *
   * @return The horizontal displacement from the lower left corner.
   */
  int getX();

  /**
   * Queries to find out the y coordinate of this move.
   *
   * @return The vertical displacement from the lower left corner.
   */
  int getY();

  /**
   * Queries to find out the color of the current move.
   * <p>
   * Note that the color can be any of the 4 MOVE_XXX constants, and
   * so this method generally should only be used after a check with
   * <code>isMove()</code>. Use of <code>isSetup</code> and
   * <code>isRoot()</code> is reccomended instead of direct divination
   * of the move type from the return value of this method.
   *
   * @return One of <code>MOVE_ROOT|MOVE_SETUP|MOVE_WHITE|MOVE_BLACK</code>
   */
  char getColor();

  /**
   * Return an array of vectors containing the setup information for this
   * instance.
   * <p>
   * The first Vector contains points specified as empty. The second
   * contains points specified as black stones. The third contains
   * points specified as white stones.
   *
   * @return A copy of (not reference to) the setup information in
   * this instance.
   */
  List<PointOfPlay>[] getSetupInfo();
}
