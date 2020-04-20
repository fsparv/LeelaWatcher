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

package leelawatcher.gui;

import leelawatcher.TsbConstants;
import leelawatcher.goboard.PointOfPlay;
import leelawatcher.goboard.Position;
import leelawatcher.goboard.move.AbstractMoveNode;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.net.URL;


/**
 * @author root
 */
public class ImageMaker implements TsbConstants {

  // drawing fine tuning
  float lastPlayedDotScaledDownFactor = 6;
  float shadowOffset = 0.12f;
  Color shadowColor = new Color(0, 0, 0, 90);

  // Constants

  public static final int PAINT_ALL = 0;
  public static final int PAINT_CLIPPED = 1;
  public static final int PAINT_STONE = 2;

  // globalInfo contains file names for images

  private BufferedImage BoardBackground;
  private Canvas someComp = new Canvas();  // need an image observer...

  /**
   * Creates new ImageMaker
   */
  public ImageMaker() {
    URL whereImage = Thread.currentThread().getContextClassLoader().getResource(BRD_BACKGRD);

    BoardBackground = loadBackground(whereImage);
  }


  public BufferedImage loadBackground(URL imageLoc) {

    // create a place to hold the background pixmap for the board
    Image i;
    BufferedImage B;
    int H, W;
    Graphics2D G;


    i = Toolkit.getDefaultToolkit().getImage(imageLoc);

    MediaTracker wholeImg = new MediaTracker(someComp);
    wholeImg.addImage(i, 0);
    try {
      wholeImg.waitForAll();
    } catch (InterruptedException e) {
      System.out.println("Image load interrupted " +
          "for goban background!\n" + e);
    }

    H = i.getHeight(someComp);      // now that we have it how big is it?
    W = i.getWidth(someComp);

    // Now we have to move it into a BufferedImage...

    B = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);


    G = B.createGraphics();
    G.drawImage(i, 0, 0, someComp);

