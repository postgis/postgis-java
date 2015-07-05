/*
 * GeometryTokenizer.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - geometry model
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA or visit the web at
 * http://www.gnu.org.
 *
 */

package org.postgis;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class GeometryTokenizer {


    public static List<String> tokenize(String string, char delimiter) {
        List<String> tokens = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        int consumed = 0;
        for (int position = 0; position < string.length(); position++) {
            char character = string.charAt(position);
            if ((character == '(') || (character == '[')) {
                stack.push(character);
            } else if (((character == ')') && (stack.peek() == '(')) ||
                       ((character == ']') && (stack.peek() == '['))
                      ) {
                stack.pop();
            }
            if ((character == delimiter) && (stack.size() == 0)) {
                tokens.add(string.substring(consumed, position));
                consumed = position + 1;
            }
        }
        if (consumed < string.length()) {
            tokens.add(string.substring(consumed));
        }
        return tokens;
    }


    public static String removeLeadingAndTrailingStrings(String string, String leadingString, String trailingString) {
        int startIndex = string.indexOf(leadingString);
        if (startIndex == -1) {
            startIndex = 0;
        } else {
            startIndex += leadingString.length();
        }

        int endIndex = string.lastIndexOf(trailingString);
        if (endIndex == -1) {
            endIndex = string.length();
        }
        return string.substring(startIndex, endIndex);
    }


}