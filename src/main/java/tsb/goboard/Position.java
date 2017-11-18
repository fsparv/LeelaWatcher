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

package tsb.goboard;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class will record a single position on a go board.
 * <p>
 * <p>A record of all positions must be kept in order to implement the AGA's
 * Super-Ko rule, which disallows exact repetition of ANY board position.
 * There is no way to uniquely represent a position on a go board in a
 * 32 bit hashCode(). In fact, that is too few bits by a factor of almost
 * 20.
 * <p>
 * <p>We will use an array of int to represent a board position for
 * each color. This representation was chosen as a compromise between
 * implementation ease and memory usage as follows:
 * <p>
 * <p>This gives us 2 arrays of 76 bytes usage per position,
 * and in a typical game there are less 300 moves. Therefore a typical
 * vector containing all positions in the game will occupy a very
 * reasonable 45k or so. Since there are 1.74e178 possible board positions
 * the smallest possible representation would be 9 longs and an int (593
 * bits are needed). Representing the positions as a character array would
 * bring the cost of a 300 move game to over 216 Kilobytes, since each point
 * on the board would then cost 16 bits. Though most computers today have much
 * more ram than this, this code was written in a time when that still was
 * a lot of memory.
 * <p>
 * <p>The dual array of 19 ints assumes empty if a bit is not on in either
 * array, (using only the 19 least significant bits). The condition of both
 * bits being on is an error. A bit on in one array indicates a black stone
 * at that position, and a bit on in the other array indicates a white stone
 * at that position. The combination of the 2 arrays to represent a board is
 * not hard to fathom (for me at least) and turning bits on and off is fairly
 * easy. This also makes error checks and queries based on boolean operations.
 * (requiring a bit of logic thinking but not too hard)
 * <p>
 * <p>The 19 int or 9 long/1 int method essentially would rely on a base 3
 * representation of the board 0 for empty, 1 for white and 2 for black.
 * Black, white or empty must be known for all 361 positions, to uniquely
 * represent the board, therefore a 593 bit (= 3.24e178) representation
 * is the minimum possible without finding a way to skip positions that are
 * illegal. I doubt the CPU costs of such a task would be reasonable! Also
 * keep in mind that pass is a legal move, so if both players were terminally
 * stupid, or trying to break the program, the board could fill with stones
 * of a single color (for example).
 * <p>
 * <p>Converting to a 361 digit base 3 number makes my brain hurt. It hardly
 * seems worth the effort for a measly 20k of ram or so. Even palmtops have
 * at least a 2 Megabytes (and frequently 8) these days!.
 * <p>
 * <p>Positions other than an empty board should be built from other positions.
 * this class does not attempt to do any rules checking, so checking for self
 * capture, ko and a stone already there. This lack of checking enables the
 * use of this class for positions on 19x19 13x13 or 9x9 since those will
 * effectively just be a subsection of the 19x19 board. Support for over sized
 * boards up to 57x57 could be added by converting this to an array of long
 * and increasing the size of the vectors.
 * <p>
 * <p>
 * <p>FIXME: This class probably should be immutable but is not.
 * <p>TODO: Recode this with array of char (or byte) and compare speed. Another
 * possibility is using java1.4 ByteBuffers which would be more flexable for
 * oversized boards, but harder to code.
 * <p>TODO: This class currently is one of the key limmitations preventing
 * alternate board sizes. The magic number in colmasks, posBlack and posWhite
 * need to be abstracted.
 *
 * @author Patrick G. Heck
 * @version $Revision$
 */


@SuppressWarnings({"WeakerAccess", "unused"})
public class Position implements Cloneable {
  public final static int[] colMasks = new int[19];        // bit fields

  private static int numInstances = 0;
  private int numThis;

  private int moveNum; // to associate this Position with a move in the game.

  private boolean blackToMove;
  private int[] posBlack = new int[19];  // the Position of black stones
  private int[] posWhite = new int[19];  // the Position of white stones

  static {                                                   // fill up colMasks
    for (int i = 0; i < colMasks.length; i++) {          // with bit fields
      colMasks[i] = (int) Math.pow(2, i);
    }
  }

  /**
   * Create an empty (blank) <code>Position</code>.
   * <p>
   * <p> This constructor is mainly used to create an initial
   * <code>Position</code> object when no moves have been made.
   * In most cases the 2 argument constructor is most useful.
   */

  public Position() {
    numThis = numInstances++;
  }

  /**
   * Create a <code>Position</code> from another <code>Position</code>.
   * <p>
   * <p> This constructor should create an independent copy of
   * a position for independent use... Generally the 2 argument constructor
   * is what you want to use.
   *
   * @param basePos The <code>Position</code> from which to create the
   *                new <code>Position</code>.
   */
  protected Position(Position basePos) {
    numThis = numInstances++;
    moveNum = basePos.moveNum;
    posBlack = basePos.getArrayBlack();
    posWhite = basePos.getArrayWhite();
    blackToMove = basePos.blackToMove;
  }

  /**
   * Create a <code>Position</code> by making a move on a previous
   * <code>Position</code>.
   * <p>
   * <p> Takes a previous Position and creates a new Position by adding
   * a stone as indicated by the move argument. In most cases this is the
   * constructor to use. Other constructors may be deprecated once this
   * class is written to be immutable.
   *
   * @param prev  The <code>Position</code> from which to create the
   *              new <code>Position</code>.
   * @param aMove The <code>Move</code> specifying the stone to add.
   */

