package com.greenmarscompany.mayoristacliente.pojo;

import com.google.android.gms.maps.model.LatLng;

public class Order {

    private int id;
    private String date;
    private String status;
    private String phone;
    private String companyName;
    private LatLng companyDirection;
    private LatLng clientDirection;
    private java.util.List<String> detalles;
    private float calification;
    private String time;
    private java.util.List<String> listPrecioUnitario;
    private java.util.List<String> listSubTotal;
    private double totalFinal;

    public java.util.List<String> getListPrecioUnitario() {
        return listPrecioUnitario;
    }

    public void setListPrecioUnitario(java.util.List<String> listPrecioUnitario) {
        this.listPrecioUnitario = listPrecioUnitario;
    }

    public java.util.List<String> getListSubTotal() {
        return listSubTotal;
    }

    public void setListSubTotal(java.util.List<String> listSubTotal) {
        this.listSubTotal = listSubTotal;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getCalification() {
        return calification;
    }

    public void setCalification(float calification) {
        this.calification = calification;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.List<String> getDetalles() {
        return detalles;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LatLng getCompanyDirection() {
        return companyDirection;
    }

    public void setCompanyDirection(LatLng companyDirection) {
        this.companyDirection = companyDirection;
    }

    public LatLng getClientDirection() {
        return clientDirection;
    }

    public void setClientDirection(LatLng clientDirection) {
        this.clientDirection = clientDirection;
    }

    public void setDetalles(java.util.List<String> detalles) {
        this.detalles = detalles;
    }

    public double getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(double totalFinal) {
        this.totalFinal = totalFinal;
    }
}
