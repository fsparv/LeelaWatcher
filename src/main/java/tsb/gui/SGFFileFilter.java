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

package tsb.gui;

/**
 * A filter for smart go format files. Files that end in <code>".sgf"</code>
 * should pass the filter. The filter is case insensitive.
 *
 * @author Gus
 * @version $Revision$
 */
public class SGFFileFilter extends javax.swing.filechooser.FileFilter {

  /**
   * Creates a new instance of SGFFileFilter
   */
  public SGFFileFilter() {
  }

  public boolean accept(java.io.File file) {
    String name = file.getName();
    if ((name.indexOf(".sgf") == (name.length() - 4)) ||
        (name.indexOf(".SGF") == (name.length() - 4)) ||
        (name.indexOf(".Sgf") == (name.length() - 4)) ||
        (name.indexOf(".sGf") == (name.length() - 4)) ||
        (name.indexOf(".sgF") == (name.length() - 4)) ||
        (name.indexOf(".sGF") == (name.length() - 4)) ||
        (name.indexOf(".SgF") == (name.length() - 4)) ||
        (name.indexOf(".SGf") == (name.length() - 4))) {
      return true;
    } else {
      return false;
    }
  }

  public String getDescription() {
    return "Smart Go Format";
  }

}
