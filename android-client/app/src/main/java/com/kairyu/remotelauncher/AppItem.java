package com.kairyu.remotelauncher;

public class AppItem {
    public String name;
    public String path;
    public boolean isCustom;

    public AppItem(String name, String path, boolean isCustom) {
        this.name = name;
        this.path = path;
        this.isCustom = isCustom;
    }
}
