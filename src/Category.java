/**
 * Class representing a Category for transactions.
 */
public class Category {

    // Attributes
    private String name;
    private String type;
    private double limit;

    // Constructors
    public Category(String name, String type, double limit) {
        this.name = name;
        this.type = type.toUpperCase();
        this.limit = limit;
    }

    public Category(String name, String type) {
        this(name, type, 0);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    // Methods
    /**
     * Validates the category fields.
     *
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        if (name == null || name.trim().isEmpty()) return false;
        if (type == null || (!type.equalsIgnoreCase("EXPENSE") && !type.equalsIgnoreCase("INCOME"))) return false;
        if (limit < 0) return false;
        return true;
    }

    /**
     * Formats the category for GUI display.
     */
    public String formatForGUI() {
        return name + " (" + type + ")" + (limit > 0 ? " | Limit: " + limit : "");
    }

    @Override
    public String toString() {
        return formatForGUI();
    }
}
