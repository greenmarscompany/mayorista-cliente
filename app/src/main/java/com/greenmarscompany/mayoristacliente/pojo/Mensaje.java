package com.greenmarscompany.mayoristacliente.pojo;

public class Mensaje {
    private int id;
    private String nombre;
    private String message;
    private String image;
    private boolean status;

    public int getID() { return id; }
    public void setID(int value) { this.id = value; }

    public String getNombre() { return nombre; }
    public void setNombre(String value) { this.nombre = value; }

    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }

    public String getImage() { return image; }
    public void setImage(String value) { this.image = value; }

    public boolean getStatus() { return status; }
    public void setStatus(boolean value) { this.status = value; }
}
