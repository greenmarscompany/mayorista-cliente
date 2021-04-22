package com.greenmarscompany.cliente.persistence.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "acount")
public class Acount {

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "num_documento")
    private String numDocumento;

    @ColumnInfo(name = "nombre")
    private String nombre;

    @ColumnInfo(name = "phone1")
    private String phoneOne;

    @ColumnInfo(name = "phone2")
    private String phoneTwo;

    @ColumnInfo(name = "direccion")
    private String direccion;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "token")
    private String token;

    @ColumnInfo(name = "latitud")
    private double latitud;
    @ColumnInfo(name = "longitud")
    private double longitud;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumDocumento() {
        return numDocumento;
    }

    public void setNumDocumento(String numDocumento) {
        this.numDocumento = numDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPhoneOne() {
        return phoneOne;
    }

    public void setPhoneOne(String phoneOne) {
        this.phoneOne = phoneOne;
    }

    public String getPhoneTwo() {
        return phoneTwo;
    }

    public void setPhoneTwo(String phoneTwo) {
        this.phoneTwo = phoneTwo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    @Override
    public String toString() {
        return "Acount{" +
                "id=" + id +
                ", numDocumento='" + numDocumento + '\'' +
                ", nombre='" + nombre + '\'' +
                ", phoneOne='" + phoneOne + '\'' +
                ", phoneTwo='" + phoneTwo + '\'' +
                ", direccion='" + direccion + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", token='" + token + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                '}';
    }
}