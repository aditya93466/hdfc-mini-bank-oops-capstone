package com.hdfc.minibank;

import com.hdfc.minibank.domain.Customer;
import com.hdfc.minibank.domain.Transaction;
import com.hdfc.minibank.domain.accounts.Account;
import com.hdfc.minibank.domain.accounts.CurrentAccount;
import com.hdfc.minibank.domain.accounts.SavingsAccount;
import com.hdfc.minibank.domain.enums.AccountType;
import com.hdfc.minibank.domain.enums.TransactionType;
import com.hdfc.minibank.exceptions.InvalidAccountException;
import com.hdfc.minibank.exceptions.InsufficientBalanceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, Account> accounts = new HashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private int customerCounter = 1000;
    private int accountCounter = 1000;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$"); // Indian mobile format
    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^CUST\\d{4}$");

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("=== HDFC Mini Bank - Java Training Project ===");
        System.out.println("Welcome to the Banking Application!");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": registerCustomer(); break;
                case "2": createAccount(); break;
                case "3": performTransactions(); break;
                case "4": viewAccountDetails(); break;
                case "5": viewTransactionHistory(); break;
                case "6": running = false; break;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
        shutdown();
        System.out.println("Thank you for using HDFC Mini Bank!");
    }

    private void printMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Register New Customer");
        System.out.println("2. Create Account");
        System.out.println("3. Perform Transactions");
        System.out.println("4. View Account Details");
        System.out.println("5. View Transaction History");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private void registerCustomer() {
        try {
            System.out.print("Enter Customer ID (format CUST####): ");
            String id = scanner.nextLine().trim().toUpperCase();
            if (!CUSTOMER_ID_PATTERN.matcher(id).matches()) {
                System.out.println("Invalid Customer ID format.");
                return;
            }
            if (customers.containsKey(id)) {
                System.out.println("Customer ID already exists.");
                return;
            }

            System.out.print("Enter Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("Invalid email format.");
                return;
            }

            System.out.print("Enter Phone (10 digits, starts with 6-9): ");
            String phone = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                System.out.println("Invalid phone format.");
                return;
            }

            System.out.print("Enter Date of Birth (yyyy-MM-dd): ");
            String dobStr = scanner.nextLine().trim();
            LocalDate dob = LocalDate.parse(dobStr);

            Customer c = new Customer(id, name, email, phone, dob);
            customers.put(id, c);
            System.out.println("Customer registered successfully: " + c);
        } catch (Exception e) {
            System.out.println("Error registering customer: " + e.getMessage());
        }
    }

    private void createAccount() {
        try {
            System.out.print("Enter Customer ID: ");
            String custId = scanner.nextLine().trim().toUpperCase();
            if (!customers.containsKey(custId)) {
                System.out.println("Customer not found.");
                return;
            }

            System.out.print("Select Account Type (1=SAVINGS, 2=CURRENT): ");
            String t = scanner.nextLine().trim();
            AccountType type = "1".equals(t) ? AccountType.SAVINGS : AccountType.CURRENT;

            System.out.print("Enter Initial Deposit Amount: ");
            BigDecimal initial = new BigDecimal(scanner.nextLine().trim());

            String accountNo = generateAccountNo();
            Account account = (type == AccountType.SAVINGS)
                    ? new SavingsAccount(accountNo, custId, initial)
                    : new CurrentAccount(accountNo, custId, initial);

            accounts.put(accountNo, account);
            transactions.add(new Transaction(UUID.randomUUID().toString(), accountNo, TransactionType.DEPOSIT, initial, LocalDateTime.now(), "Initial deposit"));
            System.out.println(type + " account created. Account No: " + accountNo);
        } catch (Exception e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    private void performTransactions() {
        System.out.println("\n1. Deposit\n2. Withdraw\n3. Transfer");
        System.out.print("Choose: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1": depositFlow(); break;
            case "2": withdrawFlow(); break;
            case "3": transferFlow(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private void depositFlow() {
        try {
            Account acc = askAccount("Enter Account No for Deposit: ");
            System.out.print("Amount: ");
            BigDecimal amt = new BigDecimal(scanner.nextLine().trim());
            acc.deposit(amt);
            transactions.add(new Transaction(UUID.randomUUID().toString(), acc.getAccountNo(), TransactionType.DEPOSIT, amt, LocalDateTime.now(), "Deposit"));
            System.out.println("✓ Deposit successful. New Balance: " + acc.getBalance());
        } catch (Exception e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private void withdrawFlow() {
        try {
            Account acc = askAccount("Enter Account No for Withdrawal: ");
            System.out.print("Amount: ");
            BigDecimal amt = new BigDecimal(scanner.nextLine().trim());
            acc.withdraw(amt);
            transactions.add(new Transaction(UUID.randomUUID().toString(), acc.getAccountNo(), TransactionType.WITHDRAWAL, amt, LocalDateTime.now(), "Withdrawal"));
            System.out.println("✓ Withdrawal successful. New Balance: " + acc.getBalance());
        } catch (Exception e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private void transferFlow() {
        try {
            Account from = askAccount("From Account No: ");
            Account to = askAccount("To Account No: ");
            System.out.print("Amount: ");
            BigDecimal amt = new BigDecimal(scanner.nextLine().trim());
            transfer(from, to, amt);
            System.out.println("✓ Transfer successful.");
        } catch (Exception e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void transfer(Account from, Account to, BigDecimal amount) throws Exception {
        Account first = from.getAccountNo().compareTo(to.getAccountNo()) < 0 ? from : to;
        Account second = (first == from) ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                to.deposit(amount);
                LocalDateTime now = LocalDateTime.now();
                transactions.add(new Transaction(UUID.randomUUID().toString(), from.getAccountNo(), TransactionType.TRANSFER_OUT, amount, now, "Transfer to " + to.getAccountNo()));
                transactions.add(new Transaction(UUID.randomUUID().toString(), to.getAccountNo(), TransactionType.TRANSFER_IN, amount, now, "Transfer from " + from.getAccountNo()));
            }
        }
    }

    private void viewAccountDetails() {
        try {
            Account acc = askAccount("Enter Account No: ");
            System.out.println("Account: " + acc.getAccountNo());
            System.out.println("Type  : " + (acc instanceof SavingsAccount ? "Savings" : "Current"));
            System.out.println("Owner : " + acc.getCustomerId());
            System.out.println("Opened: " + acc.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("Balance: " + acc.getBalance());
            System.out.println("Min Balance: " + acc.getMinimumBalance());
            System.out.println("Interest Rate: " + acc.getInterestRate() + "%");
            System.out.println("Annual Interest (as of now): " + acc.calculateInterest());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewTransactionHistory() {
        try {
            System.out.print("Enter Account No (or press Enter for ALL): ");
            String accNo = scanner.nextLine().trim();
            List<Transaction> list = transactions.stream()
                    .filter(t -> accNo.isEmpty() || t.getAccountNo().equalsIgnoreCase(accNo))
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .collect(Collectors.toList());

            if (list.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }
            System.out.println("=== Transactions ===");
            list.forEach(t -> System.out.println("  " + t.getType() + " - " + t.getAmount() + " at " + t.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + (t.getNotes() == null ? "" : " | " + t.getNotes())));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }




    private Account askAccount(String prompt) throws InvalidAccountException {
        System.out.print(prompt);
        String accNo = scanner.nextLine().trim().toUpperCase();
        Account acc = accounts.get(accNo);
        if (acc == null) throw new InvalidAccountException("Account not found: " + accNo);
        return acc;
    }

    private String generateAccountNo() {
        return "ACC" + (++accountCounter);
    }

    private String autoCreateCustomerIfNeeded() {
        String id = "CUST" + (++customerCounter);
        customers.putIfAbsent(id, new Customer(id, "Concurrency User", "user"+customerCounter+"@example.com", "9" + (int)(Math.random()*1000000000L), LocalDate.of(1990,1,1)));
        return id;
    }

    private Account autoCreateAccountIfNeeded(String custId, AccountType type, BigDecimal initial) {
        Optional<Account> maybe = accounts.values().stream()
                .filter(a -> a.getCustomerId().equals(custId) && ((type == AccountType.SAVINGS && a instanceof SavingsAccount) || (type == AccountType.CURRENT && a instanceof CurrentAccount)))
                .findFirst();
        if (maybe.isPresent()) return maybe.get();
        Account a = (type == AccountType.SAVINGS)
                ? new SavingsAccount(generateAccountNo(), custId, initial)
                : new CurrentAccount(generateAccountNo(), custId, initial);
        accounts.put(a.getAccountNo(), a);
        transactions.add(new Transaction(UUID.randomUUID().toString(), a.getAccountNo(), TransactionType.DEPOSIT, initial, LocalDateTime.now(), "Initial"));
        return a;
    }

    private void shutdown() {
        executorService.shutdown();
    }
}