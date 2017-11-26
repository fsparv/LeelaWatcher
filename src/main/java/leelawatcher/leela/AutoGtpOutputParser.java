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
package leelawatcher.leela;

import leelawatcher.goboard.PointOfPlay;
import leelawatcher.gui.BoardView;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoGtpOutputParser {

  /*
   * This pattern is meant to report a match for one of 3 groups:
   * 1. anything ending in 'set.'
   */
  public static final Pattern EVENT = Pattern.compile("^(.*set\\.|\\s*\\d+\\s\\(\\w+\\)\\s*|Game).*", Pattern.DOTALL);
  private static final Pattern MOVE_EVENT = Pattern.compile("\\s*\\d+\\s*\\((\\w+)\\)\\s*");
  private static final Pattern MOVE = Pattern.compile("(?:(.)(\\d+))|(pass)");
  BoardView boardView;
  private boolean inProgress = false;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    String old = this.message;
    this.message = message;
    support.firePropertyChange("message", old, message);
  }

  String message;

  PropertyChangeSupport support = new PropertyChangeSupport(this);


  /**
   * Dead simple parser for the standard output from leela autogtp
   *
   * @param boardView the view on which to reflect the output.
   */
  public AutoGtpOutputParser(BoardView boardView) {
    this.boardView = boardView;
  }

  public void start(InputStream is) {
    Executors.newSingleThreadExecutor().submit(() -> {
      StringBuffer buffer = new StringBuffer();
      int next;
      try {
        //noinspection InfiniteLoopStatement
        while ((next = is.read()) != -1) {
          buffer.append((char) next);
          System.out.print("" + (char) next);
          String event = nextEvent(buffer);
          if (event == null) {
            continue;
          }
          Matcher m = MOVE_EVENT.matcher(event);
          if (m.matches()) {
            if (!isInProgress()) {
              boardView.reset();
              System.out.println();
              message("New Game Started!\n");
            }
            setInProgress(true);
            String mv = m.group(1);
            System.out.print(" \t");
            message("Move:" + mv);
            PointOfPlay pop = parseMove(mv);
            boardView.move(pop);
            // we got a move
          } else {
            // we got something other than a move, therefore the game is over
            // setting this to false causes the game to be saved to disk.
            setInProgress(false);
          }
        }
      } catch (Exception e) {
        message("oh noes!!!");
        e.printStackTrace();
      }

    });
  }

  private void message(String x) {
    System.out.println(x);
    setMessage(x + "\n");
  }

  private String nextEvent(StringBuffer buff) {
    Matcher m = EVENT.matcher(buff);
    if (m.matches()) {
      String evt = m.group(1);
      buff.delete(0, evt.length());
      return evt;
    }
    return null;
  }

  private PointOfPlay parseMove(String move) {
    Matcher m = MOVE.matcher(move);
    if (!m.matches()) {
      throw new RuntimeException("BAD MOVE: " + move);
    }
    String xChar = m.group(1);
    if ("pass".equals(m.group(3))) {
      return null;
    }
    String yNum = m.group(2);
    int x = xChar.toLowerCase().charAt(0) - 'a';
    // sadly leela doesn't output SGF coordinates, so I is omitted
    if (x > 8) {
      x--;
    }
    int y = Integer.valueOf(yNum) - 1;
    return new PointOfPlay(x, y);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.support.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.support.removePropertyChangeListener(listener);
  }


  public boolean isInProgress() {
    return inProgress;
  }

  public void setInProgress(boolean inProgress) {
    boolean old = this.inProgress;
    this.inProgress = inProgress;
    support.firePropertyChange("inProgress", old, inProgress);
  }
}
