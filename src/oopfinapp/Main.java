package oopfinapp;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService();
    private static final FinanceService financeService = new FinanceService();

    public static void main(String[] args) {
        System.out.println("===== Управление личными финансами =====");

        while (true) {
            System.out.println("\n1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("help")){
                printMainHelp();
                continue;
            }

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "0" -> {
                    System.out.println("Выход из приложения.");
                    userService.saveAllUsers();
                    return;
                }
                default -> System.out.println("Некорректный ввод. Попробуйте ещё раз.");
            }
        }
    }

    private static void handleRegister() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        if (login.isEmpty() || password.isEmpty()) {
            System.out.println("Логин и пароль не могут быть пустыми.");
            return;
        }

        try {
            userService.register(login, password);
            System.out.println("Регистрация успешна. Теперь можете войти.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        Optional<User> userOpt = userService.login(login, password);
        if (userOpt.isEmpty()) {
            System.out.println("Неверный логин или пароль.");
            return;
        }

        User user = userOpt.get();
        System.out.println("Вы успешно вошли как " + user.getLogin());

        walletMenu(user);

        FileStorage.saveUser(user);
    }
    // основное меню кошелька
    private static void walletMenu(User user) {
        while (true) {
            System.out.println("\n=== Меню кошелька пользователя: " + user.getLogin() + " ===");
            System.out.println("1. Добавить доход");
            System.out.println("2. Добавить расход");
            System.out.println("3. Управление категориями и бюджетами");
            System.out.println("4. Показать сводку (доходы/расходы/бюджеты)");
            System.out.println("5. Подсчитать расходы по нескольким категориям");
            System.out.println("6. Показать операции за период");
            System.out.println("7. Сохранить сводку в файл");
            System.out.println("8. Перевод другому пользователю");
            System.out.println("9. Экспорт операций в CSV");
            System.out.println("10.Импорт операций из CSV");
            System.out.println("0. Выйти из аккаунта");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("help")){
                printWalletHelp();
                continue;
            }

            try {
                switch (choice) {
                    case "1" -> addIncome(user);
                    case "2" -> addExpense(user);
                    case "3" -> manageCategories(user);
                    case "4" -> financeService.printSummary(user);
                    case "5" -> calcExpensesForCategories(user);
                    case "6" -> calcTransactionsForPeriod(user);
                    case "7" -> saveSummaryToFile(user);
                    case "8" -> transfer(user);
                    case "9" -> exportTransactionsCsv(user);
                    case "10" -> importTransactionsCsv(user);
                    case "0" -> {
                        System.out.println("Выход из аккаунта...");
                        return;
                    }
                    default -> System.out.println("Некорректный ввод. Попробуйте ещё раз.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void addIncome(User user) {
        System.out.print("Категория дохода: ");
        String category = scanner.nextLine().trim();
        System.out.print("Сумма дохода: ");
        double amount = readPositiveDouble();
        System.out.print("Описание (необязательно): ");
        String desc = scanner.nextLine().trim();

        financeService.addIncome(user, category, amount, desc);
    }

    private static void addExpense(User user) {
        System.out.print("Категория расхода: ");
        String category = scanner.nextLine().trim();
        System.out.print("Сумма расхода: ");
        double amount = readPositiveDouble();
        System.out.print("Описание (необязательно): ");
        String desc = scanner.nextLine().trim();

        financeService.addExpense(user, category, amount, desc);
    }

    private static void setBudget(User user) {
        System.out.print("Категория: ");
        String category = scanner.nextLine().trim();
        System.out.print("Сумма бюджета: ");
        double amount = readNonNegativeDouble();

        financeService.setBudget(user, category, amount);
    }

    private static void calcExpensesForCategories(User user) {
        System.out.print("Введите категории через запятую: ");
        String line = scanner.nextLine();
        String[] parts = Arrays.stream(line.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        financeService.printExpenseForCategories(user, Arrays.asList(parts));
    }

    private static void calcTransactionsForPeriod(User user) {
        System.out.println("\nВыберите период:");
        System.out.println("1. Сегодня");
        System.out.println("2. Последние 7 дней");
        System.out.println("3. Этот месяц");
        System.out.println("4. Этот год");
        System.out.println("5. Свой период (формат: dd.MM.yyyy - dd.MM.yyyy)");
        System.out.print("Ваш выбор: ");

        String choice = scanner.nextLine().trim();
        LocalDate now = LocalDate.now();
        LocalDate from;
        LocalDate to;

        switch (choice) {
            case "1" -> {
                from = now;
                to = now;
            }
            case "2" -> {
                // последние 7 дней включая сегодня
                to = now;
                from = now.minusDays(6);
            }
            case "3" -> {
                // текущий календарный месяц
                to = now;
                from = now.withDayOfMonth(1);
            }
            case "4" -> {
                // текущий календарный год
                to = now;
                from = now.withDayOfYear(1);
            }
            case "5" -> {
                LocalDate[] range = readCustomDateRange();
                from = range[0];
                to = range[1];
            }
            default -> {
                System.out.println("Некорректный выбор периода.");
                return;
            }
        }

        financeService.printTransactionsForPeriod(user, from, to);
    }
    private static void saveSummaryToFile(User user) {
        System.out.print("Введите имя файла (например, report.txt): ");
        String fileName = scanner.nextLine().trim();
        if (fileName.isEmpty()) {
            fileName = "report.txt";
        }
        financeService.saveSummaryToFile(user, fileName);
    }

    private static void transfer(User user) {
        System.out.print("Логин получателя: ");
        String toLogin = scanner.nextLine().trim();
        System.out.print("Категория для операции (например: 'Переводы'): ");
        String category = scanner.nextLine().trim();
        System.out.print("Сумма перевода: ");
        double amount = readPositiveDouble();

        userService.transfer(user.getLogin(), toLogin, amount, category);
        System.out.println("Перевод выполнен.");
    }
    //  > 0
    private static double readPositiveDouble() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input.replace(",", "."));
                if (value <= 0) {
                    System.out.print("Введите число > 0: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Некорректное число, попробуйте ещё раз: ");
            }
        }
    }
    //  >= 0
    private static double readNonNegativeDouble() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input.replace(",", "."));
                if (value < 0) {
                    System.out.print("Введите число >= 0: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Некорректное число, попробуйте ещё раз: ");
            }
        }
    }

    private static LocalDate[] readCustomDateRange() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        while (true) {
            System.out.print("Введите период в формате dd.MM.yyyy - dd.MM.yyyy: ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("-");
            if (parts.length != 2) {
                System.out.println("Некорректный формат. Пример: 01.01.2024 - 30.12.2025");
                continue;
            }

            String fromStr = parts[0].trim();
            String toStr = parts[1].trim();

            try {
                LocalDate from = LocalDate.parse(fromStr, formatter);
                LocalDate to = LocalDate.parse(toStr, formatter);

                if (to.isBefore(from)) {
                    System.out.println("Дата окончания не может быть раньше даты начала.");
                    continue;
                }

                return new LocalDate[]{from, to};
            } catch (DateTimeParseException e) {
                System.out.println("Не удалось разобрать даты. Проверьте формат dd.MM.yyyy.");
            }
        }
    }
    // меню управления категориями
    private static void manageCategories(User user) {
        while (true) {
            System.out.println("\n=== Управление категориями и бюджетами ===");
            System.out.println("1. Изменить/установить бюджет категории");
            System.out.println("2. Удалить категорию");
            System.out.println("3. Переименовать категорию");
            System.out.println("0. Назад");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> setBudget(user);
                    case "2" -> deleteCategoryCli(user);
                    case "3" -> renameCategoryCli(user);
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Некорректный ввод. Попробуйте ещё раз.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
    // удаление категории
    private static void deleteCategoryCli(User user) {
        System.out.print("Введите название категории для удаления: ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println("Категория не может быть пустой.");
            return;
        }

        System.out.printf("Вы уверены, что хотите удалить категорию '%s'? (y/n): ", category);
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("д")) {
            System.out.println("Удаление отменено.");
            return;
        }

        financeService.deleteCategory(user, category);
    }
    //переименование категории
    private static void renameCategoryCli(User user) {
        System.out.print("Старое название категории: ");
        String oldCat = scanner.nextLine().trim();

        System.out.print("Новое название категории: ");
        String newCat = scanner.nextLine().trim();

        if (oldCat.isEmpty() || newCat.isEmpty()) {
            System.out.println("Старое и новое имя категории не могут быть пустыми.");
            return;
        }

        financeService.renameCategory(user, oldCat, newCat);
    }

    private static void exportTransactionsCsv(User user) {
        System.out.print("Введите имя CSV-файла для экспорта (например, transactions.csv): ");
        String fileName = scanner.nextLine().trim();

        if (fileName.isEmpty()) {
            System.out.println("Имя файла не может быть пустым.");
            return;
        }

        financeService.exportTransactionsToCsv(user, fileName);
    }

    private static void importTransactionsCsv(User user) {
        System.out.print("Введите имя CSV-файла для импорта: ");
        String fileName = scanner.nextLine().trim();

        if (fileName.isEmpty()) {
            System.out.println("Имя файла не может быть пустым.");
            return;
        }

        financeService.importTransactionsFromCsv(user, fileName);
    }

    private static void printMainHelp() {
        System.out.println("\n========== Справка: главное меню ==========");
        System.out.println("1. Войти — авторизация пользователя по логину и паролю.");
        System.out.println("2. Зарегистрироваться — создает нового пользователя с пустым кошельком.");
        System.out.println("0. Выход — сохраняет данные пользователей и завершает работу программы.");
        System.out.println("В любом меню вы можете ввести 'help', чтобы увидеть подсказки по командам.\n");
    }

    private static void printWalletHelp() {
        System.out.println("\n========== Справка: меню кошелька ==========");
        System.out.println("1. Добавить доход — ввести сумму, категорию и описание дохода.");
        System.out.println("2. Добавить расход — ввести сумму, категорию и описание расхода.");
        System.out.println("3. Управление категориями и бюджетами — изменить бюджет, удалить или переименовать категорию.");
        System.out.println("4. Показать сводку — общие доходы/расходы, баланс, бюджеты с загруженностью.");
        System.out.println("5. Подсчитать расходы по нескольким категориям — укажите категории через запятую,");
        System.out.println("   чтобы увидеть расходы, бюджеты и загруженность по каждой из них.");
        System.out.println("6. Сохранить сводку в файл — текстовый отчёт по текущему состоянию кошелька.");
        System.out.println("7. Перевод другому пользователю — списание у вас и зачисление другому по логину.");
        System.out.println("8. Показать операции за период — выберите Сегодня/Неделя/Месяц/Год или свой диапазон.");
        System.out.println("9. Экспорт операций в CSV — сохранить все операции в файл .csv.");
        System.out.println("10.Импорт операций из CSV — загрузить операции из .csv файла в ваш кошелёк.");
        System.out.println("0. Выйти из аккаунта — вернуться в главное меню и сохранить ваш кошелёк.\n");
    }

}


