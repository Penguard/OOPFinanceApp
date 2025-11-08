package oopfinapp;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {
    private TransactionType type; // (либо INCOME либо EXPENSE)
    private String category;
    private double amount;
    private String description;
    private LocalDateTime dateTime;

    public Transaction(TransactionType type, String category, double amount, String description) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.dateTime = LocalDateTime.now();
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateTime() { return dateTime; }

    public void setCategory(String category) { this.category = category; }

    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

}
