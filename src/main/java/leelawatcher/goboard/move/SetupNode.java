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


public class SetupNode extends AbstractMoveNode {

  /**
   * Constructor to use for setup Moves.
   * <p>
   * Generally a setup move will most commonly occur right after the
   * root move to place handicap stones if they havnt been placed in
   * the root move. It is not legal to add setup information to a
   * standard move and move information cannot be added to a setup move.
   * This restriction is necessary for compliance with the SGF format.
   * Setup moves are otherwise allowed anywhere in the game tree, but
   * they are usually only useful for sgf files that describe problem sets,
   * or are annotated with alternate positions. Setup information must be
   * added after the move is created using Move.setupEmpty, Move.setupBlack
   * or Move.setupWhite methods.
   *
   * @param parentMove The move preceding this one in the tree.
   */
  @SuppressWarnings("unused")
  public SetupNode(Move parentMove) {
    numInstances++;        // keep track of how many have been created
    numThis = numInstances;

    // On a standard 19x19 board:
    x = 98;                // 0-18 0 on left -1 to resign 19+ to pass
    y = 98;                // 0-18 0 on bottom -1 to resign 19+ to pass

    color = MOVE_SETUP;    // 'S' indicates setup Move

    colorMoveNext = parentMove.getColorNextMove();

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

}
