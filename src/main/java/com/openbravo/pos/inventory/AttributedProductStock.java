package com.openbravo.pos.inventory;

import java.util.Objects;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 26, 2017, 9:25:07 PM
 */
public class AttributedProductStock implements IKeyed {

    private String id;

    private String name;

    private String productId;

    private String attributeSetId;

    private int quantity;

    private double price;

    private int orderQty;

    private int minQty;

    private int maxQty;

    private String productName;

    private AttributeSetInfo attrSetInfo;

    private boolean newStock = true;

    private boolean markedForDelete;
    
    private boolean countable;

    public AttributedProductStock() {
    }

    public AttributedProductStock(String id, String name, String productId) {
        this.id = id;
        this.name = name;
        this.productId = productId;
    }

    @Override
    public Object getKey() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public boolean isNewStock() {
        return newStock;
    }

    public void setMarkedForDelete(boolean markedForDelete) {
        this.markedForDelete = markedForDelete;
    }

    public boolean isMarkedForDelete() {
        return markedForDelete;
    }

    public String getProductId() {
        return productId;
    }

    public String getAttributeSetId() {
        return attributeSetId;
    }

    public AttributeSetInfo getAttrSetInfo() {
        return attrSetInfo;
    }

    public void setAttributeSetId(String attributeSetId) {
        this.attributeSetId = attributeSetId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getOrderQty() {
        return orderQty;
    }

    public void setOrderQty(int orderQty) {
        this.orderQty = orderQty;
    }

    public int getMinQty() {
        return minQty;
    }

    public void setMinQty(int minQty) {
        this.minQty = minQty;
    }

    public int getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(int maxQty) {
        this.maxQty = maxQty;
    }

    public String getProductName() {
        return productName;
    }
    
    public boolean isCountable() {
        return countable;
    }

    public void setCountable(boolean countable) {
        this.countable = countable;
    }

    public static AttributedProductStock readValue(final DataRead dr) throws BasicException {
        final AttributedProductStock productStock = new AttributedProductStock();
        productStock.id = dr.getString(1);
        productStock.name = dr.getString(2);
        productStock.productId = dr.getString(3);
        productStock.attributeSetId = dr.getString(4);
        productStock.quantity = dr.getInt(5);
        productStock.price = dr.getDouble(6);
        productStock.newStock = false;

        final String attrSetName = dr.getString(7);
        productStock.attrSetInfo = new AttributeSetInfo(productStock.attributeSetId, attrSetName);
         //countable
        productStock.countable = dr.getBoolean(8);
        return productStock;
    }

    public static AttributedProductStock readValueWithInventoryInformation(final DataRead dr) throws BasicException {
        final AttributedProductStock productStock = readValue(dr);
        productStock.orderQty = dr.getInt(9);
        productStock.minQty = dr.getInt(10);
        productStock.maxQty = dr.getInt(11);
        productStock.maxQty = dr.getInt(10);

        return productStock;
    }

    public static AttributedProductStock readValueWithInventoryInformationAndProductName(final DataRead dr) throws BasicException {
        final AttributedProductStock productStock = readValue(dr);
        productStock.orderQty = dr.getInt(9);
        productStock.minQty = dr.getInt(10);
        productStock.maxQty = dr.getInt(11);
        productStock.productName = dr.getString(12);

        return productStock;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttributedProductStock other = (AttributedProductStock) obj;
        return Objects.equals(this.id, other.id);
    }

}
