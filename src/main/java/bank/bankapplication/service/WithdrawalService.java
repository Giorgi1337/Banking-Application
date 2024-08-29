package bank.bankapplication.service;


import bank.bankapplication.model.Account;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.repository.WithdrawalRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;

    public Page<Withdrawal> getWithdrawalsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("account.accountHolderName").ascending());
        return withdrawalRepository.findAll(pageable);
    }

    public void deleteWithdrawalsByAccount(Account account) {
        withdrawalRepository.deleteAll(withdrawalRepository.findByAccount(account));
    }

    public void saveWithdrawal(Account account, double amount) {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAccount(account);
        withdrawal.setAmount(amount);
        withdrawalRepository.save(withdrawal);
    }

    public ResponseEntity<InputStreamResource> generateWithdrawalsPdf(int page, int size) throws DocumentException, IOException {
        List<Withdrawal> withdrawals = withdrawalRepository.findAll(PageRequest.of(page, size)).getContent();
        List<List<String>> rows = withdrawals.stream()
                .map(w -> List.of(
                        w.getAccount().getAccountHolderName(),
                        String.format("$%.2f", w.getAmount()),
                        w.getWithdrawDate().toString()
                ))
                .collect(Collectors.toList());

        return generatePdfResponse("Withdrawals Report",
                new String[]{"Account Holder Name", "Amount", "Date"},
                rows,
                "withdrawals_report_page_" + page + ".pdf");
    }

    private ResponseEntity<InputStreamResource> generatePdfResponse(String title, String[] headers, List<List<String>> rows, String fileName) throws DocumentException, IOException {
        InputStreamResource pdfStream = PdfUtils.generatePdf(title, headers, rows, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfStream);
    }
}
