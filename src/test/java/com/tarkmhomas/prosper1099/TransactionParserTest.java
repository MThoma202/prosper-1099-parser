package com.tarkmhomas.prosper1099;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests TransactionFinder.
 */
public class TransactionParserTest {

    private final TransactionParser transactionParser = new TransactionParser();

    @DataProvider
    Object[][] valuesForTestParseCostBasis() {
        return new Object[][] {
                {". Box 1e. $0.00", "0.00"},
                {". Box 1e. $43.87", "43.87"},
                {"asdf Box 1e. ($37.32)", "-37.32"}
        };
    }

    @Test(dataProvider = "valuesForTestParseCostBasis")
    public void testParseCostBasis(String nextLine, String expectedCostBasis) {
        String costBasis = transactionParser.parseCostBasis(nextLine);

        Assert.assertEquals(costBasis, expectedCostBasis);
    }

    @DataProvider
    Object[][] valuesForTestParseIsShortTerm() {
        return new Object[][] {
                {"Box 2. Short−term", true},
                {"Box 2. Short-term", true},
                {"Box 2. Long−term", false},
                {"Box 2. Long-term", false},
        };
    }

    @Test(dataProvider = "valuesForTestParseIsShortTerm")
    public void testParseIsShortTerm(String nextLine, boolean expectedIsShortTerm) {
        boolean isShortTerm = transactionParser.parseIsShortTerm(nextLine);

        Assert.assertEquals(isShortTerm, expectedIsShortTerm);
    }
}
