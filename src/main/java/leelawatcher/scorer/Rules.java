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
package leelawatcher.scorer;


import leelawatcher.goboard.Board;
import leelawatcher.goboard.MarkablePosition;
import leelawatcher.goboard.PointOfPlay;

public interface Rules {
  boolean isEmpty(PointOfPlay p, Board board);

  boolean isSelfCapture(PointOfPlay p, Board board);

  boolean isSelfCaptureAllowed();

  boolean isKo(PointOfPlay p, Board board);

  boolean isLegalMove(PointOfPlay p, Board board);

  int countLibs(PointOfPlay p, int counter, MarkablePosition m, Board board);
    /*	  public boolean isGroupCaptured(PointOfPlay p);
    public position moveResult(PointOfPlay p);
    */
}
