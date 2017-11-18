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

package tsb;

/**
 * A central location for application wide constants. Any immutable value
 * needed by multiple classes, or relating to the application as a whole
 * should be defined here.
 *
 * @author Patrick G. Heck
 * @version $Revision$
 */

public interface TsbConstants {
  /**
   * The current version of the program. String is used since the value
   * of the version is never used in computation.
   */
  String VERSION_STR = "1.0";

  /**
   * The name of the program.
   */
  String PROG_NAME = "LeelaWatcher";

  /**
   * The location of the woodgrain image file used as a background for the
   * playing board.
   */
  String BRD_BACKGRD = "images/wd-back.gif";
}

/*
 * $Log$
 * Revision 1.3  2003/07/19 03:08:07  gus
 * added PROG_NAME constant
 *
 * Revision 1.2  2003/07/19 02:50:04  gus
 * New License based on the Apache License, Yeah open source :)
 *
 * Revision 1.1.1.1  2002/12/15 07:02:56  gus
 * Initial import into cvs server running on Aptiva
 *
 * Revision 1.1  2002/04/16 02:08:59  togo
 * Migrated globalInfo.java to TsbConstants.java, and fixed refering files.
 * Also found remaining references to board.java and cleaned them up. Removed
 * board.java (which has been replaced by Board.java).
 *
 */
