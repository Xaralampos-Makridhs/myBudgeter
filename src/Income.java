import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Income extends Transaction {
    // Attributes
    private boolean recurring;
    private RecurringFrequency recurringFrequency;
    private String sourceType; 
    private PaymentMethod paymentMethod;
    private double limitAlert;

    // Constructor
    public Income(double amount, Category category, String description,
                  boolean recurring, RecurringFrequency recurringFrequency,
                  String sourceType, PaymentMethod paymentMethod, double limitAlert) {
        super(amount, category, description);
        this.recurring = recurring;
        this.recurringFrequency = recurringFrequency;
        this.sourceType = sourceType;
        this.paymentMethod = paymentMethod;
        this.limitAlert = limitAlert;
    }

    // One-line Getters & Setters
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    public RecurringFrequency getRecurringFrequency() { return recurringFrequency; }
    public void setRecurringFrequency(RecurringFrequency recurringFrequency) { this.recurringFrequency = recurringFrequency; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getLimitAlert() { return limitAlert; }
    public void setLimitAlert(double limitAlert) { this.limitAlert = limitAlert; }

    // Abstract Methods Implementation
    @Override
    public String getType() { return "Income"; }

    @Override
    public double applyToBalance(double currentBalance) { return currentBalance + getAmount(); }

    // Recurring Methods
    public LocalDateTime calculateNextPaymentDate() {
        if (!recurring || recurringFrequency == null) return null;

        switch (recurringFrequency) {
            case DAILY: return getDate().plusDays(1);
            case WEEKLY: return getDate().plusWeeks(1);
            case MONTHLY: return getDate().plusMonths(1);
            case YEARLY: return getDate().plusYears(1);
            default: return null;
        }
    }

    public String getRecurringInfo() {
        if (!recurring || recurringFrequency == null) return "This transaction is not recurring";
        LocalDateTime nextDate = calculateNextPaymentDate();
        return "Recurring transaction: " + recurringFrequency
                + ", next payment: " + nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Extra Methods
    public boolean isOverLimit(double limit) { return limitAlert > limit; }

    public String formatForGUI() {
        return "Income: " + getAmount() + " | " + getCategory().getName() + " | "
                + getFormattedDate() + " | " + getRecurringInfo()
                + " | Source: " + sourceType + " | Payment: " + paymentMethod;
    }
}
