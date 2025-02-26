package com.ecom.pradeep.angadi_bk.model;

import java.math.BigDecimal;

public class CartSummary {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;

    public CartSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
}
