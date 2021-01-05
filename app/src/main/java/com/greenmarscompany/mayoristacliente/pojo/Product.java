package com.greenmarscompany.mayoristacliente.pojo;

public class Product {

    private int Id;
    private String Name;
    private String Description;
    private float Price;
    private float UnitMeasurement;
    private int Size;
    private String Url;
    private String type;
    private String marke;
    private int markeId;

    public int getMarkeId() {
        return markeId;
    }

    public void setMarkeId(int markeId) {
        this.markeId = markeId;
    }
    public Product(int id, String name, String description, float price, float unitMeasurement, int size, String url, String type, String marke
                  ) {
        Id = id;
        Name = name;
        Description = description;
        Price = price;
        UnitMeasurement = unitMeasurement;
        Size = size;
        Url = url;

        this.type = type;
        this.marke = marke;
    }
    public Product(int id, String name, String description, float price, float unitMeasurement, int size, String url, String type, String marke,
                   int markeId) {
        Id = id;
        Name = name;
        Description = description;
        Price = price;
        UnitMeasurement = unitMeasurement;
        Size = size;
        Url = url;

        this.type = type;
        this.marke = marke;
        this.markeId=markeId;
    }

    public String getType() {
        return type;
    }

    public String getMarke() {
        return marke;
    }

    public void setMarke(String marke) {
        this.marke = marke;
    }

    public void setType(String type) {
        this.type = type;
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

    public float getPrice() {
        return Price;
    }

    public void setPrice(float price) {
        Price = price;
    }

    public float getUnitMeasurement() {
        return UnitMeasurement;
    }

    public void setUnitMeasurement(float unitMeasurement) {
        UnitMeasurement = unitMeasurement;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }
}
