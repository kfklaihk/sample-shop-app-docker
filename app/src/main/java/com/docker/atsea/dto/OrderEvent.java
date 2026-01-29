package com.docker.atsea.dto;

import java.io.Serializable;
import java.util.List;

public class OrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private List<ProductDetail> products;
    private double totalPrice;

    public OrderEvent() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<ProductDetail> getProducts() { return products; }
    public void setProducts(List<ProductDetail> products) { this.products = products; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public static class ProductDetail implements Serializable {
        private String name;
        private int quantity;
        private double price;

        public ProductDetail() {}
        public ProductDetail(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}
