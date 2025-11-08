package oopfinapp;

import java.io.Serializable;
import java.util.*;

public class Wallet implements Serializable {

    private double balance;
    private List<Transaction> transactions = new ArrayList<>();

    // бюджеты по категориям: категория -> лимит
    private Map<String, Double> categoryBudgets = new HashMap<>();

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Map<String, Double> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setBudget(String category, double amount) { categoryBudgets.put(category, amount); }

    public double getBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }

    public void addIncome(String category, double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма дохода должна быть > 0");
        }
        Transaction t = new Transaction(TransactionType.INCOME, category, amount, description);
        transactions.add(t);
        balance += amount;
    }

    public void addExpense(String category, double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма расхода должна быть > 0");
        }
        Transaction t = new Transaction(TransactionType.EXPENSE, category, amount, description);
        transactions.add(t);
        balance -= amount;
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<String, Double> getIncomeByCategory() {
        Map<String, Double> result = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                result.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return result;
    }

    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> result = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                result.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return result;
    }

    public double getExpenseForCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getRemainingBudgetForCategory(String category) {
        double budget = getBudget(category);
        double spent = getExpenseForCategory(category);
        return budget - spent;
    }
}
