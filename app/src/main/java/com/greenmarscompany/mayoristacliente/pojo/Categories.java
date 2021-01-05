package com.greenmarscompany.mayoristacliente.pojo;

public class Categories {
    private int Id;
    private String Name;
    private String Url;

    public Categories(int id, String name, String url) {
        Id = id;
        Name = name;
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

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }
}
