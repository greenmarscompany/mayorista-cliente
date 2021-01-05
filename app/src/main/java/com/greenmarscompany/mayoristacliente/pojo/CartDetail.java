package com.greenmarscompany.mayoristacliente.pojo;

public class CartDetail {


    private int Id;
    private String Name;

    private float Price;
    private int  cantidad;

    public CartDetail(int id, String name, float price, int cantidad) {
        Id = id;
        Name = name;
        Price = price;
        this.cantidad = cantidad;
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

    public float getPrice() {
        return Price;
    }

    public void setPrice(float price) {
        Price = price;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
