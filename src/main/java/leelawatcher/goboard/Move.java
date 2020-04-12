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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class will be used to model a tree of Moves.
 * <p>
 * The branches of the tree represent variations, or moves undone. The
 * tree may have any number of leaves at each node. Tree traversal is
 * not included as a method. The order of the nodes is based entirely on
 * order of entry since there is no sensible way to sort Moves.
 * <p>
 * <p>Setup nodes occur within the tree and any Move related information stored
 * in a node containing setup information should be ignored. Setup nodes
 * should not add to the MoveNum variable as a setup does not constitute a
 * Move. Setups in the middle of the tree are allowed in order to support
 * the SGF file format.
 * <p>
 * <p><b><i>This class is due for a refactoring</i></b>, it does too much. It
 * should be divided into a MoveNode, a SetupNode and a RootNode which inherit
 * from an AbstractNode. This will also bring the object model in closer
 * correspondence with the structure of an SGF file.
 * <p>
 * <p>There will also be a need to write a GameInfo class and provide a place
 * to attach it to the first distinguishing move in a tree of games beginning
 * with the move sequence. This will not be necessary until either loading of
 * ANY SGF file is supported, or creation of multi-game trees is supported.
 * <p>
 * <p> It is important to note that while some methods here will work for
 * board sizes over or under 19x19 <b>only 19x19 is fully supported</b> by all
 * the methods in this class at this time. Some Functionality may need to be
 * swapped out to other classes (particularly in the realm of generating
 * SGF output) to support other board sizes.
 *
 * @author Patrick G. Heck
 * @version 0.1
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Move {
  /**
   * <code>MOVE_WHITE</code> is the character that should be used to
   * designate a white move throughout the application.
   */
  public final static char MOVE_WHITE = 'W';

  /**
   * <code>MOVE_BLACK</code> is the character that should be used to
   * designate a black move throughout the application.
   */
  public final static char MOVE_BLACK = 'B';
  public static final char EMPTY = 'E';

  /**
   * <code>MOVE_ROOT</code> is the character that should be used to
   * designate the root node of any tree of moves.
   */
  private final static char MOVE_ROOT = 'R';

  /**
   * <code>MOVE_SETUP</code> is the character that should be used to
   * designate a setup move within any tree of moves.
   */
  private final static char MOVE_SETUP = 'S';

  /**
   * The coordinate value that indicates a pass.
   */
  public static final int PASS = 99;

  /**
   * The value representing a setup move.
   */
  @SuppressWarnings("unused")
  public static final int SETUP = 98;


  /**
   * The maximum supported board size.
   */
  @SuppressWarnings("unused")
  public static final int MAX_SIZE = 19;

  private int x;                // horizontal coordinate 0-18
  private int y;                // vertical coordinate 0-18
  private char color;           // 'B' or 'W'
  private int moveNum;          // depth into the tree
  private Move parent;          // Move that came before
  private List<Move> children;      // Moves that come after
  private String comment;       // Where people demonstrate their (lack of?)
  //    knowledge
  private char colorMoveNext;   // who gets to place the next stone (B||W)

  private List<PointOfPlay> addBlack;      // vectors to store PointOfPlay objects
  private List<PointOfPlay> addWhite;      // in setup nodes.
  private List<PointOfPlay> addEmpty;

  // to help keep track of objects, and debug problems, all objects number
  // their instances.

  private static long numInstances = 0;
  private long numThis;

  /**
   * Constructor to create the root of a game tree.
   * <p>
   * Only should be used for root nodes. The color will be set to MOVE_ROOT
   * and this node is it's own parent. A root move can contain setup Info,
   * usually handicap placement
   */
  public Move() {

    numInstances++;        // keep track of how many have been created
    numThis = numInstances;


    x = 99;              // since this is the root, no stone is placed
    y = 99;              // making it a pass

    color = MOVE_ROOT;   // mark it as a root node (colorless)

    moveNum = 0;         // This is the root Move, not a real Move

    parent = this;       // This is the root Move and thus it's own parent

    children = new ArrayList<>(1);

    addBlack = new ArrayList<>(10);         // Handicaps most often
    addWhite = new ArrayList<>(10);         // For setup of white stones
    addEmpty = new ArrayList<>(10);         // For erasure of stones

    comment = "";
  }

  /**
   * Constructor to use for setup Moves.
   * <p>
   * Generally a setup move will most commonly occur right after the
   * root move to place handicap stones if they havnt been placed in
   * the root move. It is not legal to add setup information to a
   * standard move and move information cannot be added to a setup move.
   * This restriction is neccesary for compliance with the SGF format.
   * Setup moves are otherwise allowed anywhere in the game tree, but
   * they are usually only useful for sgf files that describe problem sets,
   * or are annotated with alternate positions. Setup information must be
   * added after the move is created using Move.setupEmpty, Move.setupBlack
   * or Move.setupWhite methods.
   *
   * @param parentMove The move preceeding this one in the tree.
   */
  @SuppressWarnings("unused")
  public Move(Move parentMove) {
    numInstances++;        // keep track of how many have been created
    numThis = numInstances;

    // On a standard 19x19 board:
    x = 98;                // 0-18 0 on left -1 to resign 19+ to pass
    y = 98;                // 0-18 0 on botom -1 to resign 19+ to pass

    color = MOVE_SETUP;    // 'S' indicates setup Move

    colorMoveNext = parentMove.colorMoveNext;

    moveNum = parentMove.getMoveNum();    // find out who is before us
    // but don't increment

    parent = parentMove;                  // Supprised?
    parentMove.addChild(this);            // inform Parent of new child

    children = new ArrayList<>(1);           // A place to put it's children

    addBlack = new ArrayList<>(10);         // Handicaps most often
    addWhite = new ArrayList<>(10);         // For setup of white stones
    addEmpty = new ArrayList<>(10);         // For erasure of stones

    comment = "";

  }

  /**
   * Constructor to use for normal Moves.
   * <p>
   * This constructor creates a "normal" move by adding it to
   * <code>parentMove</code>, setting the x and y coordinates and
   * setting the color. It is not legal to try to attach a black move
   * to a black parent move, or a white move to a white parent move,
   * and only black and white moves may be created with this constructor
   * (no setup or root nodes). In the future, this constructor may set
   * the vectors for setup moves to null since they shouldn't be used
   * on an object created with this constructor.
   *
   * @param xcoor      The horizontal displacement from the lower left corner
   * @param ycoor      The vertical displacement from the lower left corner
   * @param pcolor     The color for this move.
   * @param parentMove The move to which this move is attached in the game
   *                   tree.
   * @throws IllegalArgumentException If pcolor is not <code>Move.MOVE_BLACK</code> or
   *                                  <code>Move.MOVE_WHITE</code>
   * @throws IllegalArgumentException If pcolor is of the same color as the parent move
   */
  public Move(int xcoor, int ycoor, char pcolor, Move parentMove) {
    numInstances++;        // keep track of how many have been created
    numThis = numInstances;

    // On a standard 19x19 board:
    x = xcoor;             // 0-18 0 on left -1 to resign 19 to pass
    y = ycoor;             // 0-18 0 on bottom -1 to resign 19 to pass

    if (pcolor != MOVE_BLACK && pcolor != MOVE_WHITE) {
      String msg = "Moves must be black or white!";
      throw new IllegalArgumentException(msg);
    }

    if (parentMove.isMove() && (pcolor == parentMove.color)) {
      String msg = "Attempted to move the same color twice!";
      throw new IllegalArgumentException(msg);
    }

    color = pcolor;        // 'B' or 'W'

    moveNum = parentMove.getMoveNum();    // find out who is before us
    moveNum++;                            // we are one more move into tree

    parent = parentMove;                  // Suprised?

    parentMove.addChild(this);            // inform Parent of new child

    children = new ArrayList<>(1);           // A place to put it's children

    addBlack = new ArrayList<>(1);         // shouldn't be used in move
    addWhite = new ArrayList<>(1);         // node, and may be set to null
    addEmpty = new ArrayList<>(1);         // in future versions

    comment = "";

  }

  /**
   * Tests a move to see if it is a setup move.
   *
   * @return <code>true</code> if <code>color == Move.MOVE_SETUP</code> for
   * the calling object, <code>false</code> otherwise.
   */
  @SuppressWarnings("WeakerAccess")
  public boolean isSetup() {
    return (color == MOVE_SETUP);
  }

  /**
   * Tests a move to see if it is a root move.
   *
   * @return <code>true</code> if <code>color == Move.MOVE_ROOT</code> for
   * the calling object, <code>false</code> otherwise.
   */
  public boolean isRoot() {
    return (color == MOVE_ROOT);
  }

  /**
   * Tests a move to see if it is a "normal" move.
   *
   * @return <code>true</code> if <code>color</code> is either
   * <code>Move.MOVE_WHITE</code> or <code> Move.MOVE_BLACK</code>
   * for the calling object, <code>false</code> otherwise.
   */
  public boolean isMove() {
    return (color == MOVE_WHITE || color == MOVE_BLACK);
  }

  /**
   * Tests if this move belongs to white.
   *
   * @return <code>true</code> if <code>color</code> is equal to
   * <code>Move.MOVE_WHITE</code>, <code>false</code> otherwise.
   */
  public boolean isWhite() {
    return (color == Move.MOVE_WHITE);
  }

  /**
   * Tests if this move belongs to black.
   *
   * @return <code>true</code> if <code>color</code> is equal to
   * <code>Move.MOVE_WHITE</code>, <code>false</code> otherwise.
   */
  public boolean isBlack() {
    return (color == Move.MOVE_BLACK);
  }

  /**
   * Test if this move is a pass.
   *
   * @return <code>true</code> if the move coordinates match
   * <code>Move.PASS</code>
   */
  public boolean isPass() {
    //System.out.println("(" + x + " == " + Move.PASS + ") && (" + y + " == " + Move.PASS + ")");
    return isPass(x,y);
  }

  public static boolean isPass(int x, int y) {
    return ((x == Move.PASS) && (y == Move.PASS));
  }

  /**
   * Returns the number of this instance.
   * <p>
   * In order to keep track of how many moves are being created by
   * various operations during debugging  and to identify moves uniquely
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
  public long ID() {
    return numThis;
  }

  /**
   * Outputs the contents of all variables in the class for debugging.
   * <p>
   * This is for debugging purposes only. The output is of the form
   * variable_name=variable_value for each variable in the class.
   * The output for each variable is seperated by a newline, and all
   * output is sent directly to standard output via
   * <code>System.out.println</code>
   */

  public void dPrint() {
    Iterator empty = addEmpty.iterator();
    Iterator white = addWhite.iterator();
    Iterator black = addBlack.iterator();
    int numchildren = children.size();

    System.out.println("Move instance " + numThis + " of "
        + numInstances + ":");
    System.out.println("moveNum=" + moveNum);
    System.out.println("x=" + x);
    System.out.println("y=" + y);
    System.out.println("color=" + color);
    System.out.println("parent=/n- - - - - - -");
    parent.dPrint();
    System.out.println("- - - - - - -");
    System.out.println("There are " + numchildren + " children");
    System.out.println("comment=" + comment);
    System.out.println("Setup vectors:");
    System.out.print("addEmpty=");
    while (empty.hasNext())
      System.out.print(empty.next());
    System.out.print("\naddWhite=");
    while (white.hasNext())
      System.out.print(white.next());
    System.out.print("\naddBlack=");
    while (black.hasNext())
      System.out.print(black.next());
    System.out.println("\n----------------");
  }

  /**
   * Returns the number of child moves (leaves) are attached at this node.
   *
   * @return An integer representing the number of variations currently
   * attached as children (leaves) to this node on the tree of
   * moves.
   */
  public int numChildren() {
    return children.size();
  }

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
  public void removeChild(Move which) {
    children.remove(which);
  }

  //
  // Add public void removeSelf(Move Which) method here?
  //

  /**
   * Add the specified child to the <code>children</code> vector.
   * <p>
   * This method simply the reference to the <code>Move</code> object
   * specified by <code>child</code> to the <code>children</code> vector.
   *
   * @param child A reference to the move to be added to the
   *              <code>children</code> vector.
   */
  public void addChild(Move child) {
    children.add(child);
  }

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
  public void setupEmpty(int x, int y) {
    setup(x, y, addEmpty, addWhite, addBlack);
  }

  private void setup(int x, int y, List<PointOfPlay> target, List<PointOfPlay> other1, List<PointOfPlay> other2) {
    PointOfPlay temp = new PointOfPlay(x, y);

    if (this.isMove())             // setup nodes and Moves MUST not be
      return;                      // mixed!

    // check if point is already in list of empty points
    for (PointOfPlay point : target) {
      if (point.equals(temp))
        return;
    }

    // now remove any duplicates
    remDup(temp, other1);
    remDup(temp, other2);

    target.add(temp);            // if we get here the point is unique
    // so we can go ahead and add it
  }

  private void remDup(PointOfPlay temp, List<PointOfPlay> list) {
    for (PointOfPlay thisOne : list) {
      if (thisOne.equals(temp)) {
        addWhite.remove(thisOne);
        break;
      }
    }
  }

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
  public void setupWhite(int x, int y) {
    setup(x, y, addWhite, addEmpty, addBlack);
  }

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
  public void setupBlack(int x, int y) {
    setup(x, y, addBlack, addWhite, addEmpty);
  }

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
  public void undoSetupEmpty(int x, int y) {
    PointOfPlay temp = new PointOfPlay(x, y);

    if (this.isMove())             // setup nodes and moves MUST not be
      return;                    // mixed!

    remDup(temp, addEmpty);
  }

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
  public void undoSetupWhite(int x, int y) {
    PointOfPlay temp = new PointOfPlay(x, y);

    if (this.isMove())             // setup nodes and Moves MUST not be
      return;                    // mixed!

    remDup(temp, addWhite);
  }


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
  public void undoSetupBlack(int x, int y) {
    PointOfPlay temp = new PointOfPlay(x, y);
    Iterator points = addBlack.iterator();

    if (this.isMove())             // setup nodes and Moves MUST not be
      return;                    // mixed!

    while (points.hasNext())        // check if point is already in list
    {
      PointOfPlay thisOne = (PointOfPlay) points.next();
      if (thisOne.equals(temp)) {
        addBlack.remove(thisOne);
        return;
      }
    }
  }

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
  public int yEnglish() {
    return (y + 1);
  }

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
  public char xEnglish() {
    if (x >= 9)
      return (char) ('a' + x + 1); // This is a convention used in books.
      // The letter i is skipped, probably
      // because of it's similarity to j
      // in some type faces. Since fonts may
      // be customizable it should be kept.
    else
      return (char) ('a' + x);
  }

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
  public char xSGF(int xNum) {
    return (xNum == Move.PASS) ? ' ' : (char) ('a' + xNum);
  }

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
  public char ySGF(int yNum) {
    return (yNum == Move.PASS) ? ' ' : (char) ('a' + 18 - yNum); // SGF format a,a is upper left corner
  }

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
  public String toString() {
    StringBuilder temp = new StringBuilder();

    if (this.isMove()) {
      temp.append(color);
      temp.append("[").append(xSGF(x)).append(ySGF(y)).append("]");
    } else {
      if (addEmpty.size() > 0) {
        Iterator points = addEmpty.iterator();
        temp.append("AE");
        while (points.hasNext()) {
          PointOfPlay thisOne = (PointOfPlay) points.next();
          temp.append("[").append(xSGF(thisOne.getX())).append(ySGF(thisOne.getY())).append("]");
        }
      }
      if (addWhite.size() > 0) {
        Iterator points = addWhite.iterator();
        temp.append("AW");
        while (points.hasNext()) {
          PointOfPlay thisOne = (PointOfPlay) points.next();
          temp.append("[").append(xSGF(thisOne.getX())).append(ySGF(thisOne.getY())).append("]");
        }
      }
      if (addBlack.size() > 0) {
        Iterator points = addBlack.iterator();
        temp.append("AB");
        while (points.hasNext()) {
          PointOfPlay thisOne = (PointOfPlay) points.next();
          temp.append("[").append(xSGF(thisOne.getX())).append(ySGF(thisOne.getY())).append("]");
        }
      }
    }
    if (!"".equals(comment))
      temp.append("C[").append(comment).append("]");

    return temp.toString();
  }

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
  public Move next() {
    if (children.size() == 0)
      return this;
    return children.get(0);
  }

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
  public Move next(int variation) {
    if (variation >= 0 && variation <= children.size())
      return children.get(variation);
    else if (children.size() == 0)
      return this;
    else
      return children.get(0);
  }

  /**
   * Get a reference to the parent of this move.
   * <p>
   * This method allows traversal back up the tree without exposing the
   * back reference to the parent move to modification. This ensures that
   * variations cannot be pruned from the move tree accidentally.
   *
   * @return A reference to the parent move one level up the move tree.
   */
  public Move getParent() {
    return parent;
  }

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
  public void setColorMoveNext(char nextColor) {
    if ((color == MOVE_SETUP) || (color == MOVE_ROOT)) {
      colorMoveNext = nextColor;
    } else {
      String msg = "Can only set colorNextMove in setup/root Moves!";
      throw new UnsupportedOperationException(msg);
    }
  }

  /**
   * Querys to find out what the next color to be played is.
   *
   * @return Either </code>Move.MOVE_WHITE</code> or
   * </code>Move.MOVE_BLACK</code>.
   */
  public char getColorNextMove() {
    return colorMoveNext;
  }

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
  public int getMoveNum() {
    return moveNum;
  }

  /**
   * Add a comment about this move.
   * <p>
   * Comments are where advice or notations about the game are stored.
   *
   * @param aComment The comment about the move in string form.
   */
  public void setComment(String aComment) {
    comment = aComment;
  }

  /**
   * Queries to get the comments for this move.
   * <p>
   * This is where we look to find out what good(?) advice has been
   * given about this move by a reviewer or observer, or one of the players.
   *
   * @return A string containing the comments for this move.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Queries to find out the x coordinate of this move.
   *
   * @return The horizontal displacement from the lower left corner.
   */
  public int getX() {
    return x;
  }

  /**
   * Queries to find out the y coordinate of this move.
   *
   * @return The vertical displacement from the lower left corner.
   */
  public int getY() {
    return y;
  }

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
  public char getColor() {
    return color;
  }

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
  public List<PointOfPlay>[] getSetupInfo() {
    @SuppressWarnings("unchecked")
    List<PointOfPlay>[] tmp = new List[3];

    tmp[0] = Collections.unmodifiableList(addEmpty);
    tmp[1] = Collections.unmodifiableList(addBlack);
    tmp[2] = Collections.unmodifiableList(addWhite);

    return tmp;
  }
}

