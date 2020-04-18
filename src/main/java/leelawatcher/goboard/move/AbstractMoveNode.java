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
public class AbstractMoveNode implements Move {

  protected int x;                // horizontal coordinate 0-18
  protected int y;                // vertical coordinate 0-18
  protected char color;           // 'B' or 'W' or 'S' or 'R'
  protected int moveNum;          // depth into the tree
  protected Move parent;          // Move that came before
  protected List<Move> children;      // Moves that come after
  protected String comment;       // Where people demonstrate their (lack of?)
  //    knowledge
  protected char colorMoveNext;   // who gets to place the next stone (B||W)

  protected List<PointOfPlay> addBlack;      // vectors to store PointOfPlay objects
  protected List<PointOfPlay> addWhite;      // in setup nodes.
  protected List<PointOfPlay> addEmpty;

  // to help keep track of objects, and debug problems, all objects number
  // their instances.
  protected static long numInstances = 0;
  protected long numThis;

  public boolean isSetup() {
    return (color == SetupNode.MOVE_SETUP);
  }

  public boolean isRoot() {
    return (color == RootNode.MOVE_ROOT);
  }

  public boolean isMove() {
    return (color == MOVE_WHITE || color == MOVE_BLACK);
  }

  public boolean isWhite() {
    return (color == AbstractMoveNode.MOVE_WHITE);
  }

  public boolean isBlack() {
    return (color == AbstractMoveNode.MOVE_BLACK);
  }

  public boolean isPass() {
    return isPass(x,y);
  }

  public static boolean isPass(int x, int y) {
    return ((x == AbstractMoveNode.PASS) && (y == AbstractMoveNode.PASS));
  }

  public long ID() {
    return numThis;
  }

  public void dPrint() {
    Iterator<PointOfPlay> empty = addEmpty.iterator();
    Iterator<PointOfPlay> white = addWhite.iterator();
    Iterator<PointOfPlay> black = addBlack.iterator();
    int numChildren = children.size();

    System.out.println("Move instance " + numThis + " of "
        + numInstances + ":");
    System.out.println("moveNum=" + moveNum);
    System.out.println("x=" + x);
    System.out.println("y=" + y);
    System.out.println("color=" + color);
    System.out.println("parent=/n- - - - - - -");
    parent.dPrint();
    System.out.println("- - - - - - -");
    System.out.println("There are " + numChildren + " children");
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

  public int numChildren() {
    return children.size();
  }

  public void removeChild(Move which) {
    children.remove(which);
  }

  public void addChild(Move child) {
    children.add(child);
  }

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

  public void setupWhite(int x, int y) {
    setup(x, y, addWhite, addEmpty, addBlack);
  }

  public void setupBlack(int x, int y) {
    setup(x, y, addBlack, addWhite, addEmpty);
  }

  public void undoSetupEmpty(int x, int y) {
    PointOfPlay temp = new PointOfPlay(x, y);

    if (this.isMove())             // setup nodes and moves MUST not be
      return;                    // mixed!

    remDup(temp, addEmpty);
  }

  public void undoSetupWhite(int x, int y) {
    PointOfPlay temp = new PointOfPlay(x, y);

    if (this.isMove())             // setup nodes and Moves MUST not be
      return;                    // mixed!

    remDup(temp, addWhite);
  }

  public void undoSetupBlack(int x, int y) {
    PointOfPlay temp = new PointOfPlay(x, y);
    Iterator<PointOfPlay> points = addBlack.iterator();

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

  public int yEnglish() {
    return (y + 1);
  }

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

  public char xSGF(int xNum) {
    return (xNum == AbstractMoveNode.PASS) ? ' ' : (char) ('a' + xNum);
  }

  public char ySGF(int yNum) {
    return (yNum == AbstractMoveNode.PASS) ? ' ' : (char) ('a' + 18 - yNum); // SGF format a,a is upper left corner
  }

  public String toString() {
    StringBuilder temp = new StringBuilder();

    if (this.isMove()) {
      temp.append(color);
      temp.append("[").append(xSGF(x)).append(ySGF(y)).append("]");
    } else {
      if (addEmpty.size() > 0) {
        Iterator<PointOfPlay> points = addEmpty.iterator();
        temp.append("AE");
        while (points.hasNext()) {
          PointOfPlay thisOne = points.next();
          temp.append("[").append(xSGF(thisOne.getX())).append(ySGF(thisOne.getY())).append("]");
        }
      }
      if (addWhite.size() > 0) {
        Iterator<PointOfPlay> points = addWhite.iterator();
        temp.append("AW");
        while (points.hasNext()) {
          PointOfPlay thisOne = points.next();
          temp.append("[").append(xSGF(thisOne.getX())).append(ySGF(thisOne.getY())).append("]");
        }
      }
      if (addBlack.size() > 0) {
        Iterator<PointOfPlay> points = addBlack.iterator();
        temp.append("AB");
        while (points.hasNext()) {
          PointOfPlay thisOne = points.next();
          temp.append("[").append(xSGF(thisOne.getX())).append(ySGF(thisOne.getY())).append("]");
        }
      }
    }
    if (!"".equals(comment))
      temp.append("C[").append(comment).append("]");

    return temp.toString();
  }

  public Move next() {
    if (children.size() == 0)
      return this;
    return children.get(0);
  }

  public Move next(int variation) {
    if (variation >= 0 && variation <= children.size())
      return children.get(variation);
    else if (children.size() == 0)
      return this;
    else
      return children.get(0);
  }

  public Move getParent() {
    return parent;
  }

  public void setColorMoveNext(char nextColor) {
    if ((color == SetupNode.MOVE_SETUP) || (color == RootNode.MOVE_ROOT)) {
      colorMoveNext = nextColor;
    } else {
      String msg = "Can only set colorNextMove in setup/root Moves!";
      throw new UnsupportedOperationException(msg);
    }
  }

  public char getColorNextMove() {
    return colorMoveNext;
  }

  public int getMoveNum() {
    return moveNum;
  }

  public void setComment(String aComment) {
    comment = aComment;
  }

  public String getComment() {
    return comment;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public char getColor() {
    return color;
  }

  public List<PointOfPlay>[] getSetupInfo() {
    @SuppressWarnings("unchecked")
    List<PointOfPlay>[] tmp = new List[3];

    tmp[0] = Collections.unmodifiableList(addEmpty);
    tmp[1] = Collections.unmodifiableList(addBlack);
    tmp[2] = Collections.unmodifiableList(addWhite);

    return tmp;
  }
}
