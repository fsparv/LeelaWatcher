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

package leelawatcher.sgf;

// This interface defines the properties that Total Screen Ban will interpret
// The optional KO property will not be implemented. If the move isn't legal
// under some rule set somewhere, how can it be of interest? Total Screen Ban
// will probably have a no-rules enforced setting to allow viewing of games
// played under future, unknown rule sets.

public interface SGFproperties {

  // These properties match SGF version 4.

  String FORMAT_VERSION = "4";

  // Move properties - KO for allowing illegal moves will not be imped.

  String BLACKMOVE = "B";
  String WHITEMOVE = "W";
  String MOVENUM = "MN";

  // Setup properties

  String ADDBLACK = "AB";
  String ADDEMPTY = "AE";
  String ADDWHITE = "AW";
  String PLAYERTURN = "PL";

  // Node annotation properties

  String COMMENT = "C";
  String EVENPOS = "DM";
  String GOODBLACK = "GB";
  String GOODWHITE = "GW";
  String HOTSPOT = "HO";
  String NODENAME = "N";
  String UNCLEAR = "UC";
  String ESTSCORE = "V";

  // Move annotation properties

  String BADMOVE = "BM";
  String DOUBTFUL = "DO";
  String INTERESTING = "IT";
  String TESUJI = "TE";

  // Markup properties

  String ARROW = "AR";
  String CIRCLE = "CR";
  String DISPLAYDIM = "DD";
  String TEXTLABEL = "LB";
  String LINE = "LN";
  String XMARK = "MA";
  String SELECTED = "SL";
  String SQUARE = "SQ";
  String TRIANGLE = "TR";

  // Root properties

  String APPLICATION = "AP";
  String CHARSET = "CA";
  String FILEFORMAT = "FF";
  String GAMETYPE = "GM";
  String VARSTYLE = "ST";
  String SIZE = "SZ";

  // Game info properties

  String ANNOTATOR = "AN";
  String BLACKRANK = "BR";
  String BLACKTEAM = "BT";
  String COPYRIGHT = "CP";
  String DATE = "DT";
  String EVENT = "EV";
  String GAMENAME = "GN";
  String GAMEINFO = "GC";
  String OPENING = "ON";
  String OVERTIMETYPE = "OT";
  String PLAYERBLACK = "PB";
  String PLACE = "PC";
  String PLAYERWHITE = "PW";
  String RESULT = "RE";
  String ROUND = "RO";
  String RULES = "RU";
  String SOURCE = "SO";
  String TIMELIMIT = "TM";
  String USER = "US";
  String WHITERANK = "WR";
  String WHITETEAM = "WT";

  String HANDICAP = "HA";
  String KOMI = "KM";


  // Timing properties

  String BLACKTIME = "BL";
  String BLACKSTONES = "0B";
  String WHITESTONES = "OW";
  String WHITETIME = "WL";

  // Misc properties

  String FIGURE = "FG";
  String PRINTMOVES = "PM";
  String VIEWAREA = "VW";

}









