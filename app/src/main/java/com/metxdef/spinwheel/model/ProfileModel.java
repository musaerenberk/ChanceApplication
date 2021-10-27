package com.metxdef.spinwheel.model;

public class ProfileModel {

    private String name, email, image;
    private int coins, spins;

    public ProfileModel(int coins, String email,String image, String name, int spins) {
        this.coins = coins;
        this.email = email;
        this.image = image;
        this.name = name;
        this.spins = spins;
    }

    public ProfileModel() {
        name = "Unknown";
        email = "test@gmail.com";
        image = " ";
        coins = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getSpins() {
        return spins;
    }

    public void setSpins(int spins) {
        this.spins = spins;
    }
}
