package bank.bankapplication.service;


import bank.bankapplication.model.Transaction;
import bank.bankapplication.repository.TransactionRepository;
import bank.bankapplication.utils.PdfUtils;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<Transaction> getTransactionsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findAll(pageable);
    }

    public List<Transaction> getTransactionsByAccountHolderName(String accountHolderName) {
        List<Transaction> fromTransactions = transactionRepository.findByFromAccountHolderName(accountHolderName);
        List<Transaction> toTransactions = transactionRepository.findByToAccountHolderName(accountHolderName);
        fromTransactions.addAll(toTransactions);
        return fromTransactions;
    }

    public ResponseEntity<InputStreamResource> generateTransactionsPdfByHolder(String accountHolderName) throws DocumentException, IOException {
        List<Transaction> transactions = getTransactionsByAccountHolderName(accountHolderName);
        List<List<String>> rows = mapTransactionsToRows(transactions);

        return generatePdfResponse("Transactions for " + accountHolderName,
                new String[]{"From", "To", "Type", "Amount", "Date"},
                rows,
                "transactions_" + accountHolderName + ".pdf");
    }

    public void recordTransaction(String from, String to, String type, double amount) {
        Transaction transaction = new Transaction();
        transaction.setFromAccountHolderName(from);
        transaction.setToAccountHolderName(to);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
    }

    public ResponseEntity<InputStreamResource> generateTransactionsPdf(int page, int size) throws DocumentException, IOException {
        List<Transaction> transactions = transactionRepository.findAll(PageRequest.of(page, size)).getContent();
        List<List<String>> rows = mapTransactionsToRows(transactions);

        return generatePdfResponse("Transactions Report",
                new String[]{"From Account Holder", "To Account Holder", "Type", "Amount", "Date"},
                rows,
                "transactions_report_page_" + page + ".pdf");
    }

    private List<List<String>> mapTransactionsToRows(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> List.of(
                        t.getFromAccountHolderName() != null ? t.getFromAccountHolderName() : "N/A",
                        t.getToAccountHolderName(),
                        t.getTransactionType(),
                        String.format("%.1f", t.getAmount()),
                        t.getTransactionDate().toString()
                ))
                .collect(Collectors.toList());
    }

    private ResponseEntity<InputStreamResource> generatePdfResponse(String title, String[] headers, List<List<String>> rows, String fileName) throws DocumentException, IOException {
        InputStreamResource pdfStream = PdfUtils.generatePdf(title, headers, rows, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfStream);
    }

    public Page<Transaction> searchTransactionsPaginated(
            LocalDate fromDate,
            LocalDate toDate,
            Double minAmount,
            Double maxAmount,
            String transactionType,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findTransactions(fromDate, toDate, minAmount, maxAmount, transactionType, pageable);
    }

}
