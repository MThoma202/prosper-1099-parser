
package com.prosper1099;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of 1099-B Transaction Parsers. Since the transaction format is slightly
 * different each year so a different parser is used for each year.
 */
@Component
public class TransactionParsers {

    private final Map<String, TransactionParser> transactionParsers = new HashMap<>();


    public void registerTransactionParser(String taxYear, TransactionParser transactionParser) {
        if (transactionParsers.containsKey(taxYear)) {
            throw new IllegalStateException("Transaction Parser already registered using tax year " + taxYear);
        }

        transactionParsers.put(taxYear, transactionParser);
    }

    TransactionParser getTransactionParser(String taxYear) {
        if (!transactionParsers.containsKey(taxYear)) {
            throw new IllegalStateException("Tax year " + taxYear + " isn't supported.");
        }

        return transactionParsers.get(taxYear);
    }
}
