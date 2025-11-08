package oopfinapp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;


public class FinanceService {

    public void addIncome(User user, String category, double amount, String description) {
        Wallet w = user.getWallet();
        w.addIncome(category, amount, description);
        System.out.println("Доход добавлен. Текущий баланс: " + w.getBalance());
    }

    public void addExpense(User user, String category, double amount, String description) {
        Wallet w = user.getWallet();
        //double before = w.getExpenseForCategory(category);
        w.addExpense(category, amount, description);

        double after = w.getExpenseForCategory(category);
        double budget = w.getBudget(category);
        double totalIncome = w.getTotalIncome();
        double totalExpense = w.getTotalExpense();

        // Информация и визуализация по бюджету категории
        if (budget > 0) {
            double remaining = budget - after;
            double percent = (after / budget) * 100.0;

            String bar;
            if (percent > 100.0) {
                bar = "【ПЕРЕРАСХОД】";
            } else {
                int filledSegments = (int) (percent / 10);
                if (filledSegments > 10) filledSegments = 10;

                StringBuilder sb = new StringBuilder();
                sb.append('【');
                for (int i = 0; i < filledSegments; i++) {
                    sb.append('█'); // заполненный сегмент
                }
                for (int i = filledSegments; i < 10; i++) {
                    sb.append('░'); // пустой сегмент
                }
                sb.append('】');
                bar = sb.toString();
            }

            String alert = "";
            if (percent >= 90) {
                alert = "!!!";
            } else if (percent >= 80) {
                alert = "!!";
            } else if (percent >= 70) {
                alert = "!";
            }

            System.out.printf(
                    "Бюджет категории %s: %.2f | потрачено %.2f | осталось %.2f | %s (%.1f%%) %s%n",
                    category, budget, after, remaining, bar, percent, alert
            );
        }

        // Оповещения
        if (budget > 0 && after > budget) {
            System.out.println("ВНИМАНИЕ: Превышен лимит бюджета по категории '" + category + "'.");
            System.out.println("Лимит: " + budget + ", потрачено: " + after);
        }

        if (totalExpense > totalIncome) {
            System.out.println("ВНИМАНИЕ: Расходы превышают доходы!");
        }

        System.out.println("Расход добавлен. Текущий баланс: " + w.getBalance());
    }

