
package com.prosper1099.parser;

import com.prosper1099.TransactionParser;
import com.prosper1099.TransactionParsers;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for parsing the 1099-B transactions from the PDF text.
 */
@Component
public class TransactionParser2024Plus implements TransactionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionParser2024Plus.class);

    private static final String DESCRIPTION_PREFIX = "Prosper Note ";

    private static final Pattern POSITIVE_DOLLAR_VALUE_PATTERN = Pattern.compile("\\$(.*)");
    private static final Pattern NEGATIVE_DOLLAR_VALUE_PATTERN = Pattern.compile("\\(\\$(.*)\\)");
    private static final Pattern REPORTING_CATEGORY_PATTERN = Pattern.compile("Covered \\w*-Term Gains or Losses on Net Proceeds Report on Form 8949, Part II? with Box ([A-Z]) checked\\s*");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "(\\d*-\\d* \\w*) (\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\d\\d/\\d\\d/\\d\\d\\d\\d) (\\(?\\$.*\\)?) (\\(?\\$.*\\)?) (\\(?\\$.*\\)?) (\\(?\\$.*\\)?) (\\(?\\$.*\\)?).*"
    );

    private static final List<Pattern> PATTERNS = Arrays.asList(REPORTING_CATEGORY_PATTERN, TRANSACTION_PATTERN);

    private final TransactionParsers transactionParsers;

    @Autowired
    TransactionParser2024Plus(TransactionParsers transactionParsers) {
        this.transactionParsers = transactionParsers;
    }

    @PostConstruct
    void register() {
        transactionParsers.registerTransactionParser("2024", this);
        transactionParsers.registerTransactionParser("2025", this);
        transactionParsers.registerTransactionParser("2026", this);
        transactionParsers.registerTransactionParser("2027", this);
        transactionParsers.registerTransactionParser("2028", this);
        transactionParsers.registerTransactionParser("2029", this);
        transactionParsers.registerTransactionParser("2030", this);
        transactionParsers.registerTransactionParser("2031", this);
        transactionParsers.registerTransactionParser("2032", this);
        transactionParsers.registerTransactionParser("2033", this);
        transactionParsers.registerTransactionParser("2034", this);
        transactionParsers.registerTransactionParser("2035", this);
        transactionParsers.registerTransactionParser("2036", this);
        transactionParsers.registerTransactionParser("2037", this);
        transactionParsers.registerTransactionParser("2038", this);
        transactionParsers.registerTransactionParser("2039", this);
        transactionParsers.registerTransactionParser("2040", this);
        transactionParsers.registerTransactionParser("2041", this);
        transactionParsers.registerTransactionParser("2042", this);
        transactionParsers.registerTransactionParser("2043", this);
        transactionParsers.registerTransactionParser("2044", this);
    }

    @Override
    public String[] getHeader() {
        return new String[] {"Date Sold", "Date Acquired", "Sales Proceeds", "Description", "Cost Basis", "Reporting Category"};
    }

    @Override
    public List<List<String>> parse1099BTransactions(List<String> lines) {

        Iterator<String> iterator = lines.iterator();

        List<List<String>> transactions = new ArrayList<>();

        String reportingCategory = null;

        while (iterator.hasNext()) {

            MatchResult matchResult = getNextMatch(iterator, PATTERNS);

            if (matchResult != null) {

                if (matchResult.groupCount() == 1) {
                    reportingCategory = matchResult.group(1);
                    LOGGER.debug("reportingCategory: {}", reportingCategory);
                } else {

                    String description = DESCRIPTION_PREFIX + matchResult.group(1);
                    LOGGER.debug("description: {}", description);

                    String dateAcquired = matchResult.group(2);
                    LOGGER.debug("dateAcquired: {}", dateAcquired);

                    String dateSold = matchResult.group(3);
                    LOGGER.debug("dateSold: {}", dateSold);

                    String salesProceeds = parseDollarValue(matchResult.group(4));
                    LOGGER.debug("salesProceeds: {}", salesProceeds);

                    String costBasis = parseDollarValue(matchResult.group(5));
                    LOGGER.debug("costBasis: {}", costBasis);

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

    private MatchResult getNextMatch(Iterator<String> iterator, List<Pattern> patternsToMatch) {
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

            LOGGER.debug("Ignoring: {}", nextLine);
        }

        return null;
    }
}
