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
import leelawatcher.sgf.SGFbuilder;

import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Stores all the information about a game. Moves are stored in a tree.
 * Variations are possible by adding moves when <code>currMove</code> is
 * not pointing to the last element, or by setting <code>remUndo</code>
 * to true. If <code>remUndo</code> is true, then moves undone will not
 * be lost, and if <code>markUndos</code> is true the word UNDO will
 * be prepended to any comment.
 * <p>
 * <p><b>refactor:</b> create a player object that holds some of the player
 * specific info.</p>
 * <p><b>refactor:</b> Add an instance variable to point to a concrete instance
 * of AbstractRules</p>
 * <p>
 * <p>
 * <p><b>to be added</b>: ISO standard dates and numerically stored dates or
 * interpretation of ISO dates in program?</p>
 *
 * @author Patrick G. Heck
 * @version 0.1
 */


public class Game {

  // Basic info

  private String _bName, _wName;     // Identify the players
  private float _bRank, _wRank;      // Player ranks, -2.0 to -2.99 = 2kyu
  private int _handi;               // how many handicap stones?
  private float _komi;              // points to white in compensation
  private int _boardSizeX;          // board size - 19x19 for now, non-square
  // to be imped later
  private int _boardSizeY;
  private String _gameName;         // text summary/title to game
  private String _gameEvent;        // what event if any is this game part of
  private String _gameResult;       // What was the outcome (if any)
  private String _date;             // when?
  private String _place;            // where?
  private String _gameNotes;        // Additional info

  // also add others? for backwards
  // SGF compatability. Also need to research demarcation of variations.


  // Play details

  private String _ruleSet;          // what conventions is game played under

  // The game itself

  private Move _gameRoot;           // root of move tree, (see Move.java)

  // Status information

  private int handiLeft;           // how many handicap stones may be placed
  private boolean gameOver;        // 2 consecutive passes set this to true
  private boolean whiteLast;       // true if it is black's move

  private Move currMove;           // Points to current move
  private Move prevMove;           // Points to last move

  // Functionality options

  private boolean remUndo;          // if true remember undo as a variation
  private boolean markUndos;        // if true prepend UNDO to comment
  private boolean tradHandi;        // if true, auto place handicap stones at
  //    traditional locations
  private boolean toStringIsSGF;      // if true toString conforms to SGF format

  // to help keep track of objects, and debug problems, all objects number
  // their instances.

  private static long numInstances = 0;
  private long numThis;

  /**
   * Create an instance of a game from minimal basic info. All properties
   * other than the player's names, the handicap and the komi should be set
   * using setter methods.
   *
   * @param nameWhite The name of the player using white stones.
   * @param nameBlack The name of the player using black stones.
   * @param handicap The handicap for the game
   * @param ptsKomi Amount of komi to use
   */
  public Game(String nameWhite, String nameBlack, int handicap,
              float ptsKomi) {
    _bName = nameBlack;
    _wName = nameWhite;
    _bRank = -999.0f;
    _wRank = -999.0f;
    _handi = handicap;

    handiLeft = handicap;
    _komi = ptsKomi;

    _boardSizeX = 19;
    _boardSizeY = 19;

    _gameName = _wName + " vs. " + _bName;
    _gameEvent = "Leela Zero Self Training";
    _gameResult = "?";
    _place = "Location Unknown";
    _date = DateTimeFormatter.ISO_INSTANT.format(new Date().toInstant());
    _gameNotes = "";

    _ruleSet = "Japanese";            // default probably will be japaneese
    tradHandi = true;               // traditions are defaults
    _gameRoot = new Move();     // a white pass to root the game tree
    gameOver = false;               // we have only just begun!
    whiteLast = true;               // this makes it black's move
    currMove = _gameRoot;
    prevMove = _gameRoot;
    remUndo = true;                 // undos create variations
    markUndos = true;               // mark Undo variations
    toStringIsSGF = false;          // toString will be SGF after fully
    // implemented

    numInstances++;                // we just created one!
    numThis = numInstances;       // which one is this
  }

  /**
   * Place a stone on the board. The color of the stone is automatically
   * determined. placement of handicap stones via this method is supported
   * by counting down the <code>handiLeft</code> variable. Black is allowed
   * consecutive moves until this variable reaches 0.
   *
   * @param xcoor The x (horizontal) coordinate of the move.
   * @param ycoor The y (vertical) coordinate of the move.
   * @return The reference to the move object that has been added
   * to the game tree.
   */

