package oopfinapp;

import java.io.Serializable;

public class User implements Serializable {
    private String login;
    private String password;
    private Wallet wallet = new Wallet();

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}