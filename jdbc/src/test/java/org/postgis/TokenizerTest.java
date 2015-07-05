package org.postgis;

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