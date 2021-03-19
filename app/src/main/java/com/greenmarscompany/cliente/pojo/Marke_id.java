package com.greenmarscompany.cliente.pojo;

public class Marke_id {
    private int id;
    private String name;
    private String image;
    private int category_id;


    // Getter Methods

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public float getCategory_id() {
        return category_id;
    }

    // Setter Methods

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }
}