  public Move doMove(int xcoor, int ycoor)    // 0,0 at Upper Left
  {
    char stoneColor = 'W';

    //Check that this move will fall on the board.

    boolean onBoard = false;
    if ((xcoor >= 0 && xcoor < _boardSizeX) && (ycoor >= 0 && ycoor < _boardSizeY)) {
      onBoard = true;
    }

    if (!onBoard && (xcoor != Move.PASS))
      return currMove;

    // figure out if there are handicap stones to be placed

    if (currMove.isRoot() && handiLeft > 0) {
      if (tradHandi) {
        // place handicap stones at star points.
      } else {
        whiteLast = false;
        handiLeft--;
        currMove.setupBlack(xcoor, ycoor);
        return currMove;
      }
    }

    prevMove = currMove;
    // todo: this needs to move to trusting whiteLast all the time...
    if (currMove.isSetup()) {
      stoneColor = whiteLast ? Move.MOVE_BLACK : Move.MOVE_WHITE;
    } else {
      if (prevMove.isWhite() || (prevMove.isRoot() && (_handi == 0))) {
        stoneColor = 'B';            // if white moved last, black stone
      } else {
        stoneColor = 'W';
      }
    }

    currMove = new Move(xcoor, ycoor, stoneColor, prevMove);

    whiteLast = !whiteLast;

    return currMove;

  }

  public void doSetup(char type, int xcoor, int ycoor, boolean blackToMove) {
    if (!currMove.isSetup()) {
      currMove = new Move(prevMove);
    }
    switch (type) {
      case Move.EMPTY      : currMove.setupEmpty(xcoor,ycoor); break;
      case Move.MOVE_BLACK : currMove.setupBlack(xcoor,ycoor); break;
      case Move.MOVE_WHITE : currMove.setupWhite(xcoor,ycoor); break;
    }
    currMove.setColorMoveNext(blackToMove ? Move.MOVE_BLACK : Move.MOVE_WHITE);
    whiteLast = blackToMove;
  }

  /**
   * Deletes the current move, and returns to the previous (parent) move.
   */

  public void delCurrMove() {
    Move dead = currMove;
    currMove = currMove.getParent();
    currMove.removeChild(dead);
  }

  /**
   * Marks the current move if Undo marking is enabled and ascends the
   * move tree by one.
   */

  public void undoMove() {
    if (markUndos)
      currMove.setComment("UNDO " + currMove.getComment());
    if (remUndo)
      currMove = currMove.getParent();
    else
      delCurrMove();
  }

  /**
   * Sets the name of the player using the white stones.
   *
   * @param aName The string containing the white player's name
   */

  public void setWName(String aName) {
    _wName = aName;
  }

  /**
   * Gets the name of the player using the white stones.
   *
   * @return The string containing the white player's name
   */

  public String getWName() {
    return _wName;
  }

  /**
   * Gets the rank of the player using the white stones in numeric form.
   *
   * @return The float vaue indicating the white player's rank.
   */

  public float getWRank() {
    return _wRank;
  }

