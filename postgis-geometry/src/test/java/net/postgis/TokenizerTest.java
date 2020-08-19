/*
 * TokenizerTest.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package net.postgis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;


public class TokenizerTest {

    private static final Logger logger = LoggerFactory.getLogger(TokenizerTest.class);


    @Test
    public void testTokenizer() {
        char delimiterL1 = ',';
        char delimiterL2 = ' ';
        String stringToTokenize = "((1 2 3),(4 5 6),(7 8 9)";
        logger.debug("tokenizing string value => {}", stringToTokenize);
        List<String> tokensLevel1 = GeometryTokenizer.tokenize(GeometryTokenizer.removeLeadingAndTrailingStrings(stringToTokenize, "(", ")"), delimiterL1);
        logger.debug("level 1 tokens [delimiter = {}] [tokenCount = {}]", delimiterL1, tokensLevel1.size());
        for (String tokenL1 : tokensLevel1) {
            logger.debug("L1 token => {} / {}", tokenL1, GeometryTokenizer.removeLeadingAndTrailingStrings(tokenL1, "(", ")"));
            List<String> tokensLevel2 = GeometryTokenizer.tokenize(GeometryTokenizer.removeLeadingAndTrailingStrings(tokenL1, "(", ")"), delimiterL2);
            logger.debug("level 2 tokens [delimiter = {}] [tokenCount = {}]", delimiterL2, tokensLevel2.size());
            for (String tokenL2 : tokensLevel2) {
                logger.debug("L2 token => {} / {}", tokenL2, GeometryTokenizer.removeLeadingAndTrailingStrings(tokenL2, "(", ")"));
            }
        }
    }

}