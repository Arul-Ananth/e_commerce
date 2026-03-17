package org.example.modules.checkout.payment;

import java.math.BigDecimal;

public class PaymentLineItem {

    private String name;
    private BigDecimal unitAmount;
    private int quantity;

    public PaymentLineItem() {
    }

    public PaymentLineItem(String name, BigDecimal unitAmount, int quantity) {
        this.name = name;
        this.unitAmount = unitAmount;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public int getQuantity() {
        return quantity;
    }
}