  public Position(Position prev, Move aMove) {
    numThis = numInstances++;

    posBlack = prev.getArrayBlack(); // start with the last Position
    posWhite = prev.getArrayWhite();

    prev.checkValid();      // make sure previous errors don't propagate

    moveNum = aMove.getMoveNum();

    if (aMove.isMove())      // now add the new stone
    {
      if (aMove.getColor() == 'W') {
        blackToMove = true;
        if (!aMove.isPass()) {
          posWhite[aMove.getY()] += colMasks[aMove.getX()];
        }
      } else if (aMove.getColor() == 'B') {
        blackToMove = false;
        if (!aMove.isPass()) {
          posBlack[aMove.getY()] += colMasks[aMove.getX()];
        }
      }
    } else if (aMove.isSetup()) {
      List<PointOfPlay>[] tmp = aMove.getSetupInfo();

      tmp[0].forEach(p -> {
        if (this.blackAt(p.getX(), p.getY()))
          posBlack[p.getY()] ^= colMasks[p.getX()];
        if (this.whiteAt(p.getX(), p.getY()))
          posWhite[p.getY()] ^= colMasks[p.getX()];
      });
      blackToMove = (aMove.getColorNextMove() == Move.MOVE_BLACK);

      tmp[1].forEach(p -> posBlack[p.getY()] |= colMasks[p.getX()]);

      tmp[2].forEach(p -> posWhite[p.getY()] |= colMasks[p.getX()]);

    }
  }

  public String toString() {
    StringBuilder tmp = new StringBuilder();

    this.checkValid();
    for (int y = colMasks.length - 1; y >= 0; y--) {
      tmp.append(y).append(":");
      for (int x = 0; x < colMasks.length; x++) {
        tmp.append(colorAt(x, y));
      }
      tmp.append("\n");
    }
    return tmp.toString();
  }

  public boolean equals(Object aPosition) {
    int tmp;
    int[] aPosBlack;
    int[] aPosWhite;

    if (!(aPosition instanceof Position))
      return false;
    Position other = (Position) aPosition;

    if (other.blackToMove != blackToMove) {
      return false;
    }
    //System.out.println("matches player to move");

    aPosBlack = other.getArrayBlack();
    aPosWhite = other.getArrayWhite();

    //	dPrint();

    //	tPos.dPrint();

    // stays 0 if equal
    tmp = IntStream.range(0, posBlack.length).map(i -> (posBlack[i] ^ aPosBlack[i])).sum();
    // stays 0 if equal
    tmp += IntStream.range(0, posWhite.length).map(i -> (posWhite[i] ^ aPosWhite[i])).sum();

    // since we only use 19 of 32 bits
    // in each int there is no danger of
    // the sum rolling the tmp over to 0.
    return tmp == 0;
  }

  @Override
  public int hashCode() {
    int result = (blackToMove ? 1 : 0);
    result = 31 * result + Arrays.hashCode(posBlack);
    result = 31 * result + Arrays.hashCode(posWhite);
    return result;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }


  public void checkValid() {
    int tmp = 0;
    for (int i = 0; i < colMasks.length; i++) {
      tmp += posBlack[i] & posWhite[i];
    }
    if (tmp > 0)
      throw new RuntimeException("Malformed Go Position:" + tmp);
  }

  public char colorAt(PointOfPlay p) {
    return colorAt(p.getX(), p.getY());
  }

  public char colorAt(int x, int y) {
    this.checkValid();        // Make sure no errors since a stone marked
    if (this.blackAt(x, y))    // in both black and white would come up
      return 'B';           // black by this algorithm
    if (this.whiteAt(x, y))
      return 'W';
    return 'E';
  }

  public boolean stoneAt(PointOfPlay p) {
    return stoneAt(p.getX(), p.getY());
  }

  public boolean stoneAt(int x, int y) {
    this.checkValid();

    return (this.blackAt(x, y) || this.whiteAt(x, y));
  }

  /**
   * Checks if a black stone resides at x,y.
   * <p>
   * If the bit that is on in colMasks[x] is on in posBlack[y] then there
   * is a stone at x,y.
   */
  public boolean blackAt(int x, int y) {
    return ((posBlack[y] & colMasks[x]) > 0);
  }

  public boolean blackAt(PointOfPlay p) {
    return blackAt(p.getX(), p.getY());
  }


  /**
   * Checks if a white stone resides at x,y.
   * <p>
   * If the bit that is on in colMasks[x] is on in posWhite[y] then there
   * is a stone at x,y.
   */
  public boolean whiteAt(int x, int y) {
    return ((posWhite[y] & colMasks[x]) > 0);
  }

  public boolean whiteAt(PointOfPlay p) {
    return whiteAt(p.getX(), p.getY());
  }

  public void removeStoneAt(int x, int y) {
    posWhite[y] &= ~colMasks[x];
    posBlack[y] &= ~colMasks[x];
  }

  public void removeStoneAt(PointOfPlay p) {
    removeStoneAt(p.getX(), p.getY());
  }

  public int[] getArrayBlack() {
    int[] temp = new int[19];
    System.arraycopy(this.posBlack, 0, temp, 0, temp.length);
    return temp;
  }

  public int[] getArrayWhite() {
    int[] temp = new int[19];
    System.arraycopy(this.posWhite, 0, temp, 0, temp.length);
    return temp;
  }

  public int getMoveNum() {
    return moveNum;
  }

  public void dPrint() {
    String tmp = this.toString();
    tmp += "numInstances=" + numInstances + "\n";
    tmp += "numThis=" + numThis + "\n";
    tmp += "blackToMove=" + blackToMove;
    System.out.println(tmp);
  }
}