    return B;
  }

  // paint a stone at a given coordinate specified in pixels

  public void paintStone(int x, int y, Color player,
                         int pixSize, Graphics g) {
    //System.out.println("by pixel"+x+","+y);

    // draw shadow first
    g.setColor(shadowColor);
    g.fillOval(x+(int)(pixSize*shadowOffset), y+(int)(pixSize*shadowOffset), pixSize, pixSize);

    // draw stone itself
    g.setColor(Color.black);
    g.fillOval(x, y, pixSize, pixSize);
    g.setColor(player);
    g.fillOval(x + 1, y + 1, pixSize - 2, pixSize - 2);
  }

  // paint a stone at a given coordinate specified by board position
  // used to pre-paint a stone during a re-paint following a move
  //
  // The above technique didn't work. Oh well...

  public void paintStone(int x, int y, Color player,
                         int pixAvail, int numLines, Graphics G) {
    int makeSize = pixAvail;
    makeSize = Math.max(21, makeSize); // but not too small...

    float lineSp = makeSize / ((float) numLines + 1);

    int stnSize = Math.round(lineSp - 1);

    y = numLines - y - 1; // convert to screen orientation (0,0) in upper L

    paintStone(Math.round((lineSp / 2 + x * lineSp)),
        Math.round((lineSp / 2 + y * lineSp)),
        player, stnSize, G);
  }

  public BufferedImage paintBoard(int pixAvail, int size, Position pos) {
    // PixAvail is the number of pixels (square) we have to draw the board.
    // Size is the number of lines we need to draw.

    int makeSize = pixAvail;
    makeSize = Math.max(21, makeSize); // but not too small...

    //makeSize -= 10;                  // we want to leave a 5 pixel edge


    // Now, we have to draw a board, so get something to draw on...

    BufferedImage BoardImg = new BufferedImage(makeSize, makeSize,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D BGraphs = BoardImg.createGraphics();
    BGraphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // and some paint to work with...

    int H, W;
    H = BoardBackground.getHeight();
    W = BoardBackground.getWidth();

    BGraphs.setPaint(new TexturePaint(BoardBackground,
        new Rectangle(0, 0, W, H)));

    // Draw the Background...

    BGraphs.fillRect(0, 0, makeSize, makeSize);

    // Draw the lines...

    float x1, y1, x2, y2, h, w;
    x1 = y1 = x2 = y2 = h = w = 0;
    Line2D.Float aLine = new Line2D.Float();

    float lineSp = makeSize / (float) (size + 1);
    for (int lines = 1; lines < size + 1; lines++) {
      x1 = lineSp * lines;
      x2 = lineSp * lines;
      y1 = lineSp;
      y2 = makeSize - lineSp;
      aLine.setLine(x1, y1, x2, y2);
      BGraphs.setColor(Color.black);
      BGraphs.draw(aLine);
    }
    for (int lines = 1; lines < size + 1; lines++) {
      y1 = lineSp * lines;
      y2 = lineSp * lines;
      x1 = lineSp;
      x2 = makeSize - lineSp;
      aLine.setLine(x1, y1, x2, y2);
      BGraphs.setColor(Color.black);
      BGraphs.draw(aLine);
    }

    float hoshiSize = lineSp / 5.0f; // make hoshi 1/5 of a square wide
    hoshiSize = Math.max(hoshiSize, 2.0f); // but >= 6 pix

    Ellipse2D.Float hoshi = new Ellipse2D.Float();
    h = w = hoshiSize;

    int FromSide, BtwnHoshi, NumHoshi;

    if (size <= 9) {
      FromSide = 3;
      NumHoshi = 2; // results in 4 hoshi total
    } else {
      FromSide = 4;
      NumHoshi = 3; // results in 9 hoshi total for odd sizes
    }
    BtwnHoshi = ((size - 2 * FromSide) + 1) / 2;

    if ((size % 2 == 0) && size > 6) // only 4 when even # lines
    {
      x1 = (FromSide * lineSp) - hoshiSize / 2;
      y1 = (FromSide * lineSp) - hoshiSize / 2;
      hoshi.setFrame(x1, y1, w, h);
      BGraphs.setColor(Color.black);
      BGraphs.draw(hoshi);
      BGraphs.fill(hoshi);

      x1 = (FromSide * lineSp) - hoshiSize / 2;
      y1 = ((size - FromSide) * lineSp) - hoshiSize / 2;
      hoshi.setFrame(x1, y1, w, h);
      BGraphs.setColor(Color.black);
      BGraphs.draw(hoshi);
      BGraphs.fill(hoshi);

      x1 = ((size - FromSide) * lineSp) - hoshiSize / 2;
      y1 = (FromSide * lineSp) - hoshiSize / 2;
      hoshi.setFrame(x1, y1, w, h);
      BGraphs.setColor(Color.black);
      BGraphs.draw(hoshi);
      BGraphs.fill(hoshi);

      x1 = ((size - FromSide) * lineSp) - hoshiSize / 2;
      y1 = ((size - FromSide) * lineSp) - hoshiSize / 2;
      hoshi.setFrame(x1, y1, w, h);
      BGraphs.setColor(Color.black);
      BGraphs.draw(hoshi);
      BGraphs.fill(hoshi);
    } else if (size > 6) {
      // 4 or 9 hoshi depending on size:

      // KLUDGE: the (2-NumHoshi%2) term allows 4 and 9 to be handled
      // in the same loops. Without it only the 0,0 hoshi would
      // be correctly placed for 4 hoshi (e.g. 9x9 would have hoshi
      // at 3,3 3,5 5,3 5,5 instead of 3,3 3,7 7,3 7,7)

      for (int hoshiX = 0; hoshiX < NumHoshi; hoshiX++)
        for (int hoshiY = 0; hoshiY < NumHoshi; hoshiY++) {
          x1 = (hoshiX * lineSp * BtwnHoshi * (2 - NumHoshi % 2)
              + FromSide * lineSp) - hoshiSize / 2;

          y1 = (hoshiY * lineSp * BtwnHoshi * (2 - NumHoshi % 2)
              + FromSide * lineSp) - hoshiSize / 2;

          hoshi.setFrame(x1, y1, w, h);
          BGraphs.setColor(Color.black);
          BGraphs.draw(hoshi);
          BGraphs.fill(hoshi);
        }
    }

    int stnSize = Math.round(lineSp - 1);

    // render it top down  so shadows work correctly
    for (int x = 0; x < size; x++)
      for (int y = size-1; y >= 0; --y)
        if (pos.stoneAt(x, y))
          paintStone(Math.round((lineSp / 2 + x * lineSp)),
                     Math.round((lineSp / 2 + ((size - 1) - y) * lineSp)),
                     (pos.blackAt(x, y)) ? Color.black : Color.white,
                     stnSize, BGraphs);

    // mark last move
    PointOfPlay lastMove = pos.getLastMove();

    float offsetFacor = ((lastPlayedDotScaledDownFactor/2)-1)/lastPlayedDotScaledDownFactor;
    if (lastMove != null && !AbstractMoveNode.isPass(lastMove.getX(),lastMove.getY())) {
        BGraphs.setColor(pos.blackAt(lastMove.getX(), lastMove.getY()) ? Color.white : Color.black);
        BGraphs.fillOval(Math.round((lineSp / 2 + lastMove.getX() * lineSp)+stnSize*offsetFacor),
                         Math.round((lineSp / 2 + ((size - 1) - lastMove.getY()) * lineSp)+stnSize*offsetFacor),
                         stnSize/(int)(lastPlayedDotScaledDownFactor/2),
                         stnSize/(int)(lastPlayedDotScaledDownFactor/2));
    }

    return BoardImg;
  }
}