  /**
   * Gets the rank of the player using the white stones in string form.
   * <p>
   * This program's rank number is similar to the AGA rating system
   * from 30 kyu to 7 dan. Above 7 dan they diverge so that this
   * program can easily handle Profesional ranks. Most traditional
   * ranking systems don't recognize non professional dan ranks above
   * 7 dan, so this is not unreasonable. The AGA's system is a rating
   * system, not a ranking system, which is why it diverges. If a
   * player enters an rank over 7 dan in the program, they will be
   * required to confirm their pro status, otherwise they will be
   * recorded as a 7 dan (9.3 -> 7d) rather than a pro (9.3 -> 2p).
   * All checking for valid ranks is done when the rank is set by the
   * <code>setWRank</code> method.
   * <p>
   * <center><table border="1" width="80%">
   * <p>
   * <tr><th> Rank value </th><th> This program </th><th> AGA Rating </th>
   * </tr>
   * <p>
   * <tr><td> -31    </td><td>  30 kyu (30k)   </td><td>  Same    </td></tr>
   * <tr><td> -30    </td><td>  30 kyu (30k)   </td><td>  same    </td></tr>
   * <tr><td> -29.1  </td><td>  29 kyu (29k)   </td><td>  same    </td></tr>
   * <tr><td> -29.0  </td><td>  29 kyu (29k)   </td><td>  same    </td></tr>
   * <tr><td> - 1    </td><td>  1 kyu  (1k)    </td><td>  same    </td></tr>
   * <tr><td> - 0.5  </td><td>  Inval. (1k)    </td><td>  same    </td></tr>
   * <tr><td>   0.5  </td><td>  Inval. (1k)    </td><td>  same    </td></tr>
   * <tr><td>   1.0  </td><td>  1 dan  (1d)    </td><td>  same    </td></tr>
   * <tr><td>   1.8  </td><td>  1 dan  (1d)    </td><td>  same    </td></tr>
   * <tr><td>   7.9  </td><td>  7 dan  (7d)    </td><td>  same    </td></tr>
   * <tr><td>   8.0  </td><td>  1 dan pro (1p) </td><td>  7 dan   </td></tr>
   * <tr><td>   8.1  </td><td>  1 dan pro (1p) </td><td>  7 dan   </td></tr>
   * <tr><td>   9.0  </td><td>  2 dan pro (1p) </td><td>  7 dan   </td></tr>
   * <tr><td>   16.0 </td><td>  9 dan pro (1p) </td><td>  7 dan   </td></tr>
   * <tr><td>   16.9 </td><td>  9 dan pro (1p) </td><td>  7 dan   </td></tr>
   * <tr><td>   20.0 </td><td>  9 dan pro (1p) </td><td>  7 dan   </td></tr>
   * <p>
   * <table></center>
   *
   * @return The string value indicating the white player's rank. The
   * format of the return value is one or two digits followed
   * by either 'k', 'd' or 'p'. Specified as a Perl 5 rexexp
   * the output always matches<br> <br>
   * <p>
   * (\d{1,2}[kdp])|\?
   * <p>
   * <br><br>The letter portion indicates the ranking category,
   * with 'k' indicating a kyu level rank 'd' indicating a dan
   * level rank and 'p' indicating a professional dan
   * level rank.
   */

  public String getTradWRank()      // relies on setWRank to prevent illegal
  {                                 // rank values! If you get a ERR rank
    // setWRank has screwed up.
    int intRank = (int) _wRank;
    String tradRank = "ERR";

    if (intRank < 30) {
      return "?";
    }
    if (intRank <= -1) {
      intRank *= -1;
      tradRank = (intRank > 30 ? "NR" : intRank + "k");
      return tradRank;
    }
    if (intRank >= 1 && intRank < 8)
      tradRank = intRank + "d";
    if (intRank >= 8)
      tradRank = intRank - 7 + "p";
    if (intRank > 16) {
      tradRank = "9p";
    }
    return tradRank;
  }

  /**
   * Sets the rank of the player using the white stones in numeric form.
   * This method is responsible for preventing the entry of nonsense values
   * for ranks. See the {@link #getTradWRank() getTradWRank} for detail on
   * interpretation of ranks.
   *
   * @param aRank The float value indicating the white player's rank.
   */

  public void setWRank(float aRank) {
    if (aRank < -30.0f) {
      // message to user once GUI imped

      aRank = -30.0f;      // 30kyu is lowest possible rank in most
    }                        // ranking systems.
    if (aRank > -1.0f && aRank < 1.0f) {
      aRank = -1.0f;       // otherwise 2 stones between 1Dan and
    }                        // 1 kyu... kyu until you are a dan.
    if (aRank >= 8.0f) {
      // Add Code? or make part of a player object?:
      // Confirm pro status before asigning a pro rank
      // 7 Dan is generally the highest ametur rank
      // the American Go Association has a rating system that goes
      // beyond +7 so a wIsPro flag may be added later.
      // Can't do this until GUI is built
    }
    if (aRank >= 17.0f) {
      aRank = 16.99f;       // 9 Dan pro (9p) is highest rank
    }                         // possible
    _wRank = aRank;
  }

  /**
   * Sets the name of the player using the black stones.
   *
   * @param aName The string containing the black player's name
   */

  public void setBName(String aName) {
    _bName = aName;
  }

  /**
   * Gets the name of the player using the black stones.
   *
   * @return The string containing the black player's name
   */

