package com.prosper1099;

import java.util.List;

/**
 * Responsible for parsing the 1099-B transactions from the PDF text.
 */
public interface TransactionParser {

    String[] getHeader();

    List<List<String>> parse1099BTransactions(List<String> lines, String taxYear);
}
