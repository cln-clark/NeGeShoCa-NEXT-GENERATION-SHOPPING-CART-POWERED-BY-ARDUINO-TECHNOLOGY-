package com.mycompany.posapplication;

public class Product {
    
    private String name;
    private double price;
    private int quantity;
    
    // Constructor
    public Product(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getQuantity(){
        return quantity;
    }
    
    @Override
    public String toString() {
        return "Product{name='" + name + "', quantity=" + quantity + ", price=" + price + "}";
    }
}
