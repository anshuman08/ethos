/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.mobileorders;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteInteger;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author premsarojanand
 */
public class DatalogicMobileAppSales extends BeanFactoryDataSingle {

    protected Session s;
    protected AppView m_App;

    private final AppConfig m_config;

    public DatalogicMobileAppSales() {
        AppView app = null;
        m_App = app;
        m_config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        m_config.load();

    }

    @Override
    public void init(Session s) {
        this.s = s;
    }

    public final List<MobileAppInfo> getOrders() throws BasicException {
        return new PreparedSentence(s, "select id,`order_no`,`order_date`,`customer_name`,`customer_phone`,"
                + "`address`,`total_value`,`total_tax`,`total_items`,`total_discount`,`gross_total`,"
                + "`order_type`,`promotion_id`,`credits_used`,`credits_earned`,`order_status`,"
                + "`payment_method`,`transactionid`,`payment_status`,`payment_party`,`coupon_discount`,"
                + "`siteguid` , `appname` , `table` from app_orders where order_status = 'initiated'", null, new SerializerReadClass(MobileAppInfo.class)).list();

    }

    public final List<MobileAppDetailInfo> getOrderDetails(int orderId) throws BasicException {
        String sql = "SELECT app_order_details.id,order_id,product,products.name,price,pricewithtax,quantity,products.attributeset_id,attributestock_id,attributename,tax "
                + "FROM app_order_details join products on products.id = app_order_details.product "
                + "join app_orders on app_orders.order_no = app_order_details.order_id where app_orders.order_no =  " + orderId;

        return new PreparedSentence(s, sql, null, new SerializerReadClass(MobileAppDetailInfo.class)).list();
    }

    public final MobileAppDetailInfo getOrderDetail(int orderId) throws BasicException {
        String sql = "SELECT app_order_details.id,order_id,product,products.name,price,pricewithtax,quantity,products.attributeset_id,attributestock_id,attributename,tax "
                + "FROM app_order_details join products on products.id = app_order_details.product "
                + "join app_orders on app_orders.order_no = app_order_details.order_id where app_orders.order_no =  ?";

        return (MobileAppDetailInfo) new PreparedSentence(s, sql, SerializerWriteInteger.INSTANCE, MobileAppDetailInfo.getSerializerRead()).find(orderId);
    }

    public MobileAppInfo getOrder(int id) throws BasicException {
        String sql = "select id,`order_no`,`order_date`,`customer_name`,`customer_phone`,"
                + "`address`,`total_value`,`total_tax`,`total_items`,`total_discount`,`gross_total`,"
                + "`order_type`,`promotion_id`,`credits_used`,`credits_earned`,`order_status`,"
                + "`payment_method`,`transactionid`,`payment_status`,`payment_party`,`coupon_discount`,"
                + "`siteguid` ,`appname` , `table` from app_orders where order_no = ?";
        return (MobileAppInfo) new PreparedSentence(s, sql, SerializerWriteInteger.INSTANCE, MobileAppInfo.getSerializerRead()).find(id);
    }
    
    public final int setOrderUpdate(MobileAppInfo mobileAppInfo){
        int i = 0;
        try {
            final Object[] data = new Object[11];
            data[0] = mobileAppInfo.getTotal_value();
            data[1] = mobileAppInfo.getTotalTax();
            data[2] = mobileAppInfo.getTotalItems();
            data[3] = mobileAppInfo.getTotalDiscount();
            data[4] = mobileAppInfo.getGrossTotal();
            data[5] = mobileAppInfo.getOrder_status();
            data[6] = mobileAppInfo.getPayment_method();
            data[7] = mobileAppInfo.getPayment_status();
            data[8] = mobileAppInfo.getCouponDiscount();
            data[9] = mobileAppInfo.getTable();
            data[10] = mobileAppInfo.getOrderNo();
            
            final Datas[] datas = new Datas[11];
            datas[0] = Datas.DOUBLE;
            datas[1] = Datas.DOUBLE;
            datas[2] = Datas.INT;
            datas[3] = Datas.DOUBLE;
            datas[4] = Datas.DOUBLE;
            datas[5] = Datas.STRING;
            datas[6] = Datas.STRING;
            datas[7] = Datas.STRING;
            datas[8] = Datas.DOUBLE;
            datas[9] = Datas.STRING;
            datas[10] = Datas.STRING;
            i = new PreparedSentence(s,
                    "UPDATE app_orders SET "
                            + "total_value = ?, "
                            + "total_tax = ?, "
                            + "total_items = ?, "
                            + "total_discount = ?, "
                            + "gross_total = ?, "
                            + "order_status = ?, "
                            + "payment_method = ?, "
                            + "payment_status = ?, "
                            + "coupon_discount = ?, "
                            + "`table` = ? "
                            + "WHERE order_no = ?",
                    new SerializerWriteBasicExt(datas, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}))
                    .exec(data);
            
        } catch (BasicException ex) {
            Logger.getLogger(DatalogicMobileAppSales.class.getName()).log(Level.SEVERE, null, ex);
        }
      return i;  
    }

}
