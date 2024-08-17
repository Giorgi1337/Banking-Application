package bank.bankapplication.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static InputStreamResource generatePdf(String title, String[] headers, List<List<String>> rows, String fileName) throws DocumentException, IOException {
        // Create a new PDF document
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Add Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(titleParagraph);

        // Add Spacer
        document.add(new Paragraph("\n"));

        // Add Table
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Add Table Headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(headers[0], headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
        for (int i = 1; i < headers.length; i++) {
            cell.setPhrase(new Phrase(headers[i], headerFont));
            table.addCell(cell);
        }

        // Add Table Rows
        Font rowFont = new Font(Font.FontFamily.HELVETICA, 12);
        for (List<String> row : rows) {
            for (String cellContent : row) {
                // Format date without milliseconds
                String formattedContent = cellContent;
                if (cellContent.contains(":")) {
                    try {
                        Date date = DATE_FORMAT.parse(cellContent);
                        formattedContent = DATE_FORMAT.format(date);
                    } catch (ParseException e) {
                        // If parsing fails, use the original content
                    }
                }
                table.addCell(new Phrase(formattedContent, rowFont));
            }
        }
        document.add(table);

        // Close Document
        document.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        HttpHeaders headersResponse = new HttpHeaders();
        headersResponse.add("Content-Disposition", "attachment; filename=" + fileName);

        return new InputStreamResource(bais);
    }
}
