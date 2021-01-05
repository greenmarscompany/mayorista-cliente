package com.greenmarscompany.mayoristacliente.pojo;

public class Brands {
    private int Id;
    private String Name;
    private String Description;
    private String Url;

    public Brands(int id, String name, String description, String url) {
        Id = id;
        Name = name;
        Description = description;
        Url = url;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }
}