  public String getBName() {
    return _bName;
  }

  /**
   * Gets the rank of the player using the white stones in numeric form.
   *
   * @return The float vaue indicating the white player's rank.
   */

  public float getBRank() {
    return _bRank;
  }

  /**
   * Gets the rank of the player using the black stones in string form.
   * See the {@link #getTradWRank() getTradWRank} for detail on
   * interpretation of ranks.
   *
   * @return The string value indicating the white player's rank. The
   * format of the return value is one or two digits followed
   * by either 'k', 'd' or 'p'. Specified as a Perl 5 rexexp
   * the output always matches<br>
   * <p>
   * (\d{1,2}[kdp])|\?
   * <p>
   * <br>The letter portion indicates the ranking category,
   * with 'k' indicating a kyu level rank 'd' indicating a dan
   * level rank and 'p' indicating a professional dan
   * level rank. ? indicates an unknown rank.
   */

  public String getTradBRank()      // relies on setWRank to prevent illegal
  {                                 // rank values! If you get a ? rank
    // setBRank has screwed up, or never been
    // called
    int intRank = (int) _bRank;
    String TradRank = "?";

    if (intRank < 30) {
      return "?";
    }
    if (intRank <= -1) {
      intRank *= -1;
      TradRank = intRank + "k";
      return TradRank;
    }
    if (intRank >= 1 && intRank < 8)
      TradRank = intRank + "d";
    if (intRank >= 8)
      TradRank = intRank - 7 + "p";

    return TradRank;
  }

  /**
   * Sets the rank of the player using the black stones in numeric form.
   * This method is responsible for preventing the entry of nonsense values
   * for ranks. See the {@link #getTradWRank() getTradWRank} for detail on
   * interpretation of ranks.
   *
   * @param aRank The float vaue indicating the black player's rank.
   */

  public void setBRank(float aRank) {
    if (aRank < -30.0f) {
      // message to user once GUI imped

      aRank = -30.0f;      // 30kyu is lowest possible rank in most
    }                        // ranking systems.
    if (aRank == 0) {
      aRank = 0.01f;       // otherwise 0 is less than 1Dan and more
    }                        // than 1 kyu... and its only .01!
    if (aRank > 7.0f) {
      // Confirm pro status before asigning a pro rank
      // 7 Dan is generally the highest ametur rank
      // the American Go Association has a rating system that goes
      // beyond +7 so a bIsPro flag may be needed later.
      // Can't do this until GUI is built
    }
    if (aRank > 16.0f) {
      aRank = 16.0f;        // 9 Dan pro (9p) is highest rank
    }                         // possible
    _bRank = aRank;
  }

  /**
   * Adds some general information about the game.
   *
   * @param notes A String of any length containing general information
   *              about the game.
   */
  public void setGameNotes(String notes) {
    _gameNotes = notes;
  }

  /**
   * Retreives some general information about the game.
   *
   * @return notes    A String of any length containing general information
   * about the game.
   */

  public String getGameNotes() {
    return _gameNotes;
  }

  /**
   * Set the date on which the game was played. This is currently stored
   * as any generic string, but in the future this function will enforce
   * ISO format dates.
   *
   * @param someDate A String describing the date the game was played.
   */

  public void setDate(String someDate) {
    _date = someDate;                // should be ISO format! Will add
  }                                   // checks for this later.

  /**
   * Get the date on which the game was played. This is currently stored
   * as any generic string, but in the future this will be an ISO
   * formated date.
   *
   * @return A String describing the date the game was played.
   */

  public String getDate() {
    return _date;
  }

  /**
   * The local in which the game was played. This is a generic string,
   * but only intended as a breif description such as a city, and country
   * name. The gameEvent parameter should be used to describe the name of
   * the tournament or the occasion for which this game was played.
   *
   * @return A string describing the local where the game occured.
   */

  public String getPlace() {
    return _place;
  }

  /**
   * The local in which the game was played. This is a generic string,
   * but only intended as a breif description such as a city, and country
   * name. The gameEvent parameter should contain the name of
   * the tournament or the occasion for which this game was played.
   *
   * @param aPlace A string describing the local where the game occured.
   */

  public void setPlace(String aPlace) {
    _place = aPlace;
  }

  /**
   * Set the size of the board on which the game was played. Currently only
   * square boards are supported by this class.
   *
   * @param square An integer specifying the size of the board.
   */

