package com.greenmarscompany.mayoristacliente.pojo;

public class product_id {
    private int id;
    private String description;
    private long measurement;
    private double unitPrice;
    private String image;
    private long categoryID;
    private long markeID;
    private long detailMeasurementID;
    private long unitMeasurementID;

    public long getID() { return id; }
    public void setID(int value) { this.id = value; }

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public long getMeasurement() { return measurement; }
    public void setMeasurement(long value) { this.measurement = value; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double value) { this.unitPrice = value; }

    public String getImage() { return image; }
    public void setImage(String value) { this.image = value; }

    public long getCategoryID() { return categoryID; }
    public void setCategoryID(long value) { this.categoryID = value; }

    public long getMarkeID() { return markeID; }
    public void setMarkeID(long value) { this.markeID = value; }

    public long getDetailMeasurementID() { return detailMeasurementID; }
    public void setDetailMeasurementID(long value) { this.detailMeasurementID = value; }

    public long getUnitMeasurementID() { return unitMeasurementID; }
    public void setUnitMeasurementID(long value) { this.unitMeasurementID = value; }
}
