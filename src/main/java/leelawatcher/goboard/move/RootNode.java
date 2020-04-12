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

public class RootNode extends AbstractMoveNode {

  /**
   * Constructor to create the root of a game tree.
   * <p>
   * Only should be used for root nodes. The color will be set to MOVE_ROOT
   * and this node is it's own parent. A root move can contain setup Info,
   * usually handicap placement
   */
  public RootNode() {

    numInstances++;        // keep track of how many have been created
    numThis = numInstances;

    x = PASS;              // since this is the root, no stone is placed
    y = PASS;              // making it a pass

    color = MOVE_ROOT;   // mark it as a root node (colorless)

    moveNum = 0;         // This is the root Move, not a real Move

    parent = this;       // This is the root Move and thus it's own parent

    children = new ArrayList<>(1);

    addBlack = new ArrayList<>(10);         // Handicaps most often
    addWhite = new ArrayList<>(10);         // For setup of white stones
    addEmpty = new ArrayList<>(10);         // For erasure of stones

    comment = "";
  }
}
