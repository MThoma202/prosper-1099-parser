
package com.prosper1099;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for parsing PDF documents containing Prosper 1099-B records.
 */
@Component
public class DocumentParser {
    private static final Pattern TAX_YEAR_PATTERN_2017_TO_2023 = Pattern.compile("Tax Year (\\d\\d\\d\\d) Combined Form");
    private static final Pattern TAX_YEAR_PATTERN_2024_PLUS = Pattern.compile("(20[234]\\d)");

    List<String> parseDocument(PDDocument document) throws IOException {

        List<String> lines = new ArrayList<>();

        MyPDFTextStripper pdfTextStripper = new MyPDFTextStripper(lines);
        pdfTextStripper.setSortByPosition(true);
        pdfTextStripper.setStartPage(0);
        pdfTextStripper.setEndPage(document.getNumberOfPages());

        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        pdfTextStripper.writeText(document, dummy);

        // Write the last line for completeness.
        pdfTextStripper.writeLineSeparator();

        return lines;
    }

    String parseTaxYear(List<String> lines) {
        for (String line : lines) {
            Matcher matcher1 = TAX_YEAR_PATTERN_2017_TO_2023.matcher(line);
            Matcher matcher2 = TAX_YEAR_PATTERN_2024_PLUS.matcher(line);
            if (matcher1.matches()) {
                return matcher1.group(1);
            }
            if (matcher2.matches()) {
                return matcher2.group(1);
            }
        }

        throw new IllegalStateException("Unable to find tax year.");
    }


    private class MyPDFTextStripper extends PDFTextStripper {

        private final List<String> lines;
        private StringBuilder currentLine = new StringBuilder();


        MyPDFTextStripper(List<String> lines) throws IOException {
            super();
            this.lines = lines;
        }

        /**
         * Override the default functionality of PDFTextStripper.writeString()
         */
        @Override
        protected void writeString(String str, List<TextPosition> textPositions) {
            currentLine.append(str);
        }

        /**
         * Override the default functionality of PDFTextStripper.writeWordSeparator()
         */
        @Override
        protected void writeWordSeparator() {
            currentLine.append(getWordSeparator());
        }

        /**
         * Override the default functionality of PDFTextStripper.writeLineSeparator()
         */
        @Override
        protected void writeLineSeparator() {
            addCurrentLine();
        }

        /**
         * Override the default functionality of PDFTextStripper.writePageEnd()
         */
        @Override
        protected void writePageEnd() {
            addCurrentLine();
        }

        private void addCurrentLine() {
            lines.add(currentLine.toString());

            // reset the buffer
            currentLine.setLength(0);
        }
    }
}
