package oopfinapp;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileStorage {

    private static final String DATA_DIR = "data";

    static {
        try {
            Files.createDirectories(Path.of(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию data: " + e.getMessage());
        }
    }

    private static Path getUserFile(String login) {
        return Path.of(DATA_DIR, login + ".dat");
    }


    public static void saveUser(User user) {
        Path file = getUserFile(user.getLogin());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(user);
            System.out.println("Данные пользователя " + user.getLogin() + " сохранены в файл: "
                    + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении пользователя: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //  загрузить всех пользователей из папки "data"
    public static List<User> loadAllUsers() {
        List<User> result = new ArrayList<>();
        Path dir = Path.of(DATA_DIR);

        if (!Files.exists(dir)) {
            return result; // еще никто не сохранялся просто пустой список
        }

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".dat")).forEach(path -> {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                    Object obj = ois.readObject();
                    if (obj instanceof User user) {
                        result.add(user);
                        System.out.println("Загружен пользователь " + user.getLogin() +
                                        " из файла: " + path.toAbsolutePath());
                    } else {
                        System.err.println("Файл " + path.toAbsolutePath()
                                + " содержит не объект User — пропускаем.");
                    }
                } catch (IOException | ClassNotFoundException e) {
                            System.err.println("Ошибка при чтении файла " + path.toAbsolutePath() + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Ошибка при обходе директории data: " + e.getMessage());
        }

        return result;
    }

}