package oopfinapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserService {

    //  здесь храним всех пользователей
    private final Map<String, User> users = new HashMap<>();

    public UserService() {
        // при создании сервиса сразу загружаем всех пользователей с диска
        List<User> loaded = FileStorage.loadAllUsers();
        for (User user : loaded) {
            users.put(user.getLogin(), user);
        }
        System.out.println("Всего пользователей загружено в память: " + users.size());
    }

    public Optional<User> findUser(String login) {
        return Optional.ofNullable(users.get(login));
    }

    public User register(String login, String password) {
        if (users.containsKey(login)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует.");
        }
        User user = new User(login, password);
        users.put(login, user);

        // можем сразу сохранять на диск при регистрации
        FileStorage.saveUser(user);

        return user;
    }

    public Optional<User> login(String login, String password) {
        User user = users.get(login);

        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public void transfer(String fromLogin, String toLogin, double amount, String category) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть > 0");
        }

        User fromUser = users.get(fromLogin);
        User toUser = users.get(toLogin);

        if (fromUser == null || toUser == null) {
            throw new IllegalArgumentException("Отправитель или получатель не найден.");
        }

        Wallet fromWallet = fromUser.getWallet();
        Wallet toWallet = toUser.getWallet();

        if (fromWallet.getBalance() < amount) {
            throw new IllegalArgumentException("Недостаточно средств для перевода.");
        }

        fromWallet.addExpense(category, amount, "Перевод пользователю " + toLogin);
        toWallet.addIncome(category, amount, "Перевод от пользователя " + fromLogin);
    }
    // сохранить всех пользователей
    public void saveAllUsers() { for (User user : users.values()) FileStorage.saveUser(user); }
}