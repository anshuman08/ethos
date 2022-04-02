/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.mobileorders;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.data.loader.SerializerRead;

/**
 *
 * @author premsarojanand
 */
public class MobileAppDetailInfo implements SerializableRead, SerializableWrite, IKeyed {

    public int id, orderId, quantity;
    public String productId, productName, attributesetId, attributestockId, attributename ;
    public Double price, pricewithtax, tax;

    public MobileAppDetailInfo() {

    }

    public static SerializerRead getSerializerRead() {
        return (DataRead dr) -> {
            MobileAppDetailInfo mobileDetailInfo = new MobileAppDetailInfo();
            mobileDetailInfo.id = dr.getInt(1);
            mobileDetailInfo.orderId = dr.getInt(2);
            mobileDetailInfo.productId = dr.getString(3);
            mobileDetailInfo.productName = dr.getString(4);
            mobileDetailInfo.price = dr.getDouble(5);
            mobileDetailInfo.pricewithtax = dr.getDouble(6);
            mobileDetailInfo.quantity = dr.getInt(7);
            mobileDetailInfo.attributesetId = dr.getString(8);
            mobileDetailInfo.attributestockId = dr.getString(9);
            mobileDetailInfo.attributename = dr.getString(10);
            mobileDetailInfo.tax = dr.getDouble(11);
            return mobileDetailInfo;
        };
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getInt(1);
        orderId = dr.getInt(2);
        productId = dr.getString(3);
        productName = dr.getString(4);
        price = dr.getDouble(5);
        pricewithtax = dr.getDouble(6);
        quantity = dr.getInt(7);
        attributesetId = dr.getString(8);
        attributestockId = dr.getString(9);
        attributename = dr.getString(10);
        tax = dr.getDouble(11);

    }

    @Override
    public void writeValues(DataWrite dp) throws BasicException {
        dp.setInt(1, orderId);
        dp.setString(2, productId);
        dp.setString(3, productName);
        dp.setDouble(4, price);
        dp.setDouble(5, pricewithtax);
        dp.setInt(6, quantity);
        dp.setString(7, attributesetId);
        dp.setString(8, attributestockId);
        dp.setString(9, attributename);
        dp.setDouble(10, tax);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrder_id() {
        return orderId;
    }

    public void setOrder_id(int order_id) {
        this.orderId = order_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String product_id) {
        this.productId = product_id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAttributesetId() {
        return attributesetId;
    }

    public void setAttributesetId(String attributeset_id) {
        this.attributesetId = attributeset_id;
    }

    public String getAttributestockId() {
        return attributestockId;
    }

    public void setAttributestockId(String attributestock_id) {
        this.attributestockId = attributestock_id;
    }

    public String getAttributename() {
        return attributename;
    }

    public void setAttributename(String attributename) {
        this.attributename = attributename;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPricewithtax() {
        return pricewithtax;
    }

    public void setPricewithtax(Double pricewithtax) {
        this.pricewithtax = pricewithtax;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public MobileAppDetailInfo(int id, int orderId, int quantity, String productId, String productName, String attributesetId, String attributestockId, String attributename, Double price, Double pricewithtax, Double tax) {
        this.id = id;
        this.orderId = orderId;
        this.quantity = quantity;
        this.productId = productId;
        this.productName = productName;
        this.attributesetId = attributesetId;
        this.attributestockId = attributestockId;
        this.attributename = attributename;
        this.price = price;
        this.pricewithtax = pricewithtax;
        this.tax = tax;
    }

    @Override
    public Object getKey() {
        return id;
    }

}
