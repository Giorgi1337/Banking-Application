package bank.bankapplication.model;

import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.validation.MaxAge;
import bank.bankapplication.validation.MinAge;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    @Pattern(regexp = "\\d+", message = "Account number length must be 6 and numeric")
    @Size(min = 6, max = 6, message = "Size must be 6")
    private String accountNumber;

    @Column(nullable = false)
    @Length(max = 16, message = "Account holder name cannot be longer than 16 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Account holder name must not contain numbers")
    private String accountHolderName;
    private double balance;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @MinAge // Minimum age requirement
    @MaxAge // Maximum age
    private LocalDate dateOfBirth;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(nullable = false)
    @Email(message = "Email should be valid")
    private String emailAddress;

    @Column(nullable = false)
    @Pattern(regexp = "\\d+", message = "phoneNumber must be numeric")
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String accountType;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Withdrawal> withdrawals;
}
