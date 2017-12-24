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

/*
 * boardView.java
 *
 * Created on December 16, 2002, 12:59 AM
 */

package leelawatcher.gui;

import leelawatcher.goboard.Board;
import leelawatcher.goboard.IllegalMoveException;
import leelawatcher.goboard.Move;
import leelawatcher.goboard.PointOfPlay;

import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * @author Gus
 */
public class BoardView extends javax.swing.JPanel {

  private static final Dimension PREFERRED_SIZE = new Dimension(500, 500);

  private Board theGame;
  private ImageMaker goImages = new ImageMaker();

  /**
   * Creates new form boardView
   */
  BoardView(Board aBoard) {
    theGame = aBoard;
  }

  public void paint(java.awt.Graphics g) {
    // Find out how much space is available.
    super.paint(g);

    if (theGame == null) {
      return;
    }

    Container p = getParent();
    g.setColor(p.getBackground());
    int availH = getHeight();
    int availW = getWidth();
    g.fillRect(0, 0, availW, availH);

    int lines = 19;

    // We have to keep things square so choose the lesser one...

    int makeSize = Math.min(availH, availW);
    makeSize = Math.max(21, makeSize);        // but not too small...

    // Now, we have to draw a whole board in case the GUI also needs some part of
    // the board painted. (Something may be covered/revealed between the
    // call to repaint() from placing a stone, and when the GUI got around to
    // calling paint() for example)

    java.awt.image.BufferedImage boardImg = goImages.paintBoard(makeSize, lines, theGame.getCurrPos());

    g.drawImage(boardImg, ((availW - makeSize) / 2), ((availH - makeSize) / 2), this);
  }

  public void update(java.awt.Graphics g) {
    paint(g);
  }

  public void move(PointOfPlay pop) throws IllegalMoveException {
    if (pop != null) {
      theGame.doMove(pop.getX(), pop.getY());
    } else {
      // pass
      theGame.doMove(Move.PASS, Move.PASS);
    }
    repaint();
  }

  public void reset() {
    theGame.newGame("Leela", "Leela", 0, 7.5f);
    repaint();
  }

  @Override
  public Dimension getPreferredSize() {
    return PREFERRED_SIZE;
  }

  void saveGame() {
    String format = DateTimeFormatter.ISO_INSTANT
        .format(new Date().toInstant()).replaceAll(":", "_");
    File file = new File(format + ".sgf");
    System.out.println("Saving as:" + file);
    theGame.saveGame(file.getPath());
  }
}
