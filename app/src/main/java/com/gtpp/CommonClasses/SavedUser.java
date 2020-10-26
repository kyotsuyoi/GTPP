package com.gtpp.CommonClasses;

public class SavedUser {

    private int id;
    private String user;
    private String password;
    private String session;
    private boolean administrator;
    private boolean adminVisualization;
    private static SavedUser savedUser = null;

    public static SavedUser getSavedUser() {
        if (savedUser == null) savedUser = new SavedUser();
        return savedUser;
    }

    public static void setSavedUser(SavedUser user) {
        savedUser = user;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getSession() {
        return session;
    }
    public void setSession(String session) {
        this.session = session;
    }

    public boolean isAdministrator() {return administrator;}
    public void setAdministrator(boolean administrator) {this.administrator = administrator;}

    public boolean isAdminVisualization() {return adminVisualization;}
    public void setAdminVisualization(boolean adminVisualization) {this.adminVisualization = adminVisualization;}

}

