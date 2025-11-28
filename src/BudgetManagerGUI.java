import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class BudgetManagerGUI extends JFrame {

    private BudgetManager manager;
    private DefaultTableModel transactionTableModel;
    private JTable transactionTable;
    private DefaultTableModel categoryTableModel;
    private JTable categoryTable;
    private JLabel balanceLabel, totalIncomeLabel, totalExpenseLabel;

    public BudgetManagerGUI() {
        manager = new BudgetManager(0); // αρχικό balance 0
        setTitle("Budget Manager");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Transactions Tab ---
        JPanel transactionPanel = new JPanel(new BorderLayout());
        transactionTableModel = new DefaultTableModel(new Object[]{"ID", "Type", "Amount", "Category", "Date", "Description"}, 0);
        transactionTable = new JTable(transactionTableModel);
        transactionPanel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        JPanel transactionButtons = new JPanel();
        JButton addExpenseBtn = new JButton("Add Expense");
        JButton addIncomeBtn = new JButton("Add Income");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton filterBtn = new JButton("Filter");
        transactionButtons.add(addExpenseBtn);
        transactionButtons.add(addIncomeBtn);
        transactionButtons.add(updateBtn);
        transactionButtons.add(deleteBtn);
        transactionButtons.add(filterBtn);
        transactionPanel.add(transactionButtons, BorderLayout.SOUTH);

        tabbedPane.addTab("Transactions", transactionPanel);

        // --- Categories Tab ---
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryTableModel = new DefaultTableModel(new Object[]{"Name", "Type", "Limit"}, 0);
        categoryTable = new JTable(categoryTableModel);
        categoryPanel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        JPanel categoryButtons = new JPanel();
        JButton addCategoryBtn = new JButton("Add Category");
        JButton updateCategoryBtn = new JButton("Update Category");
        JButton deleteCategoryBtn = new JButton("Delete Category");
        categoryButtons.add(addCategoryBtn);
        categoryButtons.add(updateCategoryBtn);
        categoryButtons.add(deleteCategoryBtn);
        categoryPanel.add(categoryButtons, BorderLayout.SOUTH);

        tabbedPane.addTab("Categories", categoryPanel);

        // --- Statistics Tab ---
        JPanel statsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        balanceLabel = new JLabel("Current Balance: 0.0");
        totalIncomeLabel = new JLabel("Total Income: 0.0");
        totalExpenseLabel = new JLabel("Total Expense: 0.0");
        JButton refreshStatsBtn = new JButton("Refresh Stats");
        statsPanel.add(balanceLabel);
        statsPanel.add(totalIncomeLabel);
        statsPanel.add(totalExpenseLabel);
        statsPanel.add(refreshStatsBtn);

        tabbedPane.addTab("Statistics", statsPanel);

        // --- File Tab ---
        JPanel filePanel = new JPanel();
        JButton saveBtn = new JButton("Save Transactions to File");
        filePanel.add(saveBtn);
        tabbedPane.addTab("File", filePanel);

        add(tabbedPane);

        // --- Action Listeners ---

        // Add Expense
        addExpenseBtn.addActionListener(e -> {
            AddTransactionDialog dialog = new AddTransactionDialog(this, "Expense");
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                manager.addTransaction(dialog.getExpense());
                refreshTransactionTable();
                refreshStats();
            }
        });

        // Add Income
        addIncomeBtn.addActionListener(e -> {
            AddTransactionDialog dialog = new AddTransactionDialog(this, "Income");
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                manager.addTransaction(dialog.getIncome());
                refreshTransactionTable();
                refreshStats();
            }
        });

        // Delete Transaction
        deleteBtn.addActionListener(e -> {
            int row = transactionTable.getSelectedRow();
            if (row != -1) {
                UUID id = UUID.fromString(transactionTableModel.getValueAt(row, 0).toString());
                Transaction t = manager.getTransactions().stream().filter(tr -> tr.getTransactionId().equals(id)).findFirst().orElse(null);
                if (t != null) {
                    manager.removeTransaction(t);
                    refreshTransactionTable();
                    refreshStats();
                }
            }
        });

        // Update Transaction
        updateBtn.addActionListener(e -> {
            int row = transactionTable.getSelectedRow();
            if (row != -1) {
                UUID id = UUID.fromString(transactionTableModel.getValueAt(row, 0).toString());
                Transaction old = manager.getTransactions().stream().filter(tr -> tr.getTransactionId().equals(id)).findFirst().orElse(null);
                if (old != null) {
                    AddTransactionDialog dialog = new AddTransactionDialog(this, old instanceof Expense ? "Expense" : "Income", old);
                    dialog.setVisible(true);
                    if (dialog.isSaved()) {
                        if (old instanceof Expense) manager.updateTransaction(id, dialog.getExpense());
                        else manager.updateTransaction(id, dialog.getIncome());
                        refreshTransactionTable();
                        refreshStats();
                    }
                }
            }
        });

        // Filter Transactions
        filterBtn.addActionListener(e -> {
            String type = JOptionPane.showInputDialog(this, "Enter type to filter (Expense/Income) or leave blank:");
            List<Transaction> filtered = manager.getTransactions();
            if (type != null && !type.isEmpty()) {
                filtered = filtered.stream().filter(t -> t.getType().equalsIgnoreCase(type)).toList();
            }
            refreshTransactionTable(filtered);
        });

        // Add Category
        addCategoryBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Category Name:");
            String type = JOptionPane.showInputDialog(this, "Category Type (Income/Expense):");
            double limit = Double.parseDouble(JOptionPane.showInputDialog(this, "Limit (0 if none):"));
            Category c = new Category(name, type, limit);
            manager.addCategory(c);
            refreshCategoryTable();
        });

        // Update Category
        updateCategoryBtn.addActionListener(e -> {
            int row = categoryTable.getSelectedRow();
            if (row != -1) {
                String oldName = categoryTableModel.getValueAt(row, 0).toString();
                String newName = JOptionPane.showInputDialog(this, "New Name:", oldName);
                String type = JOptionPane.showInputDialog(this, "New Type (Income/Expense):", categoryTableModel.getValueAt(row, 1));
                double limit = Double.parseDouble(JOptionPane.showInputDialog(this, "New Limit:", categoryTableModel.getValueAt(row, 2)));
                Category newCat = new Category(newName, type, limit);
                manager.updateCategory(oldName, newCat);
                refreshCategoryTable();
            }
        });

        // Delete Category
        deleteCategoryBtn.addActionListener(e -> {
            int row = categoryTable.getSelectedRow();
            if (row != -1) {
                String name = categoryTableModel.getValueAt(row, 0).toString();
                Category c = manager.getCategories().stream().filter(cat -> cat.getName().equals(name)).findFirst().orElse(null);
                if (c != null) {
                    manager.removeCategory(c);
                    refreshCategoryTable();
                }
            }
        });

        // Refresh Stats
        refreshStatsBtn.addActionListener(e -> refreshStats());

        // Save to file
        saveBtn.addActionListener(e -> {
            String filename = JOptionPane.showInputDialog(this, "Enter filename to save:");
            if (filename != null && !filename.isEmpty()) {
                JOptionPane.showMessageDialog(this, manager.saveTransactionsToFile(filename));
            }
        });
    }

    private void refreshTransactionTable() {
        refreshTransactionTable(manager.getTransactions());
    }

    private void refreshTransactionTable(List<Transaction> list) {
        transactionTableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Transaction t : list) {
            transactionTableModel.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getType(),
                    t.getAmount(),
                    t.getCategory().getName(),
                    t.getFormattedDate(),
                    t.getDescription()
            });
        }
    }

    private void refreshCategoryTable() {
        categoryTableModel.setRowCount(0);
        for (Category c : manager.getCategories()) {
            categoryTableModel.addRow(new Object[]{c.getName(), c.getType(), c.getLimit()});
        }
    }

    private void refreshStats() {
        balanceLabel.setText("Current Balance: " + manager.getCurrentBalance());
        totalIncomeLabel.setText("Total Income: " + manager.getTotalIncome());
        totalExpenseLabel.setText("Total Expense: " + manager.getTotalExpense());
    }

    // Dialog for adding/updating transactions
    private class AddTransactionDialog extends JDialog {
        private boolean saved = false;
        private JTextField amountField, descriptionField, categoryField, sourceField;
        private JCheckBox recurringBox;
        private JComboBox<RecurringFrequency> freqBox;
        private JComboBox<PaymentMethod> pmBox;
        private String type;
        private Expense expense;
        private Income income;

        public AddTransactionDialog(JFrame parent, String type) {
            this(parent, type, null);
        }

        public AddTransactionDialog(JFrame parent, String type, Transaction old) {
            super(parent, "Add/Update " + type, true);
            this.type = type;
            setSize(400, 400);
            setLayout(new GridLayout(0, 2, 5, 5));
            setLocationRelativeTo(parent);

            add(new JLabel("Amount:"));
            amountField = new JTextField(old != null ? String.valueOf(old.getAmount()) : "");
            add(amountField);

            add(new JLabel("Description:"));
            descriptionField = new JTextField(old != null ? old.getDescription() : "");
            add(descriptionField);

            add(new JLabel("Category:"));
            categoryField = new JTextField(old != null ? old.getCategory().getName() : "");
            add(categoryField);

            add(new JLabel("Recurring:"));
            recurringBox = new JCheckBox();
            if (old instanceof Expense e) recurringBox.setSelected(e.isRecurring());
            if (old instanceof Income i) recurringBox.setSelected(i.isRecurring());
            add(recurringBox);

            add(new JLabel("Frequency:"));
            freqBox = new JComboBox<>(RecurringFrequency.values());
            if (old instanceof Expense e && e.getRecurringFrequency() != null)
                freqBox.setSelectedItem(e.getRecurringFrequency());
            if (old instanceof Income i && i.getRecurringFrequency() != null)
                freqBox.setSelectedItem(i.getRecurringFrequency());
            add(freqBox);

            add(new JLabel("Payment Method:"));
            pmBox = new JComboBox<>(PaymentMethod.values());
            if (old instanceof Expense e) pmBox.setSelectedItem(e.getPaymentMethod());
            if (old instanceof Income i) pmBox.setSelectedItem(i.getPaymentMethod());
            add(pmBox);

            if (type.equals("Income")) {
                add(new JLabel("Source:"));
                sourceField = new JTextField(old instanceof Income i ? i.getSourceType() : "");
                add(sourceField);
            } else {
                add(new JLabel(""));
                add(new JLabel(""));
            }

            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            add(saveBtn);
            add(cancelBtn);

            saveBtn.addActionListener(e -> {
                double amount = Double.parseDouble(amountField.getText());
                String desc = descriptionField.getText();
                String catName = categoryField.getText();
                boolean recurring = recurringBox.isSelected();
                RecurringFrequency freq = (RecurringFrequency) freqBox.getSelectedItem();
                PaymentMethod pm = (PaymentMethod) pmBox.getSelectedItem();
                Category cat = new Category(catName, type.equals("Income") ? "INCOME" : "EXPENSE");

                if (type.equals("Expense")) {
                    expense = new Expense(amount, cat, desc, recurring, freq, 0, pm);
                } else {
                    String source = sourceField.getText();
                    income = new Income(amount, cat, desc, recurring, freq, source, pm, 0);
                }
                saved = true;
                dispose();
            });

            cancelBtn.addActionListener(e -> dispose());
        }

        public boolean isSaved() {
            return saved;
        }

        public Expense getExpense() {
            return expense;
        }

        public Income getIncome() {
            return income;
        }
    }
}
