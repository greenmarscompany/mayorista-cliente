package com.greenmarscompany.mayoristacliente.pojo;

public  class ProductGas {
    private int id;
    private String description;
    private float measurement;
    private float unit_price;
    private String image;
    Category_id category_id;
    Marke_id marke_id;
    Detail_measurement_id detail_measurement_id;
    Unit_measurement_id unit_measurement_id;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public float getMeasurement() {
        return measurement;
    }

    public float getUnit_price() {
        return unit_price;
    }

    public String getImage() {
        return image;
    }

    public Category_id getCategory_id() {
        return category_id;
    }

    public Marke_id getMarke_id() {
        return marke_id;
    }

    public Detail_measurement_id getDetail_measurement_id() {
        return detail_measurement_id;
    }

    public Unit_measurement_id getUnit_measurement_id() {
        return unit_measurement_id;
    }

    // Setter Methods

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMeasurement(float measurement) {
        this.measurement = measurement;
    }

    public void setUnit_price(float unit_price) {
        this.unit_price = unit_price;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCategory_id(Category_id category_idObject) {
        this.category_id = category_idObject;
    }

    public void setMarke_id(Marke_id marke_idObject) {
        this.marke_id = marke_idObject;
    }

    public void setDetail_measurement_id(Detail_measurement_id detail_measurement_idObject) {
        this.detail_measurement_id = detail_measurement_idObject;
    }

    public void setUnit_measurement_id(Unit_measurement_id unit_measurement_idObject) {
        this.unit_measurement_id = unit_measurement_idObject;
    }
}