    public void setBudget(User user, String category, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Бюджет не может быть отрицательным.");
        }
        user.getWallet().setBudget(category, amount);
        System.out.println("Бюджет для категории '" + category + "' установлен: " + amount);
    }

    public void printSummary(User user) {
        Wallet w = user.getWallet();

        double totalIncome = w.getTotalIncome();
        double totalExpense = w.getTotalExpense();
        System.out.println("============= Сводка по финансам ==============");
        System.out.println("Общий доход: " + totalIncome);
        System.out.println("Общие расходы: " + totalExpense);
        System.out.println("Текущий баланс: " + w.getBalance());
        System.out.println("===============================================");
        System.out.println("Доходы по категориям:");
        Map<String, Double> incomeByCat = w.getIncomeByCategory();
        for (Map.Entry<String, Double> e : incomeByCat.entrySet()) {
            System.out.println("  " + e.getKey() + ": " + e.getValue());
        }
        System.out.println("===============================================");
        System.out.println("Расходы по категориям:");
        Map<String, Double> expenseByCat = w.getExpenseByCategory();
        for (Map.Entry<String, Double> e : expenseByCat.entrySet()) {
            System.out.println("  " + e.getKey() + ": " + e.getValue());
        }
        System.out.println("===============================================");
        System.out.println("Бюджеты по категориям:");
        for (Map.Entry<String, Double> e : w.getCategoryBudgets().entrySet()) {
            String cat = e.getKey();
            double budget = e.getValue();
            double spent = w.getExpenseForCategory(cat);
            double remaining = budget - spent;

            if (budget <= 0) {
                System.out.printf(
                        "  %s: бюджет не задан (расходы: %.2f)%n",
                        cat, spent
                );
                continue;
            }
            double percent = (spent / budget) * 100.0;

            String bar;
            if (percent > 100.0) {
                // перерасход
                bar = "【ПЕРЕРАСХОД】"; //【 】
            } else {
                // полоска из 10 сегментов по 10%
                int filledSegments = (int) (percent / 10); // целые десятки процентов
                if (filledSegments > 10) {
                    filledSegments = 10;
                }
                StringBuilder sb = new StringBuilder();
                sb.append('【');
                for (int i = 0; i < filledSegments; i++) {
                    sb.append('█'); // заполненный сегмент
                }
                for (int i = filledSegments; i < 10; i++) {
                    sb.append('░'); // пустой сегмент
                }
                sb.append('】');
                bar = sb.toString();
            }

            //  чем ближе к перерасходу тем больше "!"
            String alert = "";
            if (percent >= 90) {
                alert = "!!!";
            } else if (percent >= 80) {
                alert = "!!";
            } else if (percent >= 70) {
                alert = "!";
            }

            System.out.printf(
                    "  %s: бюджет %.2f | оставшийся бюджет: %.2f | %s (%.1f%%) %s%n",
                    cat, budget, remaining, bar, percent, alert
            );
        }
        System.out.println("===============================================");
    }

    // подсчет по нескольким выбранным категориям
    public void printExpenseForCategories(User user, List<String> categories) {
        Wallet w = user.getWallet();
        Map<String, Double> expenseByCat = w.getExpenseByCategory();

        double total = 0;

        System.out.println("\nРасходы по выбранным категориям:");
        for (String cat : categories) {
            double spent = w.getExpenseForCategory(cat);
            double budget = w.getBudget(cat);

            if (spent == 0 && budget <= 0 && !expenseByCat.containsKey(cat)) {
                System.out.println(" Категория '" + cat + "' не найдена (расходов нет и бюджет не задан).");
                continue;
            }

            total += spent;

            if (budget <= 0) {
                // бюджет не задан, просто показываем расходы
                System.out.printf(
                        " %s: бюджет не задан (расходы: %.2f)%n",
                        cat, spent
                );
            } else { //  вывод прикольного бара
                double remaining = budget - spent;
                double percent = (spent / budget) * 100.0;

                String bar;
                if (percent > 100.0) {
                    // перерасход
                    bar = "【ПЕРЕРАСХОД】"; //【 】
                } else {
                    // полоска из 10 сегментов по 10%
                    int filledSegments = (int) (percent / 10); // целые десятки процентов
                    if (filledSegments > 10) {
                        filledSegments = 10;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append('【');
                    for (int i = 0; i < filledSegments; i++) {
                        sb.append('█'); // заполненный сегмент
                    }
                    for (int i = filledSegments; i < 10; i++) {
                        sb.append('░'); // пустой сегмент
                    }
                    sb.append('】');
                    bar = sb.toString();
                }

                String alert = "";
                if (percent >= 90) {
                    alert = "!!!";
                } else if (percent >= 80) {
                    alert = "!!";
                } else if (percent >= 70) {
                    alert = "!";
                }

                System.out.printf(
                        " %s: бюджет %.2f | расходы %.2f | оставшийся бюджет: %.2f | %s (%.1f%%) %s%n",
                        cat, budget, spent, remaining, bar, percent, alert);
            }
        }
        System.out.printf("Суммарные расходы по выбранным категориям: %.2f%n", total);
    }

    public void printTransactionsForPeriod(User user, LocalDate from, LocalDate to) {
        Wallet w = user.getWallet();
        double totalIncome = 0;
        double totalExpense = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<Transaction> filtered = w.getTransactions().stream()
                .filter(t -> {
                    LocalDate d = t.getDateTime().toLocalDate();
                    return (d.isEqual(from) || d.isAfter(from))
                            && (d.isEqual(to) || d.isBefore(to));
                })
                .sorted(Comparator.comparing(Transaction::getDateTime))
                .toList();

        if (filtered.isEmpty()) {
            System.out.println("За выбранный период операций не найдено.");
            return;
        }

        System.out.println("\nОперации за период " + from + " - " + to + ":");
        System.out.println("===============================================");

        for (Transaction t : filtered) {
            String sign = (t.getType() == TransactionType.INCOME) ? "+" : "-";
            String formattedDate = t.getDateTime().format(formatter);

            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }

            System.out.printf(
                    "%s | %s %s%.2f : %s%n",
                    formattedDate,
                    t.getCategory(),
                    sign,
                    t.getAmount(),
                    t.getDescription() == null || t.getDescription().isEmpty() ? "(без описания)" : t.getDescription()
            );
        }
        System.out.println("===============================================");
        System.out.printf("Общие доходы : +%.2f%n", totalIncome);
        System.out.printf("Общие расходы: -%.2f%n", totalExpense);
        System.out.printf("Баланс периода: %.2f%n", totalIncome-totalExpense);
        System.out.println("===============================================");
    }

    // вывод в файл
    public void saveSummaryToFile(User user, String fileName) {
        Wallet w = user.getWallet();
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Сводка по финансам пользователя: " + user.getLogin() + "\n");
            writer.write("Общие доходы: " + w.getTotalIncome() + "\n");
            writer.write("Общие расходы: " + w.getTotalExpense() + "\n");
            writer.write("Текущий баланс: " + w.getBalance() + "\n\n");

            writer.write("Бюджеты по категориям:\n");
            for (Map.Entry<String, Double> e : w.getCategoryBudgets().entrySet()) {
                String cat = e.getKey();
                double budget = e.getValue();
                double spent = w.getExpenseForCategory(cat);
                double remaining = budget - spent;
                writer.write("  " + cat + ": " + budget + " | оставшийся бюджет: " + remaining + "\n");
            }
            System.out.println("Сводка сохранена в файл: " + fileName);
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    // удаление категории (замена на без категории)
    public void deleteCategory(User user, String category) {
        Wallet w = user.getWallet();

        boolean hadBudget = w.getCategoryBudgets().remove(category) != null;

        int changed = 0;
        for (Transaction t : w.getTransactions()) {
            if (t.getCategory() != null && t.getCategory().equalsIgnoreCase(category)) {
                t.setCategory("Без категории");
                changed++;
            }
        }

        if (!hadBudget && changed == 0) {
            System.out.println("Категория '" + category + "' не найдена: нет бюджета и операций с такой категорией.");
            return;
        }

        System.out.printf(
                "Категория '%s' удалена. Связанные операции перенесены в категорию 'Без категории' (кол-во: %d).%n",
                category, changed);
    }

    // переименование категории (переносим бюджет и переименовываем транзакции)
    public void renameCategory(User user, String oldCategory, String newCategory) {
        if (oldCategory.equalsIgnoreCase(newCategory)) {
            throw new IllegalArgumentException("Старая и новая категории совпадают.");
        }

        if (newCategory == null || newCategory.trim().isEmpty()) {
            throw new IllegalArgumentException("Новое имя категории не может быть пустым.");
        }

        Wallet w = user.getWallet();

        // перенос бюджета если был
        Map<String, Double> budgets = w.getCategoryBudgets();
        Double oldBudget = budgets.remove(oldCategory);
        boolean hadBudget = oldBudget != null;

        if (hadBudget) {
            // если на новую категорию уже есть бюджет то перезапишем его и предупредим
            Double existing = budgets.get(newCategory);
            if (existing != null) {
                System.out.printf(
                        "ВНИМАНИЕ: У категории '%s' уже был бюджет %.2f, он будет заменён бюджетом %.2f из '%s'.%n",
                        newCategory, existing, oldBudget, oldCategory
                );
            }
            budgets.put(newCategory, oldBudget);
        }

        int changed = 0;
        for (Transaction t : w.getTransactions()) {
            if (t.getCategory() != null && t.getCategory().equalsIgnoreCase(oldCategory)) {
                t.setCategory(newCategory);
                changed++;
            }
        }

        if (!hadBudget && changed == 0) {
            System.out.printf(
                    "Категория '%s' не найдена: нет бюджета и операций с такой категорией.%n",
                    oldCategory);
            return;
        }

        System.out.printf(
                "Категория '%s' переименована в '%s'. Обновлено операций: %d.%n",
                oldCategory, newCategory, changed);
    }
    //  экспорт csv
    public void exportTransactionsToCsv(User user, String fileName) {
        Wallet w = user.getWallet();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // заголовок
            writer.println("datetime;type;category;amount;description");

            for (Transaction t : w.getTransactions()) {
                String datetime = t.getDateTime().format(formatter);
                String type = t.getType().name();
                String category = t.getCategory() == null ? "" : t.getCategory().replace(";", ",");
                String description = t.getDescription() == null ? "" : t.getDescription().replace(";", ",");

                writer.printf(
                        "%s;%s;%s;%.2f;%s%n",
                        datetime,
                        type,
                        category,
                        t.getAmount(),
                        description
                );
            }

            System.out.println("Экспорт операций в CSV завершён. Файл: " + fileName);
        } catch (IOException e) {
            System.err.println("Ошибка при экспорте в CSV: " + e.getMessage());
        }
    }
    //  импорт csv
    public void importTransactionsFromCsv(User user, String fileName) {
        Wallet w = user.getWallet();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine(); // читаем заголовок

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length < 5) {
                    System.out.println("Пропущена строка (некорректный формат): " + line);
                    skipped++;
                    continue;
                }

                String datetimeStr = parts[0].trim();
                String typeStr = parts[1].trim();
                String category = parts[2].trim();
                String amountStr = parts[3].trim();
                String description = parts[4].trim();

                try {
                    LocalDateTime dt = LocalDateTime.parse(datetimeStr, formatter);
                    TransactionType type = TransactionType.valueOf(typeStr);
                    double amount = Double.parseDouble(amountStr.replace(",", "."));

                    if (type == TransactionType.INCOME) {
                        w.addIncome(category, amount, description);
                    } else {
                        w.addExpense(category, amount, description);
                    }

                    // ставим дату из файла на только что созданную транзакцию
                    List<Transaction> txs = w.getTransactions();
                    Transaction last = txs.get(txs.size() - 1);
                    last.setDateTime(dt);

                    imported++;
                } catch (DateTimeParseException e) {
                    System.out.println("Ошибка даты во входной строке, строка пропущена: " + line);
                    skipped++;
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка типа или числа во входной строке, строка пропущена: " + line);
                    skipped++;
                }
            }

            System.out.printf(
                    "Импорт CSV завершён. Импортировано операций: %d, пропущено строк: %d%n",
                    imported, skipped
            );
        } catch (IOException e) {
            System.err.println("Ошибка при импорте CSV: " + e.getMessage());
        }
    }


}


