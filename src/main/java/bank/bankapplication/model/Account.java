package bank.bankapplication.model;

import bank.bankapplication.validation.MinAge;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Size(min = 6, max = 6)
    private String accountNumber;

    @Column(nullable = false)
    @Length(max = 16, message = "Account holder name cannot be longer than 16 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Account holder name must not contain numbers")
    private String accountHolderName;
    private double balance;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @MinAge() // Minimum age requirement
    private LocalDate dateOfBirth;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;
}
