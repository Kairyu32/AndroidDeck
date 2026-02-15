package com.kairyu.remotelauncher;

public class SavedComputer {
    public String name;
    public String ip;

    public SavedComputer(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    // Using toString for the dialog list display
    @Override
    public String toString() {
        return name + " (" + ip + ")";
    }
}
