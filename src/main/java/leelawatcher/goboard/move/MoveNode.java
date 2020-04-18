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

import java.util.ArrayList;


public class MoveNode extends AbstractMoveNode {

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
  public MoveNode(int xcoor, int ycoor, char pcolor, Move parentMove) {
    numInstances++;        // keep track of how many have been created
    numThis = numInstances;

    // On a standard 19x19 board:
    x = xcoor;             // 0-18 0 on left -1 to resign 19 to pass
    y = ycoor;             // 0-18 0 on bottom -1 to resign 19 to pass

    if (pcolor != MOVE_BLACK && pcolor != MOVE_WHITE) {
      String msg = "Moves must be black or white!";
      throw new IllegalArgumentException(msg);
    }

    if (parentMove.isMove() && (pcolor == parentMove.getColor())) {
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
}
