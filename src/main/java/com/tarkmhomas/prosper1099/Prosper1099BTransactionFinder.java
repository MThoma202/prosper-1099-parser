
package com.tarkmhomas.prosper1099;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for finding the 1099-B transactions from the PDF text.
 */
@Component
public class Prosper1099BTransactionFinder {
    private static final String SHORT = "Short";
    private static final String LONG = "Long";
    private static final String DESCRIPTION_PREFIX = "Prosper Note ";

    private static final Pattern DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN =
            Pattern.compile("(\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\d\\d/\\d\\d/\\d\\d\\d\\d) \\$(\\d*\\.\\d*) (.*)");
    private static final Pattern COST_PATTERN = Pattern.compile("Box 1e. \\$(.*)");
    private static final Pattern IGNORED_LINES_PATTERN = Pattern.compile("1099−B \\(OMB No\\. 1545−0715\\)|"
            + "Short−term transactions for which basis is not reported to the IRS−−Report on Form 8949, Part I, with Box B checked\\.|"
            + "Long−term transactions for which basis is not reported to the IRS−−Report on Form 8949, Part II, with Box E checked\\.|"
            + "1c\\. Date sold 1b\\. Date 1d\\. Proceeds 6\\. Reported to IRS 1a\\. Description Other|"
            + "or disposed acquired of property|" + "\\d* \\d* \\d* \\d* \\d*−\\d* \\d* \\d\\d/\\d\\d/\\d\\d .*");
    private static final Pattern SHORT_TERM_OR_LONG_TERM_PATTERN = Pattern.compile("Box 2\\. (" + SHORT + '|' + LONG + ")−term");
    private static final Pattern REPORTING_CATEGORY_PATTERN = Pattern.compile("Applicable check box on Form 8949 ([A-Z])");


    List<List<String>> find1099BTransactions(List<String> lines, boolean includeShortTerm, boolean includeLongTerm) {

        Iterator<String> iterator = lines.iterator();

        List<List<String>> transactions = new ArrayList<>();

        while (iterator.hasNext()) {
            String nextLine = iterator.next();

            Matcher dateSoldDateAcquiredProceedsDescriptionMatcher =
                    DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN.matcher(nextLine);

            if (dateSoldDateAcquiredProceedsDescriptionMatcher.matches()) {

                String cost = getCost(iterator);

                boolean isShortTerm = getIsShortTerm(iterator);

                String reportingCategory = getReportingCategory(iterator);

                if ((isShortTerm && includeShortTerm) || (!isShortTerm && includeLongTerm)) {
                    List<String> transaction = new ArrayList<>();
                    transaction.add(dateSoldDateAcquiredProceedsDescriptionMatcher.group(1)); // date sold
                    transaction.add(dateSoldDateAcquiredProceedsDescriptionMatcher.group(2)); // date acquired
                    transaction.add(dateSoldDateAcquiredProceedsDescriptionMatcher.group(3)); // sales proceeds
                    transaction.add(DESCRIPTION_PREFIX + dateSoldDateAcquiredProceedsDescriptionMatcher.group(4)); // description
                    transaction.add(cost);
                    transaction.add(reportingCategory);
                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    private String getCost(Iterator<String> iterator) {
        String nextLine = getNextLine(iterator);

        Matcher costMatcher = COST_PATTERN.matcher(nextLine);

        if (!costMatcher.matches()) {
            throw new IllegalStateException("Expected next line to match '" + COST_PATTERN.pattern() + "' but was: " + nextLine);
        }

        return costMatcher.group(1);
    }

    private boolean getIsShortTerm(Iterator<String> iterator) {
        skipLines(iterator, 2);
        String nextLine = getNextLine(iterator);

        Matcher shortTermOrLongTermMatcher = SHORT_TERM_OR_LONG_TERM_PATTERN.matcher(nextLine);

        if (!shortTermOrLongTermMatcher.matches()) {
            throw new IllegalStateException(
                    "Expected next line to match '" + SHORT_TERM_OR_LONG_TERM_PATTERN.pattern() + "' but was: " + nextLine);
        }

        return shortTermOrLongTermMatcher.group(1).equals(SHORT);
    }

    private String getReportingCategory(Iterator<String> iterator) {
        skipLines(iterator, 9);
        String nextLine = getNextLine(iterator);

        Matcher reportingCategoryMater = REPORTING_CATEGORY_PATTERN.matcher(nextLine);

        if (!reportingCategoryMater.matches()) {
            throw new IllegalStateException(
                    "Expected next line to match '" + REPORTING_CATEGORY_PATTERN.pattern() + "' but was: " + nextLine);
        }

        return reportingCategoryMater.group(1);
    }

    private void skipLines(Iterator<String> iterator, int numLinesToSkip) {
        for (int i = 0; i < numLinesToSkip; i++) {
            getNextLine(iterator);
        }
    }

    private String getNextLine(Iterator<String> iterator) {
        String nextLine = iterator.next();

        while (IGNORED_LINES_PATTERN.matcher(nextLine).matches()) {
            // Skip over header/footer lines when there's a line break
            nextLine = iterator.next();
        }

        return nextLine;
    }
}
