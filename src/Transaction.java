import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Abstract class representing a financial transaction.
 */
public abstract class Transaction {

    // Attributes
    private UUID transactionId;
    private LocalDateTime date;
    private double amount;
    private Category category;
    private String description;

    // Constructor
    public Transaction(double amount, Category category, String description) {
        this.transactionId = UUID.randomUUID();
        this.date = LocalDateTime.now();
        this.amount = amount;
        this.category = category;
        this.description = description;
    }

    // Getters and Setters
    public UUID getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Abstract Methods
    public abstract String getType();

    public abstract double applyToBalance(double currentBalance);

    // Methods
    /**
     * Returns the formatted date of the transaction.
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return date.format(formatter);
    }

    /**
     * Validates the transaction fields and returns a list of errors.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (transactionId == null) errors.add("Transaction ID cannot be null.");
        if (date == null) errors.add("Date cannot be null.");
        if (amount < 0) errors.add("Amount cannot be negative.");
        if (category == null) errors.add("Category cannot be null.");
        if (description == null || description.trim().isEmpty()) errors.add("Description cannot be empty.");

        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction transaction = (Transaction) o;
        return Objects.equals(transactionId, transaction.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}
