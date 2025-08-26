import java.util.Scanner;

import com.example.bank.repository.BankRepository;
import com.example.bank.service.AccountService;
import com.example.bank.service.PaymentService;
import com.example.bank.service.ReportService;
import com.example.bank.service.TransferService;

public class ConsoleApp {
    private final BankRepository repo;
    private final AccountService accounts;
    private final TransferService transfers;
    private final PaymentService payments;
    private final ReportService reports;

    private final Scanner scanner = new Scanner(System.in); //read from stdin

    public ConsoleApp(BankRepository repo, 
                    AccountService accounts, 
                    TransferService transfers, 
                    PaymentService payments, 
                    ReportService reports) {
        this.repo = repo;
        this.accounts = accounts;
        this.transfers = transfers;
        this.payments = payments;
        this.reports = reports;
    }
}
