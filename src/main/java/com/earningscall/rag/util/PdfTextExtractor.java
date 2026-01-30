package com.earningscall.rag.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PdfTextExtractor {

    public List<PageContent> extractText(InputStream pdfInputStream) throws IOException {
        List<PageContent> pages = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            log.info("Extracting text from PDF with {} pages", totalPages);

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);

                String pageText = stripper.getText(document);

                // Clean up the text
                pageText = cleanText(pageText);

                if (!pageText.trim().isEmpty()) {
                    pages.add(new PageContent(pageNum, pageText));
                }
            }

            log.info("Successfully extracted text from {} pages", pages.size());
        }

        return pages;
    }

    private String cleanText(String text) {
        // Remove excessive whitespace
        text = text.replaceAll("\\s+", " ");

        // Remove special characters that might interfere with processing
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        // Normalize line breaks
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        return text.trim();
    }

    public record PageContent(int pageNumber, String text) {
    }
}
