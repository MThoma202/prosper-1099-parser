
package com.tarkmhomas.prosper1099;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for parsing the 1099-B transactions from the PDF text.
 */
@Component
public class TransactionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionParser.class);

    private static final String SHORT = "Short";
    private static final String LONG = "Long";
    private static final String DESCRIPTION_PREFIX = "Prosper Note ";

    private static final Pattern DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN =
            Pattern.compile("(\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\(?\\$\\d*\\.\\d*\\)?) (.*[A-Z]+).*");
    private static final Pattern POSITIVE_DOLLAR_VALUE_PATTERN = Pattern.compile("\\$(.*)");
    private static final Pattern NEGATIVE_DOLLAR_VALUE_PATTERN = Pattern.compile("\\(\\$(.*)\\)");
    private static final Pattern COST_BASIS_PATTERN = Pattern.compile(".*Box 1e\\. (\\(?\\$.*\\)?)");
    private static final Pattern IGNORED_HEADER_FOOTER_LINES_PATTERN =
            Pattern.compile("1099[−\\-]B \\(OMB No\\. 1545[−\\-]0715\\)"
                    + "|Short[−\\-]term transactions for which basis is not reported to the IRS[−\\-][−\\-]Report on Form 8949, Part I, with Box B checked\\."
                    + "|Long[−\\-]term transactions for which basis is not reported to the IRS[−\\-][−\\-]Report on Form 8949, Part II, with Box E checked\\."
                    + "|1c\\. Date sold 1b\\. Date 1d\\. Proceeds 6\\. Reported to IRS 1a\\. Description Other"
                    + "|or disposed acquired of property" + "|\\d* \\d* \\d* \\d* \\d*−\\d* \\d* \\d\\d/\\d\\d/\\d\\d .*"
                    + "|.*\\.PDF");
    private static final Pattern SHORT_TERM_OR_LONG_TERM_PATTERN =
            Pattern.compile("Box 2\\. (" + SHORT + '|' + LONG + ")[−\\-]term");
    private static final Pattern REPORTING_CATEGORY_PATTERN = Pattern.compile("Applicable check box on Form 8949 ([A-Z])");


    List<List<String>> parse1099BTransactions(List<String> lines, boolean includeShortTerm, boolean includeLongTerm) {

        Iterator<String> iterator = lines.iterator();

        List<List<String>> transactions = new ArrayList<>();

        while (iterator.hasNext()) {
            String nextLine = getNextLine(iterator);

            Matcher dateSoldDateAcquiredProceedsDescriptionMatcher =
                    DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN.matcher(nextLine);

            if (dateSoldDateAcquiredProceedsDescriptionMatcher.matches()) {

                String dateSold = dateSoldDateAcquiredProceedsDescriptionMatcher.group(1);
                LOGGER.debug("dateSold: Obtained value '{}' from '{}'", dateSold, nextLine);

                String dateAcquired = dateSoldDateAcquiredProceedsDescriptionMatcher.group(2);
                LOGGER.debug("dateAcquired: Obtained value '{}' from '{}'", dateAcquired, nextLine);

                String salesProceeds = parseDollarValue(dateSoldDateAcquiredProceedsDescriptionMatcher.group(3));
                LOGGER.debug("salesProceeds: Obtained value '{}' from '{}'", salesProceeds, nextLine);

                String description = DESCRIPTION_PREFIX + dateSoldDateAcquiredProceedsDescriptionMatcher.group(4);
                LOGGER.debug("description: Obtained value '{}' from '{}'", description, nextLine);

                String costBasis = parseCostBasis(getNextLine(iterator));

                boolean isShortTerm = parseIsShortTerm(getNextLine(iterator, 2));

                String reportingCategory = parseReportingCategory(getNextLine(iterator, 9));

                if ((isShortTerm && includeShortTerm) || (!isShortTerm && includeLongTerm)) {
                    List<String> transaction = new ArrayList<>();
                    transaction.add(dateSold);
                    transaction.add(dateAcquired);
                    transaction.add(salesProceeds);
                    transaction.add(description);
                    transaction.add(costBasis);
                    transaction.add(reportingCategory);
                    transactions.add(transaction);
                }
            } else {
                if (COST_BASIS_PATTERN.matcher(nextLine).matches() || SHORT_TERM_OR_LONG_TERM_PATTERN.matcher(nextLine).matches()
                        || REPORTING_CATEGORY_PATTERN.matcher(nextLine).matches()) {
                    throw new IllegalStateException("Found line '" + nextLine + "' without a preceding line matching pattern '"
                            + DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN.pattern() + "'.");
                }

                LOGGER.debug("Ignoring unrecognized line: '{}'", nextLine);
            }
        }

        return transactions;
    }

    private String parseDollarValue(String dollarValue) {

        Matcher positiveDollarValueMatcher = POSITIVE_DOLLAR_VALUE_PATTERN.matcher(dollarValue);
        Matcher negativeDollarValueMatcher = NEGATIVE_DOLLAR_VALUE_PATTERN.matcher(dollarValue);

        if (positiveDollarValueMatcher.matches()) {
            return positiveDollarValueMatcher.group(1);
        }

        if (negativeDollarValueMatcher.matches()) {
            return '-' + negativeDollarValueMatcher.group(1);
        }

        throw new IllegalStateException("Expected dollar value '" + dollarValue + "' to match pattern '"
                + POSITIVE_DOLLAR_VALUE_PATTERN.pattern() + "' or '" + NEGATIVE_DOLLAR_VALUE_PATTERN.pattern() + "'");
    }

    String parseCostBasis(String nextLine) {

        Matcher costMatcher = COST_BASIS_PATTERN.matcher(nextLine);

        if (!costMatcher.matches()) {
            throw new IllegalStateException(
                    "Expected next line '" + nextLine + "' to match pattern '" + COST_BASIS_PATTERN.pattern() + "'");
        }

        String costBasis = parseDollarValue(costMatcher.group(1));
        LOGGER.debug("costBasis: Obtained value '{}' from '{}'", costBasis, nextLine);

        return costBasis;
    }

    boolean parseIsShortTerm(String nextLine) {

        Matcher shortTermOrLongTermMatcher = SHORT_TERM_OR_LONG_TERM_PATTERN.matcher(nextLine);

        if (!shortTermOrLongTermMatcher.matches()) {
            throw new IllegalStateException(
                    "Expected next line '" + nextLine + "' to match pattern '" + SHORT_TERM_OR_LONG_TERM_PATTERN.pattern() + "'");
        }

        boolean isShortTerm = shortTermOrLongTermMatcher.group(1).equals(SHORT);

        LOGGER.debug("isShortTerm: Obtained value '{}' from '{}'", isShortTerm, nextLine);
        return isShortTerm;
    }

    String parseReportingCategory(String nextLine) {

        Matcher reportingCategoryMatcher = REPORTING_CATEGORY_PATTERN.matcher(nextLine);

        if (!reportingCategoryMatcher.matches()) {
            throw new IllegalStateException(
                    "Expected next line '" + nextLine + "' to match pattern '" + REPORTING_CATEGORY_PATTERN.pattern() + "'");
        }

        LOGGER.debug("reportingCategory: Obtained value '{}' from '{}'", reportingCategoryMatcher.group(1), nextLine);
        return reportingCategoryMatcher.group(1);
    }

    private String getNextLine(Iterator<String> iterator, int numLinesToSkip) {
        for (int i = 1; i <= numLinesToSkip; i++) {
            String nextLine = getNextLine(iterator);
            LOGGER.debug("Skipping line {} of {}: '{}'", i, numLinesToSkip, nextLine);
        }

        return getNextLine(iterator);
    }

    private String getNextLine(Iterator<String> iterator) {
        String nextLine = iterator.next();

        while (IGNORED_HEADER_FOOTER_LINES_PATTERN.matcher(nextLine).matches()) {
            // Skip over header/footer lines that can occur randomly within and between transaction
            // lines
            LOGGER.debug("Ignoring header/footer line: '{}'", nextLine);
            nextLine = iterator.next();
        }

        return nextLine;
    }
}
