package com.greenmarscompany.cliente.pojo;

public class company_id {
    private String company_id;
    private String ruc;
    private String name;
    private String phone;
    private String address;
    private double latitude;
    private double longitude;
    private String image;

    public String getCompanyID() { return company_id; }
    public void setCompanyID(String value) { this.company_id = value; }

    public String getRuc() { return ruc; }
    public void setRuc(String value) { this.ruc = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }

    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double value) { this.latitude = value; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double value) { this.longitude = value; }

    public String getImage() { return image; }
    public void setImage(String value) { this.image = value; }
}
