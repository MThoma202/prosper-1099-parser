
package com.prosper1099.parser;

import com.prosper1099.TransactionParser;
import com.prosper1099.TransactionParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for parsing the 1099-B transactions from the PDF text.
 */
@Component
public class TransactionParser2017Plus implements TransactionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionParser2017Plus.class);

    private static final String DESCRIPTION_PREFIX = "Prosper Note ";

    private static final Pattern BOX_1ABCD_DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN =
            Pattern.compile("(\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\(?\\$\\d*\\.\\d*\\)?) (.*[A-Z]+).*");
    private static final Pattern POSITIVE_DOLLAR_VALUE_PATTERN = Pattern.compile("\\$(.*)");
    private static final Pattern NEGATIVE_DOLLAR_VALUE_PATTERN = Pattern.compile("\\(\\$(.*)\\)");
    private static final Pattern BOX_1E_COST_BASIS_PATTERN = Pattern.compile(".*Box 1e\\. (\\(?\\$.*\\)?)");
    private static final Pattern REPORTING_CATEGORY_PATTERN = Pattern.compile("Applicable check\\s?box on Form 8949 ([A-Z])");

    private final TransactionParsers transactionParsers;

    @Autowired TransactionParser2017Plus(TransactionParsers transactionParsers) {
        this.transactionParsers = transactionParsers;
    }

    @PostConstruct
    void register() {
        transactionParsers.registerTransactionParser("2017", this);
        transactionParsers.registerTransactionParser("2018", this);
        transactionParsers.registerTransactionParser("2019", this);
        transactionParsers.registerTransactionParser("2020", this);
    }

    @Override
    public String[] getHeader() {
        return new String[] {"Date Sold", "Date Acquired", "Sales Proceeds", "Description", "Cost Basis", "Reporting Category"};
    }

    @Override
    public List<List<String>> parse1099BTransactions(List<String> lines) {

        Iterator<String> iterator = lines.iterator();

        List<List<String>> transactions = new ArrayList<>();

        while (iterator.hasNext()) {

            MatchResult dateSoldDateAcquiredProceedsDescriptionMatchResult =
                    getNextMatch(iterator, false, BOX_1ABCD_DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN, BOX_1E_COST_BASIS_PATTERN,
                            REPORTING_CATEGORY_PATTERN);

            if (dateSoldDateAcquiredProceedsDescriptionMatchResult != null) {

                String dateSold = dateSoldDateAcquiredProceedsDescriptionMatchResult.group(1);
                LOGGER.debug("dateSold: {}", dateSold);

                String dateAcquired = dateSoldDateAcquiredProceedsDescriptionMatchResult.group(2);
                LOGGER.debug("dateAcquired: {}", dateAcquired);

                String salesProceeds = parseDollarValue(dateSoldDateAcquiredProceedsDescriptionMatchResult.group(3));
                LOGGER.debug("salesProceeds: {}", salesProceeds);

                String description = DESCRIPTION_PREFIX + dateSoldDateAcquiredProceedsDescriptionMatchResult.group(4);
                LOGGER.debug("description: {}", description);

                String costBasis = parseCostBasis(Objects.requireNonNull(getNextMatch(iterator, true, BOX_1E_COST_BASIS_PATTERN,
                        BOX_1ABCD_DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN, REPORTING_CATEGORY_PATTERN)));

                String reportingCategory = parseReportingCategory(Objects.requireNonNull(getNextMatch(iterator, true,  REPORTING_CATEGORY_PATTERN,
                        BOX_1ABCD_DATE_SOLD_DATE_ACQUIRED_PROCEEDS_DESCRIPTION_PATTERN, BOX_1E_COST_BASIS_PATTERN)));

                List<String> transaction = new ArrayList<>();
                transaction.add(dateSold);
                transaction.add(dateAcquired);
                transaction.add(salesProceeds);
                transaction.add(description);
                transaction.add(costBasis);
                transaction.add(reportingCategory);
                transactions.add(transaction);
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

    String parseCostBasis(MatchResult matchResult) {

        String costBasis = parseDollarValue(matchResult.group(1));
        LOGGER.debug("costBasis: {}", costBasis);

        return costBasis;
    }

    String parseReportingCategory(MatchResult matchResult) {

        String reportingCategory = matchResult.group(1);
        LOGGER.debug("reportingCategory: {}", reportingCategory);

        return reportingCategory;
    }

    private MatchResult getNextMatch(Iterator<String> iterator, boolean exceptionIfNotFound, Pattern patternToMatch, Pattern... patternsNotExpected) {
        return getNextMatch(iterator, exceptionIfNotFound, Collections.singletonList(patternToMatch), patternsNotExpected);
    }

    private MatchResult getNextMatch(Iterator<String> iterator, boolean exceptionIfNotFound, List<Pattern> patternsToMatch, Pattern... patternsNotExpected) {
        String nextLine;

        while (iterator.hasNext()) {
            nextLine = iterator.next();

            for (Pattern patternToMatch : patternsToMatch) {
                Matcher matcher = patternToMatch.matcher(nextLine);
                if (matcher.matches()) {
                    LOGGER.debug("Parsing: {}", nextLine);
                    return matcher.toMatchResult();
                }
            }

            for (Pattern patternNotExpected : patternsNotExpected) {
                if (patternNotExpected.matcher(nextLine).matches()) {
                    throw new IllegalStateException("Found line '" + nextLine + "' without a preceding line matching " + Arrays.toString(patternsToMatch.stream().map(Pattern::pattern).toArray()));
                }
            }

            LOGGER.debug("Ignoring: {}", nextLine);
        }

        if (exceptionIfNotFound) {
            throw new IllegalStateException("Reached end of file but expected to find line matching " + Arrays.toString(patternsToMatch.stream().map(Pattern::pattern).toArray()));
        }

        return null;
    }
}
