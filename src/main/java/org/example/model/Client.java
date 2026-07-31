package org.example.model;

public class Client {
    private String name;
    private String lastName;
    private String mail;
    private short age;

    public Client(String name, String lastName, short age, String mail) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " " + lastName + " <" + mail + ">";
    }
}
