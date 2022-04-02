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
import java.util.Date;

/**
 *
 * @author premsarojanand
 */
public class MobileAppInfo implements SerializableRead, SerializableWrite, IKeyed {

    public int id, totalItems, phone;
    public String orderNo, customerName, address, orderType,
            promotion_id, order_status, payment_method, transaction_id,
            payment_status, payment_party, siteguid, appname, table;
    public Date orderdate;
    public Double total_value, totalTax, totalDiscount, grossTotal,
            creditsUsed, creditsEarned, couponDiscount;

    public MobileAppInfo() {

    }

    static SerializerRead getSerializerRead() {
        return (DataRead dr) -> {
            MobileAppInfo mobileorders = new MobileAppInfo();
            mobileorders.id = dr.getInt(1);
            mobileorders.orderNo = dr.getString(2);
            mobileorders.orderdate = dr.getTimestamp(3);
            mobileorders.customerName = dr.getString(4);
            mobileorders.phone = dr.getInt(5);
            mobileorders.address = dr.getString(6);
            mobileorders.total_value = dr.getDouble(7);
            mobileorders.totalTax = dr.getDouble(8);
            mobileorders.totalItems = dr.getInt(9);
            mobileorders.totalDiscount = dr.getDouble(10);
            mobileorders.grossTotal = dr.getDouble(11);
            mobileorders.orderType = dr.getString(12);
            mobileorders.promotion_id = dr.getString(13);
            mobileorders.creditsUsed = dr.getDouble(14);
            mobileorders.creditsEarned = dr.getDouble(15);
            mobileorders.order_status = dr.getString(16);
            mobileorders.payment_method = dr.getString(17);
            mobileorders.transaction_id = dr.getString(18);
            mobileorders.payment_status = dr.getString(19);
            mobileorders.payment_party = dr.getString(20);
            mobileorders.couponDiscount = dr.getDouble(21);
            mobileorders.siteguid = dr.getString(22);
            mobileorders.appname = dr.getString(23);
            mobileorders.table = dr.getString(24);
            return mobileorders;
        };
    }

    public MobileAppInfo(int id, int totalItems, int phone, String orderNo, String customerName, String address, String orderType, Double total_value, String promotion_id, String order_status, String payment_method, String transaction_id, String payment_status, String payment_party, String siteguid, Date orderdate, Double totalTax, Double totalDiscount, Double grossTotal, Double creditsUsed, Double creditsEarned, Double couponDiscount, String appname, String table) {
        this.id = id;
        this.totalItems = totalItems;
        this.orderNo = orderNo;
        this.phone = phone;
        this.customerName = customerName;
        this.address = address;
        this.total_value = total_value;
        this.promotion_id = promotion_id;
        this.order_status = order_status;
        this.payment_method = payment_method;
        this.transaction_id = transaction_id;
        this.payment_status = payment_status;
        this.payment_party = payment_party;
        this.siteguid = siteguid;
        this.orderdate = orderdate;
        this.totalTax = totalTax;
        this.totalDiscount = totalDiscount;
        this.grossTotal = grossTotal;
        this.orderType = orderType;
        this.creditsUsed = creditsUsed;
        this.creditsEarned = creditsEarned;
        this.couponDiscount = couponDiscount;
        this.appname = appname;
        this.table = table;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getInt(1);
        orderNo = dr.getString(2);
        orderdate = dr.getTimestamp(3);
        customerName = dr.getString(4);
        phone = dr.getInt(5);
        address = dr.getString(6);
        total_value = dr.getDouble(7);
        totalTax = dr.getDouble(8);
        totalItems = dr.getInt(9);
        totalDiscount = dr.getDouble(10);
        grossTotal = dr.getDouble(11);
        orderType = dr.getString(12);
        promotion_id = dr.getString(13);
        creditsUsed = dr.getDouble(14);
        creditsEarned = dr.getDouble(15);
        order_status = dr.getString(16);
        payment_method = dr.getString(17);
        transaction_id = dr.getString(18);
        payment_status = dr.getString(19);
        payment_party = dr.getString(20);
        couponDiscount = dr.getDouble(21);
        siteguid = dr.getString(22);
        appname = dr.getString(23);
        table = dr.getString(24);
    }

    @Override
    public void writeValues(DataWrite dp) throws BasicException {
        dp.setString(1, orderNo);
        dp.setTimestamp(2, orderdate);
        dp.setString(3, customerName);
        dp.setInt(4, phone);
        dp.setString(5, address);
        dp.setDouble(6, total_value);
        dp.setDouble(7, totalTax);
        dp.setInt(8, totalItems);
        dp.setDouble(9, totalDiscount);
        dp.setDouble(10, grossTotal);
        dp.setString(11, orderType);
        dp.setString(12, promotion_id);
        dp.setDouble(13, creditsUsed);
        dp.setDouble(14, creditsEarned);
        dp.setString(15, order_status);
        dp.setString(16, payment_method);
        dp.setString(17, transaction_id);
        dp.setString(18, payment_status);
        dp.setString(19, payment_party);
        dp.setDouble(20, couponDiscount);
        dp.setString(21, siteguid);
        dp.setString(23, appname);
        dp.setString(24, table);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAppName() {
        return appname;
    }

    public void setAppName(String appname) {
        this.appname = appname;
    }

    public String getTable() {
        return table;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int m_totalItems) {
        this.totalItems = m_totalItems;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(String promotion_id) {
        this.promotion_id = promotion_id;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getPayment_party() {
        return payment_party;
    }

    public void setPayment_party(String payment_party) {
        this.payment_party = payment_party;
    }

    public String getSiteguid() {
        return siteguid;
    }

    public void setSiteguid(String siteguid) {
        this.siteguid = siteguid;
    }

    public Date getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }

    public Double getTotal_value() {
        return total_value;
    }

    public void setTotal_value(Double total_value) {
        this.total_value = total_value;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Double getGrossTotal() {
        return grossTotal;
    }

    public void setGrossTotal(Double grossTotal) {
        this.grossTotal = grossTotal;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Double getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Double creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public Double getCreditsEarned() {
        return creditsEarned;
    }

    public void setCreditsEarned(Double creditsEarned) {
        this.creditsEarned = creditsEarned;
    }

    public Double getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(Double couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    @Override
    public Object getKey() {
        return id;
    }

}
