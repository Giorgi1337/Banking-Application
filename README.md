# Bank Application

This is a comprehensive bank application built using Spring Boot, designed to manage customer accounts, transactions, 
withdrawals,and deposits. It includes features like account creation, profile management, fund transfers, and transaction history management. 
The application also supports dynamic searching, pagination, sorting, and PDF generation for reports.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Contact](#Contact)

## Features

#### Account Management

* Create Account: Register new accounts with validation on inputs like account number, email address, and phone number.
* Update Account: Modify existing account details, with updates to non-null fields only.
* Delete Account: Remove accounts by their account number.

#### Transaction Management

* Deposit Funds: Deposit funds into an account and update the balance
* Withdraw Funds: Withdraw funds from an account, ensuring sufficient balance.
* Transfer Funds: Transfer funds between accounts, with validation on the sender's balance.

#### Transaction History

* View Transactions: View all transactions with pagination, filtering by date, amount, and transaction type.
* Recent Transactions: Display recent transactions on the account profile page.
* Transaction Search: Dynamic search for transactions by account holder name with pagination.

#### PDF Generation

* Download Transactions as PDF: Generate and download PDF reports of transactions, filtered by account holder or other criteria.
* Download Withdrawals as PDF: Generate and download PDF reports of all withdrawals.

#### Error Handling

* Custom Error Pages: Display user-friendly error messages for common issues such as account not found, duplicate account numbers, invalid amounts, and more.

## Technologies Used

- **Frontend**:
  - **HTML**: Structure and markup of the web pages.
  - **CSS**: Styling and layout of the web pages.
  - **Bootstrap**: Responsive design and UI components.
  - **JavaScript**: Client-side scripting and interactivity.

- **Backend**:
  - **Spring Boot**: Java-based framework for building the backend.
  - **Spring Data JPA**: Data access and ORM.
  - **Thymeleaf**: Server-side Java template engine for rendering dynamic web pages.
  - **Lombok**: Java library to reduce boilerplate code.
  - **iText PDF**: PDF generation and manipulation library.
  - **Jakarta Validation**: Bean validation framework used for input validation.

- **Database**:
  - **PostgreSQL**: Relational database for storing data.

- **Build**:
  - **Maven**: Build automation tool.

## Getting Started

* Java 17 or higher
* Maven
* Spring Boot 3.0+
* A PostgreSQL database or any supported relational database

## Installation

1. **Clone the repository:**
  ```bash
  git clone git@github.com:Giorgi1337/Banking-Application.git
  cd Banking-Application
  ```
2. **Configure the database:**

    Update `application.yml` with your database credentials.

## Contact

For any questions or issues, please contact [kvacabaya02@gmail.com](mailto:kvacabaya02@gmail.com) or create an issue in this repository.



