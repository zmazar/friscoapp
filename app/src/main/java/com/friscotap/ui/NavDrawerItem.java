package com.friscotap.ui;

public class NavDrawerItem {
    private String title;
    private int counter;
    private boolean submenu;
    private boolean enabled;

    public NavDrawerItem () {
        // Empty constructor
    }

    public NavDrawerItem(String menuTitle, boolean isEnabled, boolean isSubmenu) {
        this.title = menuTitle;
        this.enabled = isEnabled;
        this.submenu = isSubmenu;
    }

    public String getTitle() {
        return this.title;
    }

    public int getCounter() {
        return this.counter;
    }

    public boolean getSubmenu() {
        return this.submenu;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public void setCounter(int i) {
        this.counter = i;
    }

    public void setSubmenu(boolean isSubmenu) {
        this.submenu = isSubmenu;
    }

    public void setEnabled(boolean isEnabled) {
        this.enabled = isEnabled;
    }
}