  public void setBoardSize(int square) {
    _boardSizeX = square;
    _boardSizeY = square;
  }

  /**
   * A string representation of the size of the board.
   *
   * @return A string consisting either entirely of consecutive digits or
   * consisting of two consecutive runs of digits separated by a
   * colon. The first case indicates a square board, the second
   * case indicates a rectangular board where the horizontal
   * dimension is specified before the colon and the vertical
   * dimension is specified after the colon.
   */

  public String getBoardSize() {
    if (_boardSizeX == _boardSizeY)
      return _boardSizeX + "";
    else
      return _boardSizeX + ":" + _boardSizeY;
  }

  /**
   * The number of Handicap stones black received in this game.
   *
   * @return An integer indicating the handicap for this game.
   */

  public int getHandi() {
    return _handi;
  }

  /**
   * Set the number of handicap stones black will receive this game.
   * Currently the maximum handicap allowed is 9 stones, though this
   * limitation will be removed later.
   *
   * @param handicap The number of handicap stones for black.
   */

  public void setHandi(int handicap) {
    if (tradHandi && handicap > 9) {
      handicap = 9;          // 9 is the max traditional handicap
      // After we have a GUI a question box
      // to user here.
    }
    if (handicap <= 1)
      handicap = 0;
    _handi = handicap;
    handiLeft = handicap;
  }

  /**
   * The number of handicap stones yet to be placed on the board.
   * This method is used in both the automated placement of handicap
   * stones and free placement.
   *
   * @return The number of hanicap stones not yet placed.
   */

  public int getHandiLeft() {
    return handiLeft;
  }

  /**
   * The komi points awarded as compensation to white. This value may be
   * either positive or negative. Sometimes large komi values are used
   * instead of handicap stones so there are no bounds on this value.
   *
   * @return The komi points awarded to white.
   */

  public float getKomi() {
    return _komi;
  }

  /**
   * Set the komi points awarded to white for this game. This value may be
   * either positive or negative. Sometimes large komi values are used
   * instead of handicap stones so there are no bounds on this value.
   *
   * @param ptsKomi The komi points awarded to white.
   */

  public void setKomi(float ptsKomi) {
    _komi = ptsKomi;
  }

  /**
   * The name given to the game. This is typically something like "Round 4"
   * or "Game 3 of 5" for match or tournament play. The name of the event
   * should be available via {@link #getGameEvent() getGameEvent()}.
   *
   * @return A name identifying the game.
   */

  public String getGameName() {
    return _gameName;
  }

  /**
   * Set the name identifying the game. This is typically something like
   * "Round 4"or "Game 3 of 5" for match or tournament play. The name of the
   * event should be stored via
   * <p>
   * {@link #setGameEvent(String) setGameEvent(String)}.
   *
   * @param aName A name identifying the game.
   */

  public void setGameName(String aName) {
    _gameName = aName;
  }

  /**
   * The name of the event for which the game was played. Usually blank
   * for informal games.
   *
   * @return The name of the event.
   */

  public String getGameEvent() {
    return _gameEvent;
  }

  /**
   * Set the name of the event for which the game was played. Usually blank
   * for informal games.
   *
   * @param anEvent The name of the event.
   */

  public void setGameEvent(String anEvent) {
    _gameEvent = anEvent;
  }

  /**
   * Get a string identifying the rules used for this game.
   * <p>
   * <p><b>N.B.</b>     This method will likely be renamed to getRuleSetName
   * when an AbstractRules object is associated with each
   * game. The present name will then be used to get a
   * a reference to an instanace of a subclass of
   * AbstractRules used by this game. Rule sets expected
   * to be supported include Japaneese (Nihon Kin),
   * Chineese, AGA, and ING and possibly more. This method
   * will be deprecated in the VERY near future.
   *
   * @return The name of the rule set used.
   */

  public String getRuleSet() {
    return _ruleSet;
  }

  /**
   * Set a string identifying the rules used for this game.
   * <p>
   * <p><b>N.B.</b>     This method will likely be removed when an
   * AbstractRules object is associated with each
   * game. The present name will then be used to add a
   * a reference to an instance of a subclass of
   * AbstractRules to be used by this game. Rule sets
   * expected to be supported include Japaneese (Nihon
   * Kin), Chineese, AGA, and ING and possibly more. This
   * method will be deprecated in the VERY near future.
   *
   * @param rules The name of the rule set used.
   */

