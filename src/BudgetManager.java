import java.util.*;
import java.io.*;
import java.time.LocalDateTime;

/**
 * Class to manage transactions, categories, and balance.
 */
public class BudgetManager {

    // Attributes
    private List<Transaction> transactions;
    private List<Category> categories;
    private double balance;

    // Constructor
    public BudgetManager(double balance) {
        this.transactions = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.balance = balance;
    }

    // Getters & Setters
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // Transaction Management
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) return false;
        transactions.add(transaction);
        return true;
    }

    public boolean removeTransaction(Transaction transaction) {
        if (transaction == null) return false;
        return transactions.remove(transaction);
    }

    public boolean updateTransaction(UUID transactionId, Transaction transaction) {
        if (transactionId == null || transaction == null) return false;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionId().equals(transactionId)) {
                transactions.set(i, transaction);
                return true;
            }
        }
        return false;
    }

    // Category Management
    public boolean addCategory(Category c) {
        if (c == null) return false;
        return categories.add(c);
    }

    public boolean removeCategory(Category c) {
        if (c == null) return false;
        return categories.remove(c);
    }

    public boolean updateCategory(String categoryName, Category newCategory) {
        if (categoryName == null || categoryName.isEmpty() || newCategory == null) return false;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equalsIgnoreCase(categoryName)) {
                categories.set(i, newCategory);
                return true;
            }
        }
        return false;
    }

    // Transaction Filters
    public Transaction getTransactionByType(String type) {
        if (type == null || type.isEmpty()) return null;
        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase(type)) return t;
        }
        return null;
    }

    public List<Category> getTransactionByCategory(String categoryName) {
        List<Category> list = new ArrayList<>();
        if (categoryName == null || categoryName.isEmpty()) return list;
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(categoryName)) list.add(c);
        }
        return list;
    }

    public List<Transaction> getTransactionByDate(LocalDateTime start, LocalDateTime end) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactions) {
            LocalDateTime date = t.getDate();
            if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                list.add(t);
            }
        }
        return list;
    }

    public List<Transaction> getTransactionByRecurring(boolean isRecurring) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof Expense e && e.isRecurring() == isRecurring) list.add(e);
            if (t instanceof Income i && i.isRecurring() == isRecurring) list.add(i);
        }
        return list;
    }

    public List<Transaction> getTransactionByPaymentMethod(PaymentMethod pm) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof Expense e && e.getPaymentMethod() == pm) list.add(e);
        }
        return list;
    }

    // Statistics
    public double getTotalExpense() {
        double total = 0;
        for (Transaction t : transactions) if (t instanceof Expense e) total += e.getAmount();
        return total;
    }

    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : transactions) if (t instanceof Income i) total += i.getAmount();
        return total;
    }

    public double getCurrentBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public List<Expense> getOverLimitExpense() {
        List<Expense> list = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof Expense e && e.isOverLimit(e.getLimitAlert())) list.add(e);
        }
        return list;
    }

    public List<Transaction> getRecurringPayments(PaymentMethod method) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof Expense e && e.getPaymentMethod() == method) list.add(e);
        }
        return list;
    }

    // Save transactions to a text file
    /**
     * Saves all transactions to a text file.
     *
     * @param filename name of the file
     * @return status message
     */
    public String saveTransactionsToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Transaction t : transactions) {
                writer.write("TYPE=" + t.getType());
                writer.newLine();
                writer.write("AMOUNT=" + t.getAmount());
                writer.newLine();
                writer.write("CATEGORY=" + t.getCategory().getName());
                writer.newLine();
                writer.write("DESCRIPTION=" + t.getDescription());
                writer.newLine();
                writer.write("DATE=" + t.getDate().toString());
                writer.newLine();

                //Expense
                if (t instanceof Expense e) {
                    writer.write("PAYMENT_METHOD=" + e.getPaymentMethod());
                    writer.newLine();
                    writer.write("IS_RECURRING=" + e.isRecurring());
                    writer.newLine();
                    writer.write("RECURRING_FREQUENCY=" + e.getRecurringFrequency());
                    writer.newLine();
                    writer.write("LIMIT_ALERT=" + e.getLimitAlert());
                    writer.newLine();
                }

                //Income
                if (t instanceof Income i) {
                    writer.write("IS_RECURRING=" + i.isRecurring());
                    writer.newLine();
                    writer.write("RECURRING_FREQUENCY=" + i.getRecurringFrequency());
                    writer.newLine();
                }

                writer.write("----");
                writer.newLine();
            }
            return "Data saved successfully";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
