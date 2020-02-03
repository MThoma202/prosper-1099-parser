
package com.prosper1099;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Provides HTTP endpoints that can be called by external applications.
 */
@Controller
public class ConverterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);
    private static final String EXPECTED_FIRST_LINE = "PROSPER FUNDING LLC";

    private final DocumentParser documentParser;
    private final TransactionParsers transactionParsers;


    @Autowired
    ConverterController(DocumentParser documentParser, TransactionParsers transactionParsers) {
        this.documentParser = documentParser;
        this.transactionParsers = transactionParsers;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/convertPdfToCsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "text/csv")
    public String convertPdfToCsv(@RequestParam("file") MultipartFile pdfFile) throws IOException {

        PDFParser pdfParser = new PDFParser(new RandomAccessBuffer(pdfFile.getInputStream()));

        pdfParser.parse();

        List<String> lines;
        try (PDDocument pdDocument = pdfParser.getPDDocument()) {
            lines = documentParser.parseDocument(pdDocument);
        }

        if (lines.isEmpty() || !lines.get(0).equals(EXPECTED_FIRST_LINE)) {
            throw new IllegalStateException("First line must match \"" + EXPECTED_FIRST_LINE + "\".");
        }

        String taxYear = documentParser.parseTaxYear(lines);

        TransactionParser transactionParser = transactionParsers.getTransactionParser(taxYear);

        List<List<String>> transactions = transactionParser.parse1099BTransactions(lines, taxYear);

        StringBuilder out = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(transactionParser.getHeader()));
        printer.printRecords(transactions);

        return out.toString();
    }
}