  public void setRuleSet(String rules) {
    _ruleSet = rules;
  }

  /**
   * A string indicating the result of the game. The format of this string
   * is <W | B>+<R | ###.#>. Typical examples are W+R for white wins by
   * resignation and B+34.5 for black wins by 34.5 points. This property
   * should be empty if the game is not complete.
   *
   * @return A string indicating the result of the game
   */

  public String getGameResult() {
    return _gameResult;
  }

  /**
   * Set a string indicating the result of the game. The format of this
   * string is <W | B>+<R | ###.#>. Typical examples are W+R for white wins
   * by resignation and B+34.5 for black wins by 34.5 points. This property
   * should not be set until the game is complete.
   *
   * @param result A string indicating the result of the game
   */


  public void setGameResult(String result) {
    _gameResult = result;
  }

  /**
   * Set a flag indicating that the game is over.
   *
   * @param over True if the game has ended. False otherwise.
   */

  public void setGameOver(boolean over) {
    gameOver = over;
  }

  /**
   * Is it white's turn to move.
   *
   * @return True if it is white's turn, false otherwise.
   */

  public boolean isWMove() {
    if (whiteLast)
      return false;
    else
      return true;
  }

  /**
   * Is the game over.
   *
   * @return True if the game has been completed false otherwise.
   */

  public boolean isGameOver() {
    return gameOver;
  }

  /**
   * A string representation of a game. This output closely follows the
   * specification for SGF file format version 4, but is subject to change.
   * a separate method for generating SGF output may be assigned in the
   * future.
   *
   * @return The contents of the game in a format resembling
   * SGF version 4.
   */

  public String toString() {
    SGFbuilder temp = new SGFbuilder();
    return temp.buildSGF(this);
  }

  /**
   * Get a reference to the root of the variation tree for this game.
   *
   * @return A reference to the topmost <code>Move</code> object
   * in the variation tree for this game.
   */

  public Move movesRoot() {
    return _gameRoot;
  }

  /**
   * get a reference to the last move made.
   *
   * @return a reference to the move that the game is currently on.
   */
  public Move getCurrMove() {
    return currMove;
  }

  /**
   * Outputs the contents of the move tree for debugging.
   * <p>
   * This is for debugging purposes only, and all
   * output is sent directly to standard output via
   * <code>System.out.println</code>
   *
   * @param aMove Th root of the move tree.
   */

  public static void dPrintMoves(Move aMove) {
    System.out.println(aMove);
    aMove.dPrint();

    int numChild = aMove.numChildren();
    if (aMove.next() != aMove) {
      for (int i = 0; i < numChild; i++) {
        dPrintMoves(aMove.next(i));
      }
    }
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
    System.out.println("Game " + numThis + " of " + numInstances + " Games");
    System.out.println("_bName=" + _bName);
    System.out.println("_wName=" + _wName);
    System.out.println("_bRank=" + _bRank);
    System.out.println("_wRank=" + _wRank);
    System.out.println("_handi=" + _handi);
    System.out.println("_komi=" + _komi);
    System.out.println("_boardSizeX=" + _boardSizeX);
    System.out.println("_boardSizeY=" + _boardSizeY);
    System.out.println("_gameName=" + _gameName);
    System.out.println("_gameEvent=" + _gameEvent);
    System.out.println("_gameResult=" + _gameResult);
    System.out.println("_date =" + _date);
    System.out.println("_place=" + _place);
    System.out.println("_gameNotes =" + _gameNotes);
    System.out.println("_ruleSet=" + _ruleSet);
    System.out.println("tradHandi=" + tradHandi);
    System.out.println("_gameRoot=" + _gameRoot);
    System.out.println("handiLeft=" + handiLeft);
    System.out.println("gameOver=" + gameOver);
    System.out.println("whiteLast=" + whiteLast);
    System.out.println("currMove=" + currMove);
    System.out.println("prevMove=" + prevMove);
    System.out.println("remUndo=" + remUndo);
    System.out.println("markUndos=" + markUndos);
    System.out.println("toStringIsSGF=" + toStringIsSGF);
    System.out.println("*** dPrint of move list ***");
    dPrintMoves(_gameRoot);
  }

}
