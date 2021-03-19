package com.greenmarscompany.cliente.pojo;
import java.util.Date;

public class ProductStaff {
    private int id;
    private Date date;
    private Float price;
    private String status;
    private company_id company_id;
    private product_id product_id;

    public int getID() { return id; }
    public void setID(int value) { this.id = value; }
    public Date getDate() { return date; }
    public void setDate(Date value) { this.date = value; }

    public Float getPrice() { return price; }
    public void setPrice(Float value) { this.price = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }

    public company_id getCompanyID() { return company_id; }
    public void setCompanyID(com.greenmarscompany.cliente.pojo.company_id value) { this.company_id = value; }

    public product_id getProductID() { return product_id; }
    public void setProductID(com.greenmarscompany.cliente.pojo.product_id value) { this.product_id = value; }
}
