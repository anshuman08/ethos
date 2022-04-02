//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2017 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.forms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.CustomerTransaction;
import com.openbravo.pos.inventory.*;
import com.openbravo.pos.mant.FloorsInfo;
import com.openbravo.pos.mant.TableArrangement;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.sales.restaurant.Place;
import com.openbravo.pos.suppliers.SupplierInfo;
import com.openbravo.pos.suppliers.SupplierInfoExt;
import com.openbravo.pos.ticket.*;
import com.openbravo.pos.voucher.VoucherInfo;
import edu.emory.mathcs.backport.java.util.Collections;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author adrianromero
 * @author jackgerrard
 */
public class DataLogicSales extends BeanFactoryDataSingle {

    protected Session s;

    protected Datas[] auxiliarDatas;

    protected Datas[] stockdiaryDatas;

    protected Datas[] paymenttabledatas;

    protected Datas[] stockdatas;

    protected Datas[] stockAdjustDatas;

    protected Datas[] databasechangelog;

    protected Datas[] stockdiaryDatasDatabse;

    protected Datas[] stockAdjustDatasDatabase;

    protected Row productsRow;

    protected Row productsRowDataBase;

    protected Row customersRow;

    private String pName;

    private Double getTotal;

    private Double getTendered;

    private String getRetMsg;

    private String getVoucher;

    public static final String DEBT = "debt";

    public static final String DEBT_PAID = "debtpaid";

    protected static final String PREPAY = "prepay";

    private static final Logger logger = Logger.getLogger("com.openbravo.pos.forms.DataLogicSales");

    private String getCardName;

    protected SentenceExec m_createCat;

    protected SentenceExec m_createSupp;

    protected AppView m_App;

    private final AppConfig m_config;
    // for sync
    private String siteguid;
    private String payment_id = null;
    private String tax_id = null;

    public SentenceList stln = null;

    public DataLogicSales() {
        AppView app = null;
        m_config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        m_config.load();
        m_App = app;
        // prevoius 12 now 13
        stockdiaryDatas = new Datas[]{
            Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING};

        stockdiaryDatasDatabse = new Datas[]{
            Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.INT, Datas.INT, Datas.STRING};

        databasechangelog = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.INT, Datas.INT};

        paymenttabledatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.TIMESTAMP,
            Datas.STRING, Datas.STRING, Datas.DOUBLE,
            Datas.STRING};

        stockdatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE};

        stockAdjustDatas = new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.DOUBLE};

        stockAdjustDatasDatabase = new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.DOUBLE, Datas.STRING,
            Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING,
            Datas.INT};

        auxiliarDatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.STRING};

        productsRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING), //1
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true), //2
                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true), //3
                new Field(AppLocal.getIntString("label.prodbarcodetype"), Datas.STRING, Formats.STRING, false, true,//4
                        true),
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true),//5
                new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),//6
                new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY, false, true,//7
                        true),
                new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING, false, false, true),//8
                new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING, false, false, true),//9
                new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING, false, false, true),//10
                new Field("STOCKCOST", Datas.DOUBLE, Formats.CURRENCY),//11
                new Field("STOCKVOLUME", Datas.DOUBLE, Formats.DOUBLE),//12
                new Field("IMAGE", Datas.IMAGE, Formats.NULL),//13
                new Field("ISCOM", Datas.BOOLEAN, Formats.BOOLEAN),//14
                new Field("ISSCALE", Datas.BOOLEAN, Formats.BOOLEAN),//15
                new Field("ISCONSTANT", Datas.BOOLEAN, Formats.BOOLEAN),//16
                new Field("PRINTKB", Datas.BOOLEAN, Formats.BOOLEAN),//17
                new Field("SENDSTATUS", Datas.BOOLEAN, Formats.BOOLEAN),//18
                new Field("ISSERVICE", Datas.BOOLEAN, Formats.BOOLEAN),//19
                new Field("PROPERTIES", Datas.BYTES, Formats.NULL),//20
                new Field(AppLocal.getIntString("label.display"), Datas.STRING, Formats.STRING, false, true, true),//21
                new Field("ISVPRICE", Datas.BOOLEAN, Formats.BOOLEAN),//22
                new Field("ISVERPATRIB", Datas.BOOLEAN, Formats.BOOLEAN),//23
                new Field("TEXTTIP", Datas.STRING, Formats.STRING),//24
                new Field("WARRANTY", Datas.BOOLEAN, Formats.BOOLEAN),//25
                new Field(AppLocal.getIntString("label.stockunits"), Datas.DOUBLE, Formats.DOUBLE),//26
                new Field("PRINTTO", Datas.STRING, Formats.STRING),//27
                new Field(AppLocal.getIntString("label.prodsupplier"), Datas.STRING, Formats.STRING, false, false, true),//28
                new Field(AppLocal.getIntString("label.UOM"), Datas.STRING, Formats.STRING),//29
                new Field("MEMODATE", Datas.TIMESTAMP, Formats.DATE),//30
                new Field("imgurl",Datas.STRING, Formats.STRING),//31
                new Field("description", Datas.STRING, Formats.STRING),//32
                new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),//33
                new Field("CATORDER", Datas.INT, Formats.INT),//34
                new Field("SITEGUID", Datas.STRING, Formats.STRING)//35
        );

        productsRowDataBase = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true),
                new Field(AppLocal.getIntString("label.prodbarcodetype"), Datas.STRING, Formats.STRING, false, true,
                        true),
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
                new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY, false, true,
                        true),
                new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING, false, false, true),
                new Field("STOCKCOST", Datas.DOUBLE, Formats.CURRENCY),
                new Field("STOCKVOLUME", Datas.DOUBLE, Formats.DOUBLE),
                new Field("IMAGE", Datas.IMAGE, Formats.NULL),
                new Field("ISCOM", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISSCALE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISCONSTANT", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("PRINTKB", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("SENDSTATUS", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISSERVICE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("PROPERTIES", Datas.BYTES, Formats.NULL),
                new Field(AppLocal.getIntString("label.display"), Datas.STRING, Formats.STRING, false, true, true),
                new Field("ISVPRICE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISVERPATRIB", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("TEXTTIP", Datas.STRING, Formats.STRING),
                new Field("WARRANTY", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field(AppLocal.getIntString("label.stockunits"), Datas.DOUBLE, Formats.DOUBLE),
                new Field("PRINTTO", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.prodsupplier"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.UOM"), Datas.STRING, Formats.STRING),
                new Field("MEMODATE", Datas.TIMESTAMP, Formats.DATE),
                new Field("imgurl",Datas.STRING, Formats.STRING),//31
                new Field("description", Datas.STRING, Formats.STRING),//32
                new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("CATORDER", Datas.INT, Formats.INT),
                new Field("SITEGUID", Datas.STRING, Formats.STRING),
                new Field("process", Datas.STRING, Formats.STRING),
                new Field("tablename", Datas.STRING, Formats.STRING),
                new Field("table_pk_name", Datas.STRING, Formats.STRING),
                new Field("table_sc_name", Datas.STRING, Formats.STRING),
                new Field("sflag", Datas.INT, Formats.INT),
                new Field("lflag", Datas.INT, Formats.INT),
                new Field("ID", Datas.STRING, Formats.STRING)
        );

// creating customers object here for now for future global reuse
// LOYALTY, MEMBERSHIP & etc as will be more system centric than customer
        customersRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field("SEARCHKEY", Datas.STRING, Formats.STRING),
                new Field("TAXID", Datas.STRING, Formats.STRING),
                new Field("NAME", Datas.STRING, Formats.STRING),
                new Field("TAXCATEGORY", Datas.STRING, Formats.STRING),
                new Field("CARD", Datas.STRING, Formats.STRING),
                new Field("MAXDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("ADDRESS", Datas.STRING, Formats.STRING),
                new Field("ADDRESS2", Datas.STRING, Formats.STRING),
                new Field("POSTAL", Datas.STRING, Formats.STRING),
                new Field("CITY", Datas.STRING, Formats.STRING),
                new Field("REGION", Datas.STRING, Formats.STRING),
                new Field("COUNTRY", Datas.STRING, Formats.STRING),
                new Field("FIRSTNAME", Datas.STRING, Formats.STRING),
                new Field("LASTNAME", Datas.STRING, Formats.STRING),
                new Field("EMAIL", Datas.STRING, Formats.STRING),
                new Field("PHONE", Datas.STRING, Formats.STRING),
                new Field("PHONE2", Datas.STRING, Formats.STRING),
                new Field("FAX", Datas.STRING, Formats.STRING),
                new Field("NOTES", Datas.STRING, Formats.STRING),
                new Field("VISIBLE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("CURDATE", Datas.STRING, Formats.TIMESTAMP),
                new Field("CURDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("IMAGE", Datas.BYTES, Formats.NULL),
                new Field("ISVIP", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("DISCOUNT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("MEMODATE", Datas.STRING, Formats.TIMESTAMP),
                new Field("SITEGUID", Datas.STRING, Formats.TIMESTAMP)
        );

    }

    /**
     *
     * @param s
     */
    @Override
    public void init(Session s) {
        this.s = s;
        SentenceList attsent = new StaticSentence(s,
                "SELECT "
                + "* "
                + "FROM siteguid ",
                null,
                (DataRead dr) -> new SiteguidInfo(
                        dr.getString(1),
                        dr.getInt(2),
                        dr.getInt(3)
                ));
        try {
            siteguid = attsent.list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");
        } catch (BasicException ex) {
            Logger.getLogger(DataLogicSales.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println(siteguid);

        m_createCat = new StaticSentence(s,
                "INSERT INTO categories ( ID, NAME, CATSHOWNAME,imgurl, description ) "
                + "VALUES (?, ?, ?)", new SerializerWriteBasic(new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.BOOLEAN})
        );

        m_createSupp = new StaticSentence(s,
                "INSERT INTO suppliers ( ID, NAME, SEARCHKEY, VISIBLE ) "
                + "VALUES (?, ?, ?, ?)", new SerializerWriteBasic(new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.BOOLEAN})
        );

        stln = new StaticSentence(s, "SELECT max(linenumber) from stockcurrent", null, (DataRead dr) -> (dr.getInt(1)));
    }

// Import Creates
    public final void createCategory(Object[] category) throws BasicException {
        m_createCat.exec(category);
    }

    public final void createSupplier(Object[] supplier) throws BasicException {
        m_createSupp.exec(supplier);
    }
// End Import Creates

    public final Row getProductsRow() {
        return productsRow;
    }

    public final Row getCustomersRow() {
        return customersRow;
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfo(String id) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description "
                //                                                     + "SITEGUID "
                + "FROM products WHERE ID = ?", SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(id);
    }

    public final ProductInfoExt getProductInfoByCode(String sCode) throws BasicException {
//        if (sCode.length() == 13 && (sCode.startsWith("2") || sCode.startsWith("02")))
//            return  getProductInfoByShortCode(sCode);
//        else {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl,description "
                //                                                     + "SITEGUID "
                + "FROM products WHERE CODE = ?", SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(sCode);
//        }
    }

    public final ProductInfoExt getProductInfoByShortCode(String sCode) throws BasicException {

        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE , imgurl, description "  // image URL and Description
                //                                                     + "SITEGUID "
                + "FROM products "
                + "WHERE SUBSTRING( CODE, 3, 6 ) = ?",
                SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead())
                .find(sCode.substring(2, 8));
    }
    
    public List<Place> getEmptyTables() throws BasicException {
     SentenceList sent1 = new StaticSentence(s, "Select id,name,X, Y, floor,customer,waiter,ticketid,tablemoved, design from places where ticketid  IS NULL and floor = 02 order by floor, id", null,
                    new SerializerReadClass(Place.class));
     return sent1.list();
}

    /*
     * Important Note: Deliberately extracted from other code to force strict UPC-A (full 12 digits) Why? Because other
     * manf' or in-store codes may exist and we just need a single record returned. Also, handling things this way will
     * allow use (future) of a COUPON code (5 or 9 normally used) in-store
     *
     */
    public final ProductInfoExt getProductInfoByUShortCode(String sCode) throws BasicException {

        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description "           //       description
                //                                                     + "SITEGUID "
                + "FROM products "
                + "WHERE LEFT( CODE, 7 ) = ? AND CODETYPE = 'UPC-A' " //  selection of 7 digits ie: 2123456 specific to allow for other 12 digit
                //  codes that may be in use at positions 234567
                //  last digit (position 7) can be used to identify COUPON (5 or 9) - FUTURE
                ,
                 SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead())
                .find(sCode.substring(0, 7));
    }

    /**
     *
     * @param sReference
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfoByReference(String sReference) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description " // image url  description
                //                                                     + "SITEGUID "
                + "FROM products WHERE REFERENCE = ?",
                SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead())
                .find(sReference);
    }

    /**
     *
     * @return @throws BasicException
     */
    public final List<CategoryInfo> getRootCategories() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "id, "
                + "name, "
                + "image, "
                + "imgurl,"
                + "description,  "
                + "texttip, "
                + "catshowname, "
                + "catorder "
                + "FROM categories "
                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + s.DB.TRUE() + " "
                + "ORDER BY CATORDER, NAME", null, CategoryInfo.getSerializerRead()).list();
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public final List<CategoryInfo> getSubcategories(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "imgurl, "
                + "description, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER "
                + "FROM categories WHERE PARENTID = ? "
                + "ORDER BY CATORDER, NAME", SerializerWriteString.INSTANCE, CategoryInfo
                        .getSerializerRead()).list(category);
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductCatalog(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.CATEGORY, "
                + "P.TAXCAT, "
                + "P.ATTRIBUTESET_ID, "
                + "P.STOCKCOST, "
                + "P.STOCKVOLUME, "
                + "P.IMAGE, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.ISCONSTANT, "
                + "P.PRINTKB, "
                + "P.SENDSTATUS, "
                + "P.ISSERVICE, "
                + "P.ATTRIBUTES, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.PRINTTO, "
                + "P.SUPPLIER, "
                + "P.UOM, "
                + "P.MEMODATE, P.imgurl, P.description "
                + "FROM products P, products_cat O "
                + "WHERE P.ID = O.PRODUCT AND P.CATEGORY = ? "
                + "ORDER BY O.CATORDER, P.NAME ", SerializerWriteString.INSTANCE, ProductInfoExt
                        .getSerializerRead()).list(category);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductComments(String id) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.CATEGORY, "
                + "P.TAXCAT, "
                + "P.ATTRIBUTESET_ID, "
                + "P.STOCKCOST, "
                + "P.STOCKVOLUME, "
                + "P.IMAGE, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.ISCONSTANT, "
                + "P.PRINTKB, "
                + "P.SENDSTATUS, "
                + "P.ISSERVICE, "
                + "P.ATTRIBUTES, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.PRINTTO, "
                + "P.SUPPLIER, "
                + "P.UOM, "
                + "P.MEMODATE, P.imgurl , P.description "
                + "FROM products P, "
                + "products_cat O, products_com M "
                + "WHERE P.ID = O.PRODUCT AND P.ID = M.PRODUCT2 AND M.PRODUCT = ? "
                + "AND P.ISCOM = " + s.DB.TRUE() + " "
                + "ORDER BY O.CATORDER, P.NAME", SerializerWriteString.INSTANCE, ProductInfoExt
                        .getSerializerRead()).list(id);
    }

    // JG uniCenta June 2014 includes StockUnits
    /**
     *
     * @return @throws BasicException
     */
    public List<ProductInfoExt> getProductConstant() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "products.ID, "
                + "products.REFERENCE, "
                + "products.CODE, "
                + "products.CODETYPE, "
                + "products.NAME, "
                + "products.PRICEBUY, "
                + "products.PRICESELL, "
                + "products.CATEGORY, "
                + "products.TAXCAT, "
                + "products.ATTRIBUTESET_ID, "
                + "products.STOCKCOST, "
                + "products.STOCKVOLUME, "
                + "products.IMAGE, "
                + "products.ISCOM, "
                + "products.ISSCALE, "
                + "products.ISCONSTANT, "
                + "products.PRINTKB, "
                + "products.SENDSTATUS, "
                + "products.ISSERVICE, "
                + "products.ATTRIBUTES, "
                + "products.DISPLAY, "
                + "products.ISVPRICE, "
                + "products.ISVERPATRIB, "
                + "products.TEXTTIP, "
                + "products.WARRANTY, "
                + "products.STOCKUNITS, "
                + "products.PRINTTO, "
                + "products.SUPPLIER, "
                + "products.UOM, "
                + "products.MEMODATE, products.imgurl, products.description "
                + "FROM categories INNER JOIN products ON (products.CATEGORY = categories.ID) "
                + "WHERE products.ISCONSTANT = " + s.DB.TRUE() + " "
                + "ORDER BY categories.NAME, products.NAME",
                null,
                ProductInfoExt.getSerializerRead()).list();

    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final CategoryInfo getCategoryInfo(String id) throws BasicException {
        return (CategoryInfo) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "imgurl, "
                + "description, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER "
                + "FROM categories "
                + "WHERE ID = ? "
                + "ORDER BY CATORDER, NAME", SerializerWriteString.INSTANCE,
                CategoryInfo.getSerializerRead()).find(id);
    }

    /**
     *
     * @return
     */
    public final SentenceList getProductList() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description "
                + "FROM products "
                + "WHERE ?(QBF_FILTER) "
                + "ORDER BY REFERENCE",
                new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public SentenceList getProductListNormal() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl,description "
                + "FROM products "
                + "WHERE ISCOM = " + s.DB.FALSE() + " AND ?(QBF_FILTER) ORDER BY REFERENCE",
                new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public SentenceList getProductsList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description "
                + "FROM products "
                + "ORDER BY NAME", null, ProductInfo.getSerializerRead());
    }

    public SentenceList getExtendedProductsList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description "
                + "FROM products "
                + "ORDER BY NAME", null, ProductInfoExt.getSerializerRead());
    }

    public SentenceList getProductList2() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "products.id, "
                + "products.name, "
                + "stockcurrent.units, "
                + "locations.name, "
                + "products.pricesell, "
                + "taxes.rate, "
                + "products.pricesell + (products.pricesell * taxes.rate) AS SellIncTax "
                + " FROM (((stockcurrent stockcurrent "
                + "INNER JOIN locations locations "
                + "ON (stockcurrent.location = locations.id)) "
                + "INNER JOIN products products "
                + "ON (stockcurrent.product = products.id)) "
                + "INNER JOIN taxcategories taxcategories "
                + "ON (products.taxcat = taxcategories.id)) "
                + "INNER JOIN taxes taxes "
                + "ON (taxes.category = taxcategories.id) "
                + "WHERE ?(QBF_FILTER) "
                + "GROUP BY products.name ",
                new String[]{"NAME", "UNITS", "SellIncTax", "LOCATION",}), new SerializerWriteBasic(
                new Datas[]{
                    Datas.OBJECT, Datas.STRING,
                    Datas.OBJECT, Datas.DOUBLE,
                    Datas.OBJECT, Datas.DOUBLE,
                    Datas.OBJECT, Datas.STRING}), ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public SentenceList getProductListAuxiliar() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, imgurl, description "
                + "FROM products "
                + "WHERE ISCOM = " + s.DB.TRUE() + " AND ?(QBF_FILTER) "
                + "ORDER BY REFERENCE",
                new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), ProductInfoExt.getSerializerRead());
    }

    /**
     *
     * @param productId The product id to look for bundle
     * @return List of products part of the searched product
     * @throws BasicException
     */
    public final List<ProductsBundleInfo> getProductsBundle(String productId) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "PRODUCT, "
                + "PRODUCT_BUNDLE, "
                + "QUANTITY "
                + "FROM products_bundle WHERE PRODUCT = ?", SerializerWriteString.INSTANCE,
                ProductsBundleInfo.getSerializerRead()).list(productId);
    }

    /**
     * JG Oct 2016 Called from JPanelTicket
     *
     * @param pId
     * @param location
     * @return
     * @throws BasicException
     */
    public final ProductStock getProductStockState(String pId, String location) throws BasicException {

        PreparedSentence preparedSentence = new PreparedSentence(s,
                "SELECT "
                + "products.id, "
                + "locations.id as Location, "
                + "stockcurrent.units AS Current, "
                + "stocklevel.stocksecurity AS Minimum, "
                + "stocklevel.stockmaximum AS Maximum, "
                + "products.pricebuy, "
                + "products.pricesell, "
                + "products.memodate "
                + "FROM locations "
                + "INNER JOIN ((products "
                + "INNER JOIN stockcurrent "
                + "ON products.id = stockcurrent.product) "
                + "LEFT JOIN stocklevel ON products.id = stocklevel.product) "
                + "ON locations.id = stockcurrent.location "
                + "WHERE products.id = ? "
                + "AND locations.id = ?",
                SerializerWriteString.INSTANCE, ProductStock
                        .getSerializerRead());

        ProductStock productStock = (ProductStock) preparedSentence.find(pId, location);

        return productStock;
    }

    /**
     * JG May 2016 Called from StockManagement
     *
     * @param pId
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<ProductStock> getProductStockList(String pId) throws BasicException {
        return new PreparedSentence(s,
                "SELECT products.id, "
                + "locations.name AS Location, "
                + "stockcurrent.units AS Current, "
                + "stocklevel.stocksecurity AS Minimum, "
                + "stocklevel.stockmaximum AS Maximum, "
                + "Round(products.pricebuy,2) AS PriceBuy, "
                + "Round((products.pricesell * taxes.rate) + products.pricesell,2) AS PriceSell, "
                + "products.memodate "
                + "FROM taxcategories TC "
                + "INNER JOIN taxes taxes "
                + "ON (TC.id = taxes.category) "
                + "RIGHT OUTER JOIN products products "
                + "ON (products.TAXCAT = TC.id) "
                + "LEFT OUTER JOIN stocklevel stocklevel "
                + "ON (stocklevel.product = products.ID) "
                + "LEFT OUTER JOIN stockcurrent stockcurrent "
                + "ON (products.ID = stockcurrent.product) "
                + "INNER JOIN locations locations "
                + "ON (stockcurrent.location = locations.id) "
                + "WHERE products.id= ? ",
                SerializerWriteString.INSTANCE,
                ProductStock.getSerializerRead()).list(pId);
    }

    /**
     * JG Sept 2017
     *
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<ReprintTicketInfo> getReprintTicketList() throws BasicException {
        return (List<ReprintTicketInfo>) new StaticSentence(s, "SELECT "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME, "
                + "SUM(PM.TOTAL), "
                + "T.STATUS "
                + "FROM receipts "
                + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "GROUP BY "
                + "T.ID, "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME "
                + "ORDER BY R.DATENEW DESC, T.TICKETID "
                + "LIMIT 10 ", null, new SerializerReadClass(
                        ReprintTicketInfo.class)).list();
    }

    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final TicketInfo getReprintTicket(String Id) throws BasicException {

        if (Id == null) {
            return null;
        } else {
            Object[] record = (Object[]) new StaticSentence(s, "SELECT "
                    + "T.TICKETID, "
                    + "SUM(PM.TOTAL), "
                    + "R.DATENEW, "
                    + "P.NAME, "
                    + "T.TICKETTYPE, "
                    + "C.NAME, "
                    + "T.STATUS "
                    + "FROM receipts "
                    + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                    + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                    + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                    + "WHERE T.TICKETID = ?", SerializerWriteString.INSTANCE,
                    new SerializerReadBasic(new Datas[]{Datas.SERIALIZABLE}))
                    .find(Id);
            return record == null ? null : (TicketInfo) record[0];
        }
    }

    //Tickets and Receipt list
    public SentenceList getTicketsList() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME, "
                + "SUM(PM.TOTAL), "
                + "T.STATUS "
                + "FROM receipts "
                + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "WHERE ?(QBF_FILTER) "
                + "GROUP BY "
                + "T.ID, "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME "
                + "ORDER BY R.DATENEW DESC, T.TICKETID",
                new String[]{
                    "T.TICKETID", "T.TICKETTYPE", "PM.TOTAL", "R.DATENEW", "R.DATENEW", "P.NAME", "C.NAME"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.INT,
            Datas.OBJECT, Datas.INT,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.TIMESTAMP,
            Datas.OBJECT, Datas.TIMESTAMP,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), new SerializerReadClass(FindTicketsInfo.class));
    }

    //User list
    /**
     *
     * @return
     */
    public final SentenceList getUserList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM people "
                + "ORDER BY NAME", null, (DataRead dr) -> new TaxCategoryInfo(
                        dr.getString(1),
                        dr.getString(2)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "CATEGORY, "
                + "CUSTCATEGORY, "
                + "PARENTID, "
                + "RATE, "
                + "RATECASCADE, "
                + "RATEORDER "
                + "FROM taxes "
                + "ORDER BY NAME", null, (DataRead dr) -> new TaxInfo(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3),
                        dr.getString(4),
                        dr.getString(5),
                        dr.getDouble(6),
                        dr.getBoolean(7),
                        dr.getInt(8)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getCategoriesList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "imgurl, "
                + "description, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER "
                + "FROM categories "
                + "ORDER BY NAME", null, CategoryInfo.getSerializerRead());
    }

    /**
     * JG Feb 2017 Returns all PARENT categories
     *
     * @return
     */
    public final SentenceList getCategoriesList_1() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "imgurl, "
                + "description, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER "
                + "FROM categories "
                + "WHERE PARENTID IS NULL "
                + "ORDER BY NAME", null, CategoryInfo.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public final SentenceList getSuppList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "NAME "
                + "FROM suppliers "
                + "ORDER BY NAME", null, (DataRead dr) -> new SupplierInfo(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxCustCategoriesList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcustcategories "
                + "ORDER BY NAME", null, (DataRead dr) -> new TaxCustCategoryInfo(
                        dr.getString(1),
                        dr.getString(2)));
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final CustomerInfoExt getCustomerInfo(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers WHERE ID = ?", SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(id);
    }

    /**
     * JG Apr 2017 - Revised to return Customer Id - cId param
     *
     * @param cId
     * @return
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<CustomerTransaction> getCustomersTransactionList(String cId) throws BasicException {

        return new PreparedSentence(s,
                "SELECT tickets.TICKETID, "
                + "products.NAME AS PNAME, "
                + "SUM(ticketlines.UNITS) AS UNITS, "
                + "SUM(ticketlines.UNITS * ticketlines.PRICE) AS AMOUNT, "
                + "SUM(ticketlines.UNITS * ticketlines.PRICE * (1.0 + taxes.RATE)) AS TOTAL, "
                + "receipts.DATENEW, "
                + "customers.ID AS CID "
                + "FROM ((((ticketlines ticketlines "
                + "CROSS JOIN taxes taxes ON (ticketlines.TAXID = taxes.ID)) "
                + "INNER JOIN tickets tickets ON (tickets.ID = ticketlines.TICKET)) "
                + "INNER JOIN customers customers ON (customers.ID = tickets.CUSTOMER)) "
                + "INNER JOIN receipts receipts ON (tickets.ID = receipts.ID)) "
                + "LEFT OUTER JOIN products products ON (ticketlines.PRODUCT = products.ID) "
                + "WHERE tickets.CUSTOMER = ? "
                + "GROUP BY customers.ID, receipts.DATENEW, tickets.TICKETID, "
                + "products.NAME, tickets.TICKETTYPE "
                + "ORDER BY receipts.DATENEW DESC",
                SerializerWriteString.INSTANCE,
                CustomerTransaction.getSerializerRead()).list(cId);
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxCategoriesList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcategories "
                + "ORDER BY NAME", null, (DataRead dr) -> new TaxCategoryInfo(dr.getString(1), dr
                        .getString(2)));
    }

    /**
     *
     * @return
     */
    public final SentenceList getAttributeSetList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM attributeset "
                + "ORDER BY NAME", null, (DataRead dr) -> new AttributeSetInfo(dr.getString(1), dr
                        .getString(2)));
    }

    public final SentenceList getAttributeSet() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM attributeset "
                + "WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> new AttributeSetInfo(dr.getString(1), dr.getString(2)));
    }

    public final SentenceList getAttributeUseList(String attributeSetId) {
        return new StaticSentence(s,
                "SELECT id, attributeset_id, attribute_id FROM attributeuse WHERE attributeset_id = '" + attributeSetId + "'",
                null,
                (DataRead dr) -> new AttrUseInfo(dr.getString(1), dr.getString(2), dr.getString(3)));
    }

    public final SentenceList getAttributeValueList(String[] attributeIds) {
        final String ids = Arrays.stream(attributeIds)
                .map((s) -> format("'%s'", s))
                .collect(joining(", ", "(", ")"));
        return new StaticSentence(s,
                "SELECT id, attribute_id, value FROM attributevalue WHERE attribute_id  IN " + ids,
                null,
                (DataRead dr) -> new AttrValueInfo(dr.getString(1), dr.getString(2), dr.getString(3)));
    }

    public final SentenceList getAttributeList(String[] attributeIds) {
        final String ids = Arrays.stream(attributeIds)
                .map((s) -> format("'%s'", s))
                .collect(joining(", ", "(", ")"));
        return new PreparedSentence(s,
                "SELECT id, name FROM attribute WHERE id  IN " + ids,
                null,
                (DataRead dr) -> new AttributeInfo(dr.getString(1), dr.getString(2)));
    }

    public final SentenceExec addAttributeInstance() {
        return new PreparedSentence(s,
                "INSERT INTO attributeinstance (ID, ATTRIBUTESETINSTANCE_ID, ATTRIBUTE_ID, VALUE,SITEGUID) VALUES (?, ?, ?, ?, ?)",
                new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING));
    }

    public PreparedSentence findAttributeSetInstanceId() {
        return new PreparedSentence(s,
                "SELECT ID FROM attributesetinstance WHERE ATTRIBUTESET_ID = ? AND DESCRIPTION = ?",
                new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                SerializerReadString.INSTANCE);
    }

    public final SentenceExec addAttributeSetInstance() {
        return new PreparedSentence(s,
                "INSERT INTO attributesetinstance (ID, ATTRIBUTESET_ID, DESCRIPTION,SITEGUID) VALUES (?, ?, ?, ?)",
                new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING));
    }

    public final PreparedSentence getAttributedStockProductForAttributeValues(String[] attributeValueIds) {
        final String valueIds = Arrays.stream(attributeValueIds)
                .map((s) -> format("'%s'", s))
                .collect(joining(", ", "(", ")"));
        return new PreparedSentence(s,
                "SELECT "
                + "attr_s.id, "
                + "attr_s.name, "
                + "product_id, "
                + "attributeset_id, "
                + "quantity, "
                + "price, "
                + "a.name, "
                + "attr_s.countable, "
                + "order_qty, "
                + "min_qty, "
                + "max_qty "
                + "FROM attributed_stock attr_s "
                + "JOIN attributeset a ON attr_s.attributeset_id = a.id "
                + "WHERE attr_s.attributeset_id = ? "
                + "AND attr_s.id IN "
                + "(SELECT s_attr.attr_stock_id "
                + "FROM attributed_stock_attributes s_attr "
                + "WHERE s_attr.attr_value_id IN " + valueIds + " AND product_id = ? "
                + "GROUP BY s_attr.attr_stock_id "
                + "HAVING COUNT(distinct s_attr.attr_value_id) = " + attributeValueIds.length + ") ",
                SerializerWriteString.INSTANCE,
                AttributedProductStock::readValueWithInventoryInformation);
    }

    public final PreparedSentence getAttributedProductStock() {
        return new PreparedSentence(s,
                "SELECT "
                + "attr_s.id, "
                + "attr_s.name, "
                + "product_id, "
                + "attributeset_id, "
                + "quantity, "
                + "price, "
                + "a.name as as_name, "
                + "attr_s.countable, "
                + "order_qty, "
                + "min_qty, "
                + "max_qty "
                + "FROM attributed_stock attr_s "
                + "JOIN attributeset a ON attr_s.attributeset_id = a.id  WHERE product_id = ? ",
                SerializerWriteString.INSTANCE,
                AttributedProductStock::readValueWithInventoryInformation);
    }

    public final PreparedSentence getAttributedProductStockById() {
        return new PreparedSentence(s,
                "SELECT "
                + "attr_s.id, "
                + "attr_s.name, "
                + "product_id, "
                + "attributeset_id, "
                + "quantity, "
                + "price, "
                + "a.name as as_name, "
                + "attr_s.countable, "
                + "order_qty, "
                + "min_qty, "
                + "max_qty "
                + "FROM attributed_stock attr_s "
                + "JOIN attributeset a ON attr_s.attributeset_id = a.id  WHERE attr_s.id = ? ",
                SerializerWriteString.INSTANCE,
                AttributedProductStock::readValueWithInventoryInformation);
    }

    public final List<AttributedProductStock> getAttributedProductStockByProductIds(final Collection<String> productIds) throws BasicException {
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        final String sqlIn = productIds
                .stream()
                .map((id) -> format("'%s'", id))
                .collect(joining(",", "(", ")"));
        return new StaticSentence(s,
                "SELECT "
                + "attr_s.id, "
                + "attr_s.name, "
                + "attr_s.product_id, "
                + "attr_s.attributeset_id, "
                + "attr_s.quantity, "
                + "attr_s.price, "
                + "a.name as as_name, "
                + "attr_s.countable, "
                + "attr_s.order_qty, "
                + "attr_s.min_qty, "
                + "attr_s.max_qty, "
                + "p.name "
                + "FROM attributed_stock attr_s "
                + "JOIN attributeset a ON attr_s.attributeset_id = a.id "
                + "JOIN products p ON p.id = attr_s.product_id "
                + "WHERE attr_s.product_id IN " + sqlIn,
                SerializerWriteString.INSTANCE,
                AttributedProductStock::readValueWithInventoryInformationAndProductName)
                .list();
    }

    public final PreparedSentence getAttributedProductStockByName() {
        return new PreparedSentence(s,
                "SELECT "
                + "attr_s.id, "
                + "attr_s.name, "
                + "product_id, "
                + "attributeset_id, "
                + "quantity, "
                + "price, "
                + "a.name as as_name, "
                + "attr_s.countable, "
                + "order_qty, "
                + "min_qty, "
                + "max_qty "
                + "FROM attributed_stock attr_s "
                + "JOIN attributeset a ON attr_s.attributeset_id = a.id "
                + "WHERE LOWER(attr_s.name) = LOWER(?) AND product_id = ? ",
                SerializerWriteString.INSTANCE,
                AttributedProductStock::readValueWithInventoryInformation);
    }

    public final AttributedProductStock findAttributedProductStock(final String attrProductStockId) throws BasicException {
        return (AttributedProductStock) new PreparedSentence(s,
                "SELECT "
                + "attr_s.id, "
                + "attr_s.name, "
                + "product_id, "
                + "attributeset_id, "
                + "quantity, "
                + "price, "
                + "a.name as as_name, "
                + "attr_s.countable "
                + "FROM attributed_stock attr_s "
                + "JOIN attributeset a ON attr_s.attributeset_id = a.id  WHERE attr_s.id = ? ",
                SerializerWriteString.INSTANCE,
                AttributedProductStock::readValue)
                .find(attrProductStockId);
    }

    public final SentenceExec updateAttributeProductStockOrderQty() {
        final Datas[] datas = {Datas.INT, Datas.INT, Datas.STRING};
        return new PreparedSentence(s,
                " UPDATE attributed_stock SET "
                + "order_qty = ?, "
                + "quantity = ? "
                + "WHERE id = ? ",
                new SerializerWriteBasicExt(datas, new int[]{0, 1, 2}));
    }

    public final SentenceExec changeAttributeProductStockOrderQty() {
        final Datas[] datas = {Datas.INT, Datas.INT, Datas.STRING};
        return new PreparedSentence(s,
                " UPDATE attributed_stock SET "
                + "order_qty = (order_qty + ?), "
                + "quantity = (quantity + ?) "
                + "WHERE id = ? ",
                new SerializerWriteBasicExt(datas, new int[]{0, 1, 2}));
    }

    public final SentenceList getAttributedProductStockAttributes() {
        return new PreparedSentence(s,
                "SELECT "
                + "asa.id, "
                + "attr_stock_id, "
                + "attr_value_id, "
                + "attribute_id, "
                + "value "
                + "FROM attributed_stock_attributes asa "
                + "JOIN attributevalue av ON asa.attr_value_id = av.id  WHERE attr_stock_id  = ?",
                SerializerWriteString.INSTANCE,
                AttributedProductStockAttribute::readValue);
    }

    /**
     *
     * @return
     */
    public final SentenceList getLocationsList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "ADDRESS FROM locations "
                + "ORDER BY NAME", null, new SerializerReadClass(LocationInfo.class));
    }

    /**
     *
     * @return
     */
    public final SentenceList getFloorsList() {
        return new StaticSentence(s, "SELECT ID, NAME FROM floors ORDER BY NAME", null, new SerializerReadClass(
                FloorsInfo.class));
    }

    public final SentenceList getTableArrangements() {
        return new StaticSentence(s,
                "SELECT id, name, width, length, image FROM table_arrangements ORDER BY name",
                null,
                new SerializerReadClass(TableArrangement.class));
    }

    public final TableArrangement getTableArrangement(final String id) {
        try {
            final List<TableArrangement> result = new PreparedSentence(s,
                    "SELECT id, name, width, length, image FROM table_arrangements where id = ? ORDER BY name",
                    SerializerWriteString.INSTANCE,
                    new SerializerReadClass(TableArrangement.class))
                    .list(id);
            return result
                    .stream()
                    .findFirst()
                    .orElse(null);
        } catch (BasicException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @return
     */
    public final SentenceList getFloorTablesList() {
        return new StaticSentence(s, "SELECT ID, NAME FROM places ORDER BY NAME", null, new SerializerReadClass(
                FloorsInfo.class));
    }

    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt findCustomerExt(String card) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "TAXID, "
                + "SEARCHKEY, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers "
                + "WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE() + " "
                + "ORDER BY NAME", SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(card);
    }

    /**
     *
     * @param name
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt findCustomerName(String name) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers "
                + "WHERE NAME = ? AND VISIBLE = " + s.DB.TRUE() + " "
                + "ORDER BY NAME", SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(name);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt loadCustomerExt(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers WHERE ID = ?", SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(id);
    }

    /**
     * Quick Customer create
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt qCustomerExt(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "MAXDEBT, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "ISVIP, "
                + "DISCOUNT "
                + "FROM customers WHERE ID = ?", SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final boolean isCashActive(String id) throws BasicException {

        return new PreparedSentence(s,
                "SELECT MONEY FROM closedcash WHERE DATEEND IS NULL AND MONEY = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id)
                != null;
    }

    /**
     *
     * @param tickettype
     * @param ticketid
     * @return
     * @throws BasicException
     */
    public final TicketInfo loadTicket(final int tickettype, final int ticketid) throws BasicException {
        TicketInfo ticket = (TicketInfo) new PreparedSentence(s, "SELECT "
                + "T.ID, "
                + "T.TICKETTYPE, "
                + "T.TICKETID, "
                + "R.DATENEW, "
                + "R.MONEY, "
                + "R.ATTRIBUTES, "
                + "P.ID, "
                + "P.NAME, "
                + "T.CUSTOMER, "
                + "T.STATUS "
                + "FROM receipts R "
                + "JOIN tickets T ON R.ID = T.ID "
                + "LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "WHERE T.TICKETTYPE = ? AND T.TICKETID = ? "
                + "ORDER BY R.DATENEW DESC",
                SerializerWriteParams.INSTANCE, new SerializerReadClass(
                        TicketInfo.class))
                .find(new DataParams() {

                    @Override
                    public void writeValues() throws BasicException {
                        setInt(1, tickettype);
                        setInt(2, ticketid);
                    }

                });

        if (ticket != null) {

            String customerid = ticket.getCustomerId();
            ticket.setCustomer(customerid == null
                    ? null
                    : loadCustomerExt(customerid));

            ticket.setLines(new PreparedSentence(s, "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, "
                    + "L.UNITS, L.PRICE,L.attrProductStockId, T.ID, T.NAME, T.CATEGORY, T.CUSTCATEGORY, "
                    + "T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES "
                    + "FROM ticketlines L, taxes T "
                    + "WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE",
                    SerializerWriteString.INSTANCE, new SerializerReadClass(
                            TicketLineInfo.class)).list(ticket.getId()));

            ticket.setPayments(new PreparedSentence(s,
                    "SELECT PAYMENT, TOTAL, TRANSID, TENDERED, CARDNAME FROM payments WHERE RECEIPT = ?" //                , "SELECT PAYMENT, TOTAL, TRANSID, TENDERED, CARDNAME, VOUCHER FROM payments WHERE RECEIPT = ?"
                    ,
                     SerializerWriteString.INSTANCE, new SerializerReadClass(PaymentInfoTicket.class)).list(ticket
                    .getId()));
        }
        return ticket;
    }

    //Getting the SITEGUID
    /**
     *
     * @return
     */
    public final SentenceList getSiteGuid() {
        return new StaticSentence(s, "SELECT "
                + "SITEGUID, "
                + "SFLAG, "
                + "LFLAG "
                + "FROM siteguid ",
                null, (DataRead dr) -> new SiteguidInfo(
                        dr.getString(1),
                        dr.getInt(2),
                        dr.getInt(3)
                ));
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void saveTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t;
        t = new Transaction(s) {

            @Override
            public Object transact() throws BasicException {

                // Set Receipt Id
                if (ticket.getTicketId() == 0) {
                    switch (ticket.getTicketType()) {
                        case TicketInfo.RECEIPT_NORMAL:
                            ticket.setTicketId(getNextTicketIndex());
                            break;
                        case TicketInfo.RECEIPT_REFUND:
                            ticket.setTicketId(getNextTicketRefundIndex());
                            break;
                        case TicketInfo.RECEIPT_PAYMENT:
                            ticket.setTicketId(getNextTicketPaymentIndex());
                            break;
                        case TicketInfo.RECEIPT_NOSALE:
                            ticket.setTicketId(getNextTicketPaymentIndex());
                            break;
                        default:
                            throw new BasicException();
                    }
                }

                new PreparedSentence(s,
                        "INSERT INTO receipts (ID, MONEY, DATENEW, ATTRIBUTES, PERSON, SITEGUID) VALUES (?, ?, ?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, ticket.getId());
                                setString(2, ticket.getActiveCash());
                                setTimestamp(3, ticket.getDate());

                                try {
                                    ByteArrayOutputStream o = new ByteArrayOutputStream();
                                    ticket.getProperties().storeToXML(o, AppLocal.APP_NAME, "UTF-8");
                                    setBytes(4, o.toByteArray());
                                } catch (IOException e) {
                                    setBytes(4, null);
                                }
                                setString(5, ticket.getProperty("person"));
                                setString(6, siteguid);
                            }

                        });
                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "I");
                                setString(3, "receipts");
                                setString(4, ticket.getId());
                                setString(5, "id");
                                setString(6, ticket.getActiveCash());
                                setString(7, "money");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });

                // new ticket
                new PreparedSentence(s, "INSERT INTO tickets (ID, TICKETTYPE, TICKETID, PERSON, CUSTOMER, STATUS, SITEGUID) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)", SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, ticket.getId());
                                setInt(2, ticket.getTicketType());
                                setInt(3, ticket.getTicketId());
                                setString(4, ticket.getUser().getId());
                                setString(5, ticket.getCustomerId());
                                setInt(6, ticket.getTicketStatus());
                                setString(7, siteguid);
                            }

                        });

                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "I");
                                setString(3, "tickets");
                                setString(4, ticket.getId());
                                setString(5, "id");
                                setString(6, String.valueOf(ticket.getTicketId()));
                                setString(7, "ticketid");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });

                // update status of existing ticket
                new PreparedSentence(s, "UPDATE tickets SET STATUS = ? "
                        + "WHERE TICKETTYPE = 0 AND TICKETID = ?", SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setInt(1, ticket.getTicketId());
                                setInt(2, ticket.getTicketStatus());
                            }

                        });

                SentenceExec ticketlineinsert = new PreparedSentence(s, "INSERT INTO ticketlines (TICKET, LINE, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE,cpp, TAXID,attrProductStockId, ATTRIBUTES,SITEGUID) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)",
                        SerializerWriteBuilder.INSTANCE);

                for (TicketLineInfo l : ticket.getLines()) {

                    if (l.getSiteguidID() == null) {
                        l.m_siteguid = siteguid;
                    }
                    if(l.getProductID() != null){
//                        if(l.getProductAttSetId()!= null){
//
//                        }
                    ProductInfoExt productprice = getProductInfo(l.getProductID());
                    l.m_productprice = productprice.m_dPriceSell;
                    }else{
                       l.m_productprice = 0.0;
                    }

                    ticketlineinsert.exec(l);

                    if (l.getProductID() != null && l.isProductService() != true) {
                        final StockDiaryUpdate stockDiaryUpdate = (productId, productAttrSetInstId, attrStockId, qty, price, siteguid) -> {
                            String id = UUID.randomUUID().toString();
                            try {
                                getStockDiaryInsert().exec(new Object[]{
                                    id,
                                    ticket.getDate(),
                                    qty < 0.0
                                    ? MovementReason.IN_REFUND.getKey()
                                    : MovementReason.OUT_SALE.getKey(),
                                    location,
                                    productId,
                                    productAttrSetInstId, -qty,
                                    price,
                                    ticket.getUser().getName(), attrStockId, siteguid
                                });

                                insertDatabaseChangeLog(new Object[]{
                                    UUID.randomUUID().toString(),
                                    "I",
                                    "stockdiary",
                                    id,
                                    "id", productId,
                                    "product",
                                    siteguid,
                                    0,
                                    0
                                });

                            } catch (BasicException ex) {
                                Logger.getLogger(DataLogicSales.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        };
// changes for reports
                        if (!l.isProductBundle()) {
                            stockDiaryUpdate.execute(l.getProductID(),
                                    l.getProductAttSetInstId(),
                                    l.getProductStockId(),
                                    l.getMultiply(),
                                    l.getPrice(), siteguid);

                        } else {

                            //get the bundle associated with the stocks, and insert each different stockdiary attr instance id.
                            final List<String> bundleAttrSetInstIds = l.getProductAttrSetInstIds();

                            logger.log(Level.INFO,
                                    "Adding stock diary for attr-set-instance ids: {0}",
                                    bundleAttrSetInstIds);

                            bundleAttrSetInstIds.forEach((attrSetInstanceId) -> {
                                //TODO, attrSetInstanceId should be matched one to one with productStockId from productStockIds
                                addAttrProductStockTicketLineInfo(l, attrSetInstanceId, stockDiaryUpdate);
                            });

                            //add product bundle instance id;
                            stockDiaryUpdate.execute(l.getProductID(), null, null, l.getMultiply(), l.getPrice(), siteguid);
                        }
                    }

                    new PreparedSentence(s,
                            "INSERT INTO databasechangelog (ID,PROCESS,TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                            SerializerWriteParams.INSTANCE)
                            .exec(new DataParams() {

                                @Override
                                public void writeValues() throws BasicException {
                                    setString(1, UUID.randomUUID().toString());
                                    setString(2, "I");
                                    setString(3, "ticketlines");
                                    setString(4, ticket.getId());
                                    setString(5, "ticket");
                                    setString(6, String.valueOf(l.getTicketLine()));
                                    setString(7, "line");
                                    setString(8, siteguid);
                                    setInt(9, 0);
                                    setInt(10, 0);
                                }
                            });
                }

                final Payments payments = new Payments();
                SentenceExec paymentinsert = new PreparedSentence(s,
                        "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, TRANSID, RETURNMSG, "
                        + "TENDERED, CARDNAME, VOUCHER, SITEGUID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE);

                ticket.getPayments().forEach((p) -> {
                    payments.addPayment(p.getName(), p.getTotal(), p.getPaid(), ticket.getReturnMessage(), p.getVoucher());
                });
                while (payments.getSize() >= 1) {
                    paymentinsert.exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            pName = payments.getFirstElement();
                            getTotal = payments.getPaidAmount(pName);
                            getTendered = payments.getTendered(pName);
                            getRetMsg = payments.getRtnMessage(pName);
                            getVoucher = payments.getVoucher(pName);
                            payments.removeFirst(pName);
                            payment_id = null;
                            payment_id = UUID.randomUUID().toString();
                            setString(1, payment_id);
                            setString(2, ticket.getId());
                            setString(3, pName);
                            setDouble(4, getTotal);
                            setString(5, ticket.getTransactionID());
                            setBytes(6, (byte[]) Formats.BYTEA.parseValue(getRetMsg));
                            setDouble(7, getTendered);
                            setString(8, getCardName);
                            setString(9, getVoucher);
                            setString(10, siteguid);
                            payments.removeFirst(pName);
                            new PreparedSentence(s,
                                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                                    SerializerWriteParams.INSTANCE)
                                    .exec(new DataParams() {

                                        @Override
                                        public void writeValues() throws BasicException {
                                            setString(1, UUID.randomUUID().toString());
                                            setString(2, "I");
                                            setString(3, "payments");
                                            setString(4, payment_id);
                                            setString(5, "id");
                                            setString(6, ticket.getId());
                                            setString(7, "receipt");
                                            setString(8, siteguid);
                                            setInt(9, 0);
                                            setInt(10, 0);
                                        }
                                    });

                        }

                    });

                    if (payments.getVoucher(pName) != null) {
                        getVoucherNonActive().exec(payments.getVoucher(pName));
                    }

                    if ("debt".equals(pName) || "debtpaid".equals(pName)) {
                        ticket.getCustomer().updateCurDebt(getTotal, ticket.getDate());
                        getDebtUpdate().exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setDouble(1, ticket.getCustomer().getAccdebt());
                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                setString(3, ticket.getCustomer().getId());
                            }

                        });
                    }
                }

                SentenceExec taxlinesinsert = new PreparedSentence(s,
                        "INSERT INTO taxlines (ID, RECEIPT, TAXID, BASE, AMOUNT, SITEGUID)  "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE);

                if (ticket.getTaxes() != null) {
                    for (final TicketTaxInfo tickettax : ticket.getTaxes()) {
                        taxlinesinsert.exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                tax_id = null;
                                tax_id = UUID.randomUUID().toString();
                                setString(1, tax_id);
                                setString(2, ticket.getId());
                                setString(3, tickettax.getTaxInfo().getId());
                                setDouble(4, tickettax.getSubTotal());
                                setDouble(5, tickettax.getTax());
                                setString(6, siteguid);
                            }

                        });
                        new PreparedSentence(s,
                                "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                                SerializerWriteParams.INSTANCE)
                                .exec(new DataParams() {

                                    @Override
                                    public void writeValues() throws BasicException {
                                        setString(1, UUID.randomUUID().toString());
                                        setString(2, "I");
                                        setString(3, "taxlines");
                                        setString(4, tax_id);
                                        setString(5, "id");
                                        setString(6, ticket.getId());
                                        setString(7, "receipt");
                                        setString(8, siteguid);
                                        setInt(9, 0);
                                        setInt(10, 0);
                                    }
                                });
                    }
                }
//            }

                return null;
            }

        };

        t.execute();
    }

    private void addAttrProductStockTicketLineInfo(final TicketLineInfo l,
            final String attrSetInstanceId,
            final StockDiaryUpdate stockDiaryUpdate) {
        try {
            final double qtyRatio = l.getProductAttrSetInstanceIdQtyRatio(attrSetInstanceId);
            final Double prodQty = l.getMultiply() * qtyRatio;
            final String attrProdStockId
                    = l.getProductAttrSetInstanceProductStockId(attrSetInstanceId);
            if (attrProdStockId == null) {
                logger.log(Level.SEVERE,
                        "No product stock id associated with attr set instance id: {0}",
                        attrProdStockId);
                return;
            }
            final AttributedProductStock productStock = (AttributedProductStock) getAttributedProductStockById()
                    .find(attrProdStockId);
            final String productId = productStock != null ? productStock.getProductId() : null;
            logger.log(Level.INFO,
                    "Inserting stock diary for set-instanceid ({0}, {1})",
                    new Object[]{attrSetInstanceId, qtyRatio});

            stockDiaryUpdate.execute(productId, attrSetInstanceId, l.getProductStockId(), prodQty, 0.0, siteguid);
        } catch (BasicException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void deleteTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t;
        t = new Transaction(s) {

            @Override
            public Object transact() throws BasicException {

                // update the inventory
                Date d = new Date();
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    String id = UUID.randomUUID().toString();
                    if (ticket.getLine(i).getProductID() != null) {

                        // Hay que actualizar el stock si el hay producto
                        getStockDiaryInsert().exec(new Object[]{
                            id,
                            d,
                            ticket.getLine(i).getMultiply() >= 0.0
                            ? MovementReason.IN_REFUND.getKey()
                            : MovementReason.OUT_SALE.getKey(),
                            location,
                            ticket.getLine(i).getProductID(),
                            ticket.getLine(i).getProductAttSetInstId(), ticket.getLine(i).getMultiply(), ticket.getLine(
                            i).getPrice(),
                            ticket.getUser().getName(), siteguid
                        });
                        insertDatabaseChangeLog(new Object[]{
                            UUID.randomUUID().toString(),
                            "I",
                            "stockdiary",
                            id,
                            "id", ticket.getLine(i).getProductID(),
                            "product",
                            siteguid,
                            0,
                            0
                        });
                    }
// Add test for productBundle
                    List<ProductsBundleInfo> bundle = getProductsBundle((String) ticket.getLine(i).getProductID());

                    if (bundle.size() > 0) {
                        for (ProductsBundleInfo bundleComponent : bundle) {
                            ProductInfoExt bundleProduct = getProductInfo(bundleComponent.getProductBundleId());

                            getStockDiaryInsert().exec(new Object[]{
                                UUID.randomUUID().toString(),
                                d,
                                ticket.getLine(i).getMultiply() * bundleComponent.getQuantity() >= 0.0
                                ? MovementReason.IN_REFUND.getKey()
                                : MovementReason.OUT_SALE.getKey(),
                                location,
                                bundleComponent.getProductBundleId(),
                                null, ticket.getLine(i).getMultiply() * bundleComponent.getQuantity(), bundleProduct
                                .getPriceSell(),
                                ticket.getUser().getName(), siteguid});

                            insertDatabaseChangeLog(new Object[]{
                                UUID.randomUUID().toString(),
                                "I",
                                "stockdiary",
                                id,
                                "id", ticket.getLine(i).getProductID(),
                                "product",
                                siteguid,
                                0,
                                0
                            });
                        }
                    }
// End test
                }

                // update customer debts
                for (PaymentInfo p : ticket.getPayments()) {
                    if ("debt".equals(p.getName()) || "debtpaid".equals(p.getName())) {

                        // udate customer fields...
                        ticket.getCustomer().updateCurDebt(-p.getTotal(), ticket.getDate());

                        // save customer fields...
                        getDebtUpdate().exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setDouble(1, ticket.getCustomer().getAccdebt());
                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                setString(3, ticket.getCustomer().getId());
                            }

                        });
                    }
                }

                // and delete the receipt
                new StaticSentence(s, "DELETE FROM taxlines WHERE RECEIPT = ?", SerializerWriteString.INSTANCE).exec(
                        ticket.getId());
                new StaticSentence(s, "DELETE FROM payments WHERE RECEIPT = ?", SerializerWriteString.INSTANCE).exec(
                        ticket.getId());
                new StaticSentence(s, "DELETE FROM ticketlines WHERE TICKET = ?", SerializerWriteString.INSTANCE).exec(
                        ticket.getId());
                new StaticSentence(s, "DELETE FROM tickets WHERE ID = ?", SerializerWriteString.INSTANCE).exec(ticket
                        .getId());
                new StaticSentence(s, "DELETE FROM receipts WHERE ID = ?", SerializerWriteString.INSTANCE).exec(ticket
                        .getId());
                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "D");
                                setString(3, "taxlines");
                                setString(4, ticket.getId());
                                setString(5, "RECEIPT");
                                setString(6, null);
                                setString(7, "null");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });
                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "D");
                                setString(3, "payments");
                                setString(4, ticket.getId());
                                setString(5, "RECEIPT");
                                setString(6, null);
                                setString(7, "null");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });
                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "D");
                                setString(3, "ticketlines");
                                setString(4, ticket.getId());
                                setString(5, "TICKET");
                                setString(6, null);
                                setString(7, "null");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });
                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "D");
                                setString(3, "tickets");
                                setString(4, ticket.getId());
                                setString(5, "ID");
                                setString(6, null);
                                setString(7, "null");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });
                new PreparedSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, "D");
                                setString(3, "receipts");
                                setString(4, ticket.getId());
                                setString(5, "ID");
                                setString(6, null);
                                setString(7, "null");
                                setString(8, siteguid);
                                setInt(9, 0);
                                setInt(10, 0);
                            }
                        });
                return null;
            }

        };
        t.execute();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextPickupIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "pickup_number").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketRefundIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum_refund").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum_payment").find();
    }

// JG 3 Feb 16 - Product load speedup
    public final SentenceFind getProductImage() {
        return new PreparedSentence(s,
                "SELECT IMAGE FROM products WHERE ID = ?",
                SerializerWriteString.INSTANCE, (DataRead dr) -> ImageUtils
                        .readImage(dr.getBytes(1)));
    }

    /**
     * Loads on ProductsEditor
     *
     * @return
     */
    public final SentenceList getProductCatQBF() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.CATEGORY, "
                + "P.TAXCAT, "
                + "P.ATTRIBUTESET_ID, "
                + "P.STOCKCOST, "
                + "P.STOCKVOLUME, "
                // JG 3 feb 16 speedup  + "P.IMAGE, "
                + s.DB.CHAR_NULL() + ","
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.ISCONSTANT, "
                + "P.PRINTKB, "
                + "P.SENDSTATUS, "
                + "P.ISSERVICE, "
                + "P.ATTRIBUTES, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.PRINTTO, "
                + "P.SUPPLIER, "
                + "P.UOM, "
                + "P.MEMODATE,"
                + "P.imgurl,"
                + "p.description, "
                + "CASE WHEN "
                + "C.PRODUCT IS NULL "
                + "THEN " + s.DB.FALSE()
                + " ELSE " + s.DB.TRUE()
                + " END, "
                + "C.CATORDER, "
                + "P.SITEGUID "
                + "FROM products P LEFT OUTER JOIN products_cat C "
                + "ON P.ID = C.PRODUCT "
                + "WHERE ?(QBF_FILTER) "
                + "ORDER BY P.REFERENCE",
                new String[]{
                    "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), productsRow.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatInsert() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s, "INSERT INTO products ("
                        + "ID, "
                        + "REFERENCE, "
                        + "CODE, "
                        + "CODETYPE, "
                        + "NAME, "
                        + "PRICEBUY, "
                        + "PRICESELL, "
                        + "CATEGORY, "
                        + "TAXCAT, "
                        + "ATTRIBUTESET_ID, "
                        + "STOCKCOST, "
                        + "STOCKVOLUME, "
                        + "IMAGE, "
                        + "ISCOM, "
                        + "ISSCALE, "
                        + "ISCONSTANT, "
                        + "PRINTKB, "
                        + "SENDSTATUS, "
                        + "ISSERVICE, "
                        + "ATTRIBUTES, "
                        + "DISPLAY, "
                        + "ISVPRICE, "
                        + "ISVERPATRIB, "
                        + "TEXTTIP, "
                        + "WARRANTY, "
                        + "STOCKUNITS, "
                        + "PRINTTO, "
                        + "SUPPLIER, "
                        + "UOM, "
                        + "MEMODATE,"
                        + "imgurl,"
                        + "description, "
                        + "SITEGUID ) "
                        + "VALUES ("
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?,?,?,?)", new SerializerWriteBasicExt(productsRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5, 6,
                                    7, 8, 9, 10, 11, 12,
                                    13, 14, 15, 16, 17, 18,
                                    19, 20, 21, 22, 23, 24,
                                    25, 26, 27, 28, 29, 30,31,34}))
                        .exec(params);

                if (i > 0) {
                    new PreparedSentence(s, "INSERT INTO `databasechangelog` "
                            + "(`id`,`process`,`tablename`,`table_pk_id`,"
                            + "`tablepk_name`,`table_scnd_id`,`table_scnd_name`,`siteguid`,sflag,lflag)"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?)",
                            new SerializerWriteBasicExt(productsRowDataBase.getDatas(), new int[]{41, 35, 36, 0, 37, 7, 38, 34, 39, 40}))
                            .exec(params);

                    if (((Boolean) values[32])) {
                        new PreparedSentence(s, "INSERT INTO products_cat (PRODUCT, CATORDER,SITEGUID) VALUES (?, ?,?)",
                                new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0, 33, 34}))
                                .exec(params);
                        Object[] arrayParam = new Object[10];
                        arrayParam[0] = UUID.randomUUID().toString();
                        arrayParam[1] = "I";
                        arrayParam[2] = "products_cat";
                        arrayParam[3] = values[0];
                        arrayParam[4] = "product";
                        arrayParam[5] = values[34];
                        arrayParam[6] = "siteguid";
                        arrayParam[7] = values[34];
                        arrayParam[8] = values[39];
                        arrayParam[9] = values[40];
                        insertDatabaseChangeLog(arrayParam);
                    }

                    if (values.length > 42) {
                        final Object[] attributedStocks = (Object[]) values[42];
                        final String productId = (String) values[0];
                        addAttributedStocks(productId, attributedStocks);
                    }
                }
                return i;
            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatUpdate() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s, "UPDATE products SET "
                        + "ID = ?, "
                        + "REFERENCE = ?, "
                        + "CODE = ?, "
                        + "CODETYPE = ?, "
                        + "NAME = ?, "
                        + "PRICEBUY = ?, "
                        + "PRICESELL = ?, "
                        + "CATEGORY = ?, "
                        + "TAXCAT = ?, "
                        + "ATTRIBUTESET_ID = ?, "
                        + "STOCKCOST = ?, "
                        + "STOCKVOLUME = ?, "
                        + "IMAGE = ?, "
                        + "ISCOM = ?, "
                        + "ISSCALE = ?, "
                        + "ISCONSTANT = ?, "
                        + "PRINTKB = ?, "
                        + "SENDSTATUS = ?, "
                        + "ISSERVICE = ?,  "
                        + "ATTRIBUTES = ?,"
                        + "DISPLAY = ?, "
                        + "ISVPRICE = ?, "
                        + "ISVERPATRIB = ?, "
                        + "TEXTTIP = ?, "
                        + "WARRANTY = ?, "
                        + "STOCKUNITS = ?, "
                        + "PRINTTO = ?, "
                        + "SUPPLIER = ?, "
                        + "UOM = ?, "
                        + "MEMODATE = ?, "
                        + "imgurl = ?, "
                        + "description =  ?, "
                        + "SITEGUID = ? "
                        + "WHERE ID = ?", new SerializerWriteBasicExt(productsRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5,
                                    6, 7, 8, 9, 10,
                                    11, 12, 13, 14, 15,
                                    16, 17, 18, 19, 20,
                                    21, 22, 23, 24, 25,
                                    26, 27, 28, 29,30,31,34, 0}))
                        .exec(params);
                if (i > 0) {
                    new PreparedSentence(s, "INSERT INTO `databasechangelog` "
                            + "(`id`,`process`,`tablename`,`table_pk_id`,"
                            + "`tablepk_name`,`table_scnd_id`,`table_scnd_name`,`siteguid`,sflag,lflag)"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?)",
                            new SerializerWriteBasicExt(productsRowDataBase.getDatas(), new int[]{41, 35, 36, 0, 37, 7, 38, 34, 39, 40}))
                            .exec(params);
                    if (((Boolean) values[32])) {
                        if (new PreparedSentence(s, "UPDATE products_cat SET CATORDER = ? WHERE PRODUCT = ?",
                                new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{33, 0}))
                                .exec(params) == 0) {
                            new PreparedSentence(s, "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                                    new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0, 33}))
                                    .exec(params);
                            Object[] arrayParam = new Object[10];
                            arrayParam[0] = UUID.randomUUID().toString();
                            arrayParam[1] = "I";
                            arrayParam[2] = "products_cat";
                            arrayParam[3] = values[0];
                            arrayParam[4] = "product";
                            arrayParam[5] = values[34];
                            arrayParam[6] = "siteguid";
                            arrayParam[7] = values[34];
                            arrayParam[8] = values[39];
                            arrayParam[9] = values[40];
                            insertDatabaseChangeLog(arrayParam);
                        }
                    } else {
                        new PreparedSentence(s, "DELETE FROM products_cat WHERE PRODUCT = ?",
                                new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0})).exec(
                                params);
                        Object[] arrayParam = new Object[10];
                            arrayParam[0] = UUID.randomUUID().toString();
                            arrayParam[1] = "D";
                            arrayParam[2] = "products_cat";
                            arrayParam[3] = values[0];
                            arrayParam[4] = "product";
                            arrayParam[5] = values[34];
                            arrayParam[6] = "siteguid";
                            arrayParam[7] = values[34];
                            arrayParam[8] = values[39];
                            arrayParam[9] = values[40];
                            insertDatabaseChangeLog(arrayParam);
                    }

                    if (values.length > 42) {
                        final Object[] attributedStocks = (Object[]) values[42];
                        final String productId = (String) values[0];
                        addAttributedStocks(productId, attributedStocks);
                    }
                }
                return i;
            }

        };
    }

    private void addAttributedStocks(final String productId, final Object[] attributedStocks) throws BasicException {

        logger.log(Level.INFO,
                "Product contains product attributes. Count={0}",
                attributedStocks.length);

        if (attributedStocks.length > 0) {
            for (Object ob : attributedStocks) {
                final Entry<AttributedProductStock, List<AttributedProductStockAttribute>> pStock
                        = (Entry<AttributedProductStock, List<AttributedProductStockAttribute>>) ob;
                final AttributedProductStock stock = pStock.getKey();
                if (stock.isNewStock()) {
                    addProductStock(productId, stock);
                } else if (stock.isMarkedForDelete()) {
                    deleteProductStock(stock);
                } else {
                    updateProductStock(stock);
                }

                for (AttributedProductStockAttribute attribute : pStock.getValue()) {
                    if (attribute.isMarkedForDelete()) {
                        deleteProductStockattribute(attribute);
                    } else if (attribute.isNewAttribute()) {
                        addProductStockAttribute(stock.getId(), attribute);
                    } else {
                        updateProductStockattribute(attribute);
                    }
                }
            }
        }
    }

    private void addProductStock(final String productId, AttributedProductStock stock) throws BasicException {
        final Object[] data = new Object[8];
        data[0] = stock.getId();
        data[1] = stock.getName();
        data[2] = productId;
        data[3] = stock.getAttributeSetId();
        data[4] = stock.getQuantity();
        data[5] = stock.getPrice();
        data[6] = stock.isCountable();
        data[7] = siteguid;

        final Object[] database = new Object[10];
        database[0] = UUID.randomUUID().toString();
        database[1] = "I";
        database[2] = "attributed_stock";
        database[3] = stock.getId();
        database[4] = "id";
        database[5] = stock.getAttributeSetId();
        database[6] = "attributeset_id";
        database[7] = siteguid;
        database[8] = 0;
        database[9] = 0;

        final Datas[] datas = new Datas[8];
        datas[0] = Datas.STRING;
        datas[1] = Datas.STRING;
        datas[2] = Datas.STRING;
        datas[3] = Datas.STRING;
        datas[4] = Datas.INT;
        datas[5] = Datas.DOUBLE;
        datas[6] = Datas.BOOLEAN;
        datas[7] = Datas.STRING;

        int i = new PreparedSentence(s,
                "INSERT INTO attributed_stock (id, name, product_id, attributeset_id, quantity, price, countable, siteguid) "
                + "VALUES (?, ?, ?, ?, ?, ? ,?,?)",
                new SerializerWriteBasicExt(datas, new int[]{0, 1, 2, 3, 4, 5, 6, 7}))
                .exec(data);
        //databasechangelog
        if (i > 0) {
            insertDatabaseChangeLog(database);
        }
    }

    private void updateProductStock(AttributedProductStock stock) throws BasicException {
        final Object[] data = new Object[7];
        data[0] = stock.getName();
        data[1] = stock.getAttributeSetId();
        data[2] = stock.getQuantity();
        data[3] = stock.getPrice();
        data[4] = stock.isCountable();
        data[5] = stock.getId();
        data[6] = siteguid;

        final Object[] database = new Object[10];
        database[0] = UUID.randomUUID().toString();
        database[1] = "U";
        database[2] = "attributed_stock";
        database[3] = stock.getId();
        database[4] = "id";
        database[5] = stock.getAttributeSetId();
        database[6] = "attributeset_id";
        database[7] = siteguid;
        database[8] = 0;
        database[9] = 0;

        final Datas[] datas = new Datas[7];
        datas[0] = Datas.STRING;
        datas[1] = Datas.STRING;
        datas[2] = Datas.INT;
        datas[3] = Datas.DOUBLE;
        datas[4] = Datas.BOOLEAN;
        datas[5] = Datas.STRING;
        datas[6] = Datas.STRING;
        int i = new PreparedSentence(s,
                " UPDATE attributed_stock SET "
                + "name = ?, "
                + "attributeset_id = ?, "
                + "quantity = ?, "
                + "price = ?, "
                + "countable = ?, "
                + "siteguid = ? "
                + "WHERE id = ? ",
                new SerializerWriteBasicExt(datas, new int[]{0, 1, 2, 3, 4, 6, 5}))
                .exec(data);

//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! attributed stock " + i);
        //databasechangelog
        if (i > 0) {
            insertDatabaseChangeLog(database);
        }

    }

    private void deleteProductStock(AttributedProductStock productStock) throws BasicException {
        final Object[] data = new Object[1];
        data[0] = productStock.getId();

        final Datas[] datas = new Datas[2];
        datas[0] = Datas.STRING;
        new PreparedSentence(s,
                "DELETE FROM attributed_stock WHERE id = ?",
                new SerializerWriteBasicExt(datas, new int[]{0}))
                .exec(data);
    }

    private void addProductStockAttribute(final String stockId, AttributedProductStockAttribute attribute) throws BasicException {
        final Object[] data = new Object[4];
        data[0] = attribute.getId();
        data[1] = stockId;
        data[2] = attribute.getAttributeValueId();
        data[3] = siteguid;

        final Object[] database = new Object[10];
        database[0] = UUID.randomUUID().toString();
        database[1] = "I";
        database[2] = "attributed_stock_attributes";
        database[3] = attribute.getId();
        database[4] = "id";
        database[5] = attribute.getAttributeValueId();
        database[6] = "attr_value_id";
        database[7] = siteguid;
        database[8] = 0;
        database[9] = 0;

        final Datas[] datas = new Datas[4];
        datas[0] = Datas.STRING;
        datas[1] = Datas.STRING;
        datas[2] = Datas.STRING;
        datas[3] = Datas.STRING;
        int i = new PreparedSentence(s,
                "INSERT INTO attributed_stock_attributes (id, attr_stock_id, attr_value_id,siteguid) "
                + "VALUES (?, ?, ?, ?)",
                new SerializerWriteBasicExt(datas, new int[]{0, 1, 2, 3}))
                .exec(data);
        if (i > 0) {
            insertDatabaseChangeLog(database);
        }

    }

    private void updateProductStockattribute(AttributedProductStockAttribute attribute) throws BasicException {
        final Object[] data = new Object[3];
        data[0] = attribute.getAttributeValueId();
        data[1] = attribute.getId();
        data[2] = siteguid;

        final Object[] database = new Object[10];
        database[0] = UUID.randomUUID().toString();
        database[1] = "U";
        database[2] = "attributed_stock_attributes";
        database[3] = attribute.getId();
        database[4] = "id";
        database[5] = attribute.getAttributeValueId();
        database[6] = "attr_value_id";
        database[7] = siteguid;
        database[8] = 0;
        database[9] = 0;

        final Datas[] datas = new Datas[3];
        datas[0] = Datas.STRING;
        datas[1] = Datas.STRING;
        datas[2] = Datas.STRING;
        int i = new PreparedSentence(s,
                "UPDATE attributed_stock_attributes SET attr_value_id = ?, siteguid = ? "
                + "WHERE id = ?",
                new SerializerWriteBasicExt(datas, new int[]{0, 2, 1}))
                .exec(data);

        if (i > 0) {
            insertDatabaseChangeLog(database);
        }
    }

    private void deleteProductStockattribute(AttributedProductStockAttribute attribute) throws BasicException {
        final Object[] data = new Object[1];
        data[0] = attribute.getId();

        final Datas[] datas = new Datas[2];
        datas[0] = Datas.STRING;
        new PreparedSentence(s,
                "DELETE FROM attributed_stock_attributes WHERE id = ?",
                new SerializerWriteBasicExt(datas, new int[]{0}))
                .exec(data);
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatDelete() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
              int j =  new PreparedSentence(s, "DELETE FROM products_cat WHERE PRODUCT = ?", new SerializerWriteBasicExt(
                        productsRow.getDatas(), new int[]{0})).exec(params);
              if(j>0){
                            Object[] arrayParam = new Object[10];
                            arrayParam[0] = UUID.randomUUID().toString();
                            arrayParam[1] = "D";
                            arrayParam[2] = "products_cat";
                            arrayParam[3] = values[0];
                            arrayParam[4] = "product";
                            arrayParam[5] = values[32];
                            arrayParam[6] = "siteguid";
                            arrayParam[7] = values[32];
                            arrayParam[8] = values[38];
                            arrayParam[9] = values[39];
                            insertDatabaseChangeLog(arrayParam);
              }

                int i = new PreparedSentence(s, "DELETE FROM products WHERE ID = ?", new SerializerWriteBasicExt(
                        productsRow.getDatas(), new int[]{0})).exec(params);
                if (i > 0) {
                    new PreparedSentence(s, "INSERT INTO `databasechangelog` "
                            + "(`id`,`process`,`tablename`,`table_pk_id`,"
                            + "`tablepk_name`,`table_scnd_id`,`table_scnd_name`,`siteguid`,sflag,lflag)"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?)",
                            new SerializerWriteBasicExt(productsRowDataBase.getDatas(), new int[]{40, 34, 35, 0, 36, 7, 37, 32, 38, 39}))
                            .exec(params);
                }
                return i;
            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getDebtUpdate() {

        return new PreparedSentence(s, "UPDATE customers SET CURDEBT = ?, CURDATE = ? WHERE ID = ?",
                SerializerWriteParams.INSTANCE);
    }

    public String getstockLineNumber() throws BasicException {
        return stln.list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");
    }

    /**
     * ProductBundle version
     *
     * @return
     */
    public final SentenceExec getStockDiaryInsert() {
        return new SentenceExecTransaction(s) {

            @Override
            /**
             * @param params[0] String STOCKDIARY.ID
             * @param params[1] Date Timestamp
             * @param params[2] Integer Reason
             * @param params[3] String Location
             * @param params[4] String Product ID
             * @param params[5] String Attribute instance ID
             * @param params[6] Double Units
             * @param params[7] Double Price
             * @param params[8] String Application User
             */
            public int execInTransaction(Object params) throws BasicException {

                Object[] adjustParams = new Object[12];
                Object[] paramsArray = (Object[]) params;
                adjustParams[0] = paramsArray[4];                               //product ->Location
                adjustParams[1] = paramsArray[3];                               //location -> Product
                adjustParams[2] = paramsArray[5];                               //attributesetinstance
                adjustParams[3] = paramsArray[6];                               //units
                //databasechnagelog
                adjustParams[4] = siteguid;                               //siteguid
                adjustParams[5] = paramsArray[0];                               //id
                adjustParams[6] = "I";
                adjustParams[7] = "U";
                adjustParams[8] = "stockcurrent";
                adjustParams[9] = "location";
                adjustParams[10] = "product";
                adjustParams[11] = 0;

                adjustStock(adjustParams);

                int i = new PreparedSentence(s, "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE, AppUser,attr_stock_id, SITEGUID ) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new SerializerWriteBasicExt(
                                stockdiaryDatas, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}))
                        .exec(params);

                return i;

            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryInsert1() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                final Object[] objParams = (Object[]) params;
                int updateresult = objParams[5] == null
                        ? new PreparedSentence(s, "UPDATE stockcurrent SET UNITS = (UNITS + ?), SITEGUID = ? "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID IS NULL", new SerializerWriteBasicExt(
                                        stockdiaryDatas, new int[]{6, 12, 3, 4})).exec(params)
                        : new PreparedSentence(s, "UPDATE stockcurrent SET UNITS = (UNITS + ?), SITEGUID = ? "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID = ?", new SerializerWriteBasicExt(
                                        stockdiaryDatas, new int[]{6, 12, 3, 4, 5})).exec(params);

                if (updateresult == 1) {
                 int linenumber = ((Object[]) params)[5] == null
                    ? findStockLineNumber(((Object[]) params)[3].toString(),((Object[]) params)[4].toString(),null)
                    :findStockLineNumber(((Object[]) params)[3].toString(),((Object[]) params)[4].toString(),((Object[]) params)[5].toString());

            new PreparedSentence(s,
                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                    SerializerWriteParams.INSTANCE)
                    .exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, "U");
                            setString(3, "stockcurrent");
                            setString(4, Integer.toString(linenumber));
                            setString(5, "linenumber");
                            setString(6, siteguid);
                            setString(7, "siteguid");
                            setString(8, siteguid);
                            setInt(9, 0);
                            setInt(10, 0);
                        }
                    });
        }

                if (objParams[11] != null) {
                    final Object[] updateData = {objParams[6], objParams[11]};
                    final Datas[] types = {Datas.DOUBLE, Datas.STRING};
                    new PreparedSentence(s,
                            "UPDATE attributed_stock SET quantity = (quantity + ?) WHERE id = ?",
                            new SerializerWriteBasicExt(types, new int[]{0, 1}))
                            .exec(updateData);
                    new StaticSentence(s,
                            "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                            new SerializerWriteBasicExt(stockdiaryDatasDatabse, new int[]{26, 14, 18, 12, 20, 4, 21, 11, 24, 25}))
                            .exec(params);
                }

                if (updateresult == 0) {
                    new PreparedSentence(s, "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS, SITEGUID ) "
                            + "VALUES (?, ?, ?, ?, ?)", new SerializerWriteBasicExt(stockdiaryDatas,
                                    new int[]{3, 4, 5, 6, 12}))
                            .exec(params);

                      new PreparedSentence(s,
                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                    SerializerWriteParams.INSTANCE)
                    .exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, "I");
                            setString(3, "stockcurrent");
                            setString(4, getstockLineNumber());
                            setString(5, "linenumber");
                            setString(6, siteguid);
                            setString(7, "siteguid");
                            setString(8, siteguid);
                            setInt(9, 0);
                            setInt(10, 0);
                        }
                    });




                }

//                final String attrStockId = (String) (objParams[11] != null ? ((Object[]) objParams[11])[1] : null);
//                final Object[] stockParams = new Object[13];
//                System.arraycopy(objParams, 0, stockParams, 0, 11);
//                stockParams[11] = attrStockId;
                new StaticSentence(s,
                        "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                        new SerializerWriteBasicExt(stockdiaryDatasDatabse, new int[]{0, 15, 16, 19, 20, 4, 22, 12, 24, 25}))
                        .exec(params);

                return new PreparedSentence(s, "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, "
                        + "SUPPLIER, SUPPLIERDOC, ATTR_STOCK_ID,SITEGUID) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new SerializerWriteBasicExt(
                                stockdiaryDatas,
                                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})).exec(params);

            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryDelete() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null // if ATTRIBUTESETINSTANCE_ID is null
                        ? new PreparedSentence(s, "UPDATE stockcurrent SET UNITS = (UNITS - ?) , SITEGUID = ? "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID IS NULL", new SerializerWriteBasicExt(
                                        stockdiaryDatas, new int[]{6, 3, 4})).exec(params)
                        : new PreparedSentence(s, "UPDATE stockcurrent SET UNITS = (UNITS - ?) , SITEGUID = ? "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID = ?", new SerializerWriteBasicExt(
                                        stockdiaryDatas, new int[]{6, 3, 4, 5})).exec(params);

                if (updateresult == 1) {

               int linenumber = ((Object[]) params)[5] == null
                    ? findStockLineNumber(((Object[]) params)[3].toString(),((Object[]) params)[4].toString(),null)
                    :findStockLineNumber(((Object[]) params)[3].toString(),((Object[]) params)[4].toString(),((Object[]) params)[5].toString());

            new PreparedSentence(s,
                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                    SerializerWriteParams.INSTANCE)
                    .exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, "U");
                            setString(3, "stockcurrent");
                            setString(4, Integer.toString(linenumber));
                            setString(5, "linenumber");
                            setString(6, siteguid);
                            setString(7, "siteguid");
                            setString(8, siteguid);
                            setInt(9, 0);
                            setInt(10, 0);
                        }
                    });
                }

             if (updateresult == 0) {
                    new PreparedSentence(s, "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS,siteguid) "
                            + "VALUES (?, ?, ?, -(?),?)", new SerializerWriteBasicExt(stockdiaryDatas,
                                    new int[]{3, 4, 5, 6, 12}))
                            .exec(params);

                    new PreparedSentence(s,
                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                    SerializerWriteParams.INSTANCE)
                    .exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, "I");
                            setString(3, "stockcurrent");
                            setString(4, getstockLineNumber());
                            setString(5, "linenumber");
                            setString(6, siteguid);
                            setString(7, "siteguid");
                            setString(8, siteguid);
                            setInt(9, 0);
                            setInt(10, 0);
                        }
                    });
                }
                return new PreparedSentence(s, "DELETE FROM stockdiary WHERE ID = ?", new SerializerWriteBasicExt(
                        stockdiaryDatas, new int[]{0})).exec(params);
            }

        };
    }

    private void adjustStock(Object params) throws BasicException {
        List<ProductsBundleInfo> bundle = getProductsBundle((String) ((Object[]) params)[0]);

        if (bundle.size() > 0) {
//            int as=0;
            bundle.forEach((component) -> {
                Object[] adjustParams = new Object[12];
                adjustParams[0] = component.getProductBundleId();
                adjustParams[1] = ((Object[]) params)[1];
                adjustParams[2] = ((Object[]) params)[2];
                adjustParams[3] = ((Double) ((Object[]) params)[3]) * component.getQuantity();
                //databasechnagelog
                adjustParams[4] = siteguid;                               //siteguid
                adjustParams[5] = adjustParams[0];                               //id
                adjustParams[6] = "I";
                adjustParams[7] = "U";
                adjustParams[8] = "stockcurrent";
                adjustParams[9] = "location";
                adjustParams[10] = "product";
                adjustParams[11] = 0;
                try {
                    adjustStock(adjustParams);
                } catch (BasicException ex) {
                    Logger.getLogger(DataLogicSales.class.getName()).log(Level.SEVERE, null, ex);
                }

            });
            //Prem Changes
            return;
//            return as;
        }
        int updateresult = ((Object[]) params)[2] == null
                ? new PreparedSentence(s, "UPDATE stockcurrent SET UNITS = (UNITS + ?) , SITEGUID = ? "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID IS NULL", new SerializerWriteBasicExt(
                                stockAdjustDatasDatabase, new int[]{3, 4, 1, 0}))
                        .exec(params)
                : new PreparedSentence(s, "UPDATE stockcurrent SET UNITS = (UNITS + ?) , SITEGUID = ? "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID = ?", new SerializerWriteBasicExt(
                                stockAdjustDatasDatabase, new int[]{3, 4, 1, 0, 2}))
                        .exec(params);
        if (updateresult == 1) {

            int linenumber = ((Object[]) params)[2] == null
                    ? findStockLineNumber(((Object[]) params)[1].toString(),((Object[]) params)[0].toString(),null)
                    :findStockLineNumber(((Object[]) params)[1].toString(),((Object[]) params)[0].toString(),((Object[]) params)[2].toString());

            new PreparedSentence(s,
                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                    SerializerWriteParams.INSTANCE)
                    .exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, "U");
                            setString(3, "stockcurrent");
                            setString(4, Integer.toString(linenumber));
                            setString(5, "linenumber");
                            setString(6, siteguid);
                            setString(7, "siteguid");
                            setString(8, siteguid);
                            setInt(9, 0);
                            setInt(10, 0);
                        }
                    });

        }

        if (updateresult == 0) {

            new PreparedSentence(s, "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                    + "ATTRIBUTESETINSTANCE_ID, UNITS, SITEGUID) "
                    + "VALUES (?, ?, ?, ?, ?)", new SerializerWriteBasicExt(stockAdjustDatasDatabase,
                            new int[]{1, 0, 2, 3, 4}))
                    .exec(params);
            new PreparedSentence(s,
                    "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                    SerializerWriteParams.INSTANCE)
                    .exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, "I");
                            setString(3, "stockcurrent");
                            setString(4, getstockLineNumber());
                            setString(5, "linenumber");
                            setString(6, siteguid);
                            setString(7, "siteguid");
                            setString(8, siteguid);
                            setInt(9, 0);
                            setInt(10, 0);
                        }
                    });

        }

    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementInsert() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s, "INSERT INTO receipts (ID, MONEY, DATENEW) "
                        + "VALUES (?, ?, ?)", new SerializerWriteBasicExt(paymenttabledatas,
                                new int[]{0, 1, 2})).exec(params);
                return new PreparedSentence(s, "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, NOTES) "
                        + "VALUES (?, ?, ?, ?, ?)", new SerializerWriteBasicExt(paymenttabledatas,
                                new int[]{3, 0, 4, 5, 6}))
                        .exec(params);
            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementDelete() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s, "DELETE FROM payments WHERE ID = ?", new SerializerWriteBasicExt(
                        paymenttabledatas, new int[]{3})).exec(params);
                return new PreparedSentence(s, "DELETE FROM receipts WHERE ID = ?", new SerializerWriteBasicExt(
                        paymenttabledatas, new int[]{0})).exec(params);
            }

        };
    }


    public int findStockLineNumber(String location, String product_id, String attributeid) throws BasicException{
         PreparedSentence p = attributeid == null
                ? new PreparedSentence(s, "SELECT linenumber FROM stockcurrent "
                        + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL",
                        new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                        SerializerReadInteger.INSTANCE)
                : new PreparedSentence(s, "SELECT linenumber FROM stockcurrent "
                        + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?",
                        new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING),
                        SerializerReadInteger.INSTANCE);

        int d = (int) p.find(location, product_id, attributeid);
        return  d;


    }

    /**
     *
     * @param warehouse
     * @param id
     * @param attsetinstid
     * @return
     * @throws BasicException
     */
    public final double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException {

        PreparedSentence p = attsetinstid == null
                ? new PreparedSentence(s, "SELECT UNITS FROM stockcurrent "
                        + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL",
                        new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                        SerializerReadDouble.INSTANCE)
                : new PreparedSentence(s, "SELECT UNITS FROM stockcurrent "
                        + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?",
                        new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING),
                        SerializerReadDouble.INSTANCE);

        Double d = (Double) p.find(warehouse, id, attsetinstid);
        return d == null ? 0.0 : d;
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCatalogCategoryAdd() {
        return new StaticSentence(s,
                "INSERT INTO products_cat(PRODUCT, CATORDER) SELECT ID, " + s.DB.INTEGER_NULL() + " FROM products WHERE CATEGORY = ?",
                SerializerWriteString.INSTANCE);
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCatalogCategoryDel() {
        return new StaticSentence(s, "DELETE FROM products_cat WHERE PRODUCT = ANY (SELECT ID "
                + "FROM products WHERE CATEGORY = ?)", SerializerWriteString.INSTANCE);
    }

    public final SentenceExec getPlacesUpdate() {
        return new SentenceExecTransaction(s) {

            private final String sqlStat = "UPDATE places SET x = ?, y = ? WHERE id = ?";

            private final SerializerWriteBasic serializers = new SerializerWriteBasic(Datas.INT, Datas.INT, Datas.STRING);

            @Override
            public int execInTransaction(Object params) throws BasicException {
                return new PreparedSentence(s, sqlStat, serializers).exec(params);
            }

        };
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableCategories() {
        return new TableDefinition(s,
                "categories",
                new String[]{"ID", "NAME", "PARENTID", "IMAGE", "imgurl", "description", "TEXTTIP", "CATSHOWNAME", "CATORDER", "SITEGUID"},
                new String[]{"ID", AppLocal.getIntString("label.name"), "", AppLocal.getIntString(
                    "label.image")}, new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.IMAGE, Datas.STRING,Datas.STRING,Datas.STRING, Datas.BOOLEAN, Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.NULL, Formats.STRING,Formats.STRING,Formats.STRING,Formats.BOOLEAN, Formats.STRING, Formats.STRING},
                new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxes() {
        return new TableDefinition(s,
                "taxes",
                new String[]{"ID", "NAME", "CATEGORY", "CUSTCATEGORY", "PARENTID", "RATE", "RATECASCADE", "RATEORDER", "SITEGUID"},
                new String[]{"ID", AppLocal.getIntString("label.name"), AppLocal.getIntString(
                    "label.taxcategory"), AppLocal.getIntString("label.custtaxcategory"), AppLocal
                    .getIntString("label.taxparent"), AppLocal
                    .getIntString("label.dutyrate"), AppLocal.getIntString("label.cascade"), AppLocal
                    .getIntString("label.order"), "SITEGUID"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.BOOLEAN, Datas.INT, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.PERCENT, Formats.BOOLEAN, Formats.INT, Formats.STRING},
                new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCustCategories() {
        return new TableDefinition(s,
                "taxcustcategories", new String[]{"ID", "NAME", "SITEGUID"}, new String[]{"ID", AppLocal
                            .getIntString(
                                    "label.name"), "SITEGUID"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING}, new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCategories() {
        return new TableDefinition(s,
                "taxcategories",
                new String[]{"ID", "NAME", "SITEGUID"},
                new String[]{"ID", AppLocal.getIntString("label.name"), "SITEGUID"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING}, new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableLocations() {
        return new TableDefinition(s,
                "locations", new String[]{"ID", "NAME", "ADDRESS", "SITEGUID"}, new String[]{"ID", AppLocal
                            .getIntString(
                                    "label.locationname"),
                    AppLocal
                            .getIntString(
                                    "label.locationaddress"),
                    "SITEGUID"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING}, new int[]{0}
        );
    }

    /**
     *
     */
    protected static class CustomerExtRead implements SerializerRead {

        /**
         *
         * @param dr
         * @return
         * @throws BasicException
         */
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            CustomerInfoExt c = new CustomerInfoExt(dr.getString(1));
            c.setSearchkey(dr.getString(2));
            c.setTaxid(dr.getString(3));
            c.setTaxCustomerID(dr.getString(3));
            c.setName(dr.getString(4));
            c.setTaxCustCategoryID(dr.getString(5));
            c.setCard(dr.getString(6));
            c.setMaxdebt(dr.getDouble(7));
            c.setAddress(dr.getString(8));
            c.setAddress2(dr.getString(9));
            c.setPcode(dr.getString(10));
            c.setCity(dr.getString(11));
            c.setRegion(dr.getString(12));
            c.setCountry(dr.getString(13));
            c.setFirstname(dr.getString(14));
            c.setLastname(dr.getString(15));
            c.setCemail(dr.getString(16));
            c.setPhone1(dr.getString(17));
            c.setPhone2(dr.getString(18));
            c.setFax(dr.getString(19));
            c.setNotes(dr.getString(20));
            c.setVisible(dr.getBoolean(21));
            c.setCurdate(dr.getTimestamp(22));
            c.setAccdebt(dr.getDouble(23));
            c.setImage(ImageUtils.readImage(dr.getString(24)));
            c.setisVIP(dr.getBoolean(25));
            c.setDiscount(dr.getDouble(26));
            c.setMemoDate(dr.getString(27));

            return c;
        }

    }

    public final UomInfo getUomInfoById(String id) throws BasicException {
        return (UomInfo) new PreparedSentence(s,
                "SELECT "
                + "id, name "
                + "FROM uom "
                + "WHERE id = ?", SerializerWriteString.INSTANCE, UomInfo
                        .getSerializerRead()).find(id);
    }

    public final TableDefinition getTableUom() {
        return new TableDefinition(s,
                "uom",
                new String[]{"id", "name", "siteguid"},
                new String[]{"id",
                    AppLocal.getIntString("Label.Name"), "siteguid"},
                new Datas[]{
                    Datas.STRING, Datas.STRING, Datas.STRING},
                new Formats[]{
                    Formats.STRING, Formats.STRING, Formats.STRING},
                new int[]{0}
        );
    }

    public final SentenceList getUomList() {
        return new StaticSentence(s, "SELECT ID, NAME  FROM uom ORDER BY NAME", null, UomInfo.getSerializerRead());
    }

    public final SentenceList getVoucherList() {
        return new StaticSentence(s,
                "SELECT "
                + "vouchers.ID,vouchers.VOUCHER_NUMBER,vouchers.CUSTOMER, "
                + "customers.NAME,AMOUNT "
                + "FROM vouchers   "
                + "JOIN customers ON customers.id = vouchers.CUSTOMER  "
                + "WHERE STATUS='A'", null, VoucherInfo.getSerializerRead());
    }

    public final SentenceExec getVoucherNonActive() {
        return new PreparedSentence(s,
                "UPDATE vouchers SET STATUS = 'D' "
                + "WHERE VOUCHER_NUMBER = ?" //                  "WHERE VOUCHER_NUMBER = 'VO-06-17-002'"
                ,
                 SerializerWriteString.INSTANCE);
    }

    public final SentenceExec resetPickupId() {

        return new PreparedSentence(s,
                "UPDATE pickup_number SET ID=1 ", SerializerWriteString.INSTANCE);

    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public SupplierInfoExt loadSupplierExt(String id) throws BasicException {
        return (SupplierInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "VATID "
                + "FROM suppliers WHERE ID = ?", SerializerWriteString.INSTANCE,
                new SupplierExtRead()).find(id);
    }

    /**
     *
     */
    protected static class SupplierExtRead implements SerializerRead {

        /**
         *
         * @param dr
         * @return
         * @throws BasicException
         */
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            SupplierInfoExt s = new SupplierInfoExt(dr.getString(1));
            s.setSearchkey(dr.getString(2));
            s.setTaxid(dr.getString(3));
            s.setName(dr.getString(4));
            s.setMaxdebt(dr.getDouble(5));
            s.setAddress(dr.getString(6));
            s.setAddress2(dr.getString(7));
            s.setPostal(dr.getString(8));
            s.setCity(dr.getString(9));
            s.setRegion(dr.getString(10));
            s.setCountry(dr.getString(11));
            s.setFirstname(dr.getString(12));
            s.setLastname(dr.getString(13));
            s.setEmail(dr.getString(14));
            s.setPhone(dr.getString(15));
            s.setPhone2(dr.getString(16));
            s.setFax(dr.getString(17));
            s.setNotes(dr.getString(18));
            s.setVisible(dr.getBoolean(19));
            s.setCurdate(dr.getTimestamp(20));
            s.setCurdebt(dr.getDouble(21));
            s.setSupplierVATID(dr.getString(22));

            return s;
        }

    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerInsert() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s, "INSERT INTO customers ("
                        + "ID, "
                        + "SEARCHKEY, "
                        + "TAXID, "
                        + "NAME, "
                        + "TAXCATEGORY, "
                        + "CARD, "
                        + "MAXDEBT, "
                        + "ADDRESS, "
                        + "ADDRESS2, "
                        + "POSTAL, "
                        + "CITY, "
                        + "REGION, "
                        + "COUNTRY, "
                        + "FIRSTNAME, "
                        + "LASTNAME, "
                        + "EMAIL, "
                        + "PHONE, "
                        + "PHONE2, "
                        + "FAX, "
                        + "NOTES, "
                        + "VISIBLE, "
                        + "CURDATE, "
                        + "CURDEBT, "
                        + "IMAGE, "
                        + "ISVIP, "
                        + "DISCOUNT, "
                        + "MEMODATE,"
                        + "SITEGUID ) "
                        + "VALUES ("
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?)", new SerializerWriteBasicExt(customersRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5, 6,
                                    7, 8, 9, 10, 11, 12,
                                    13, 14, 15, 16, 17, 18,
                                    19, 20, 21, 22, 23, 24,
                                    25, 26, 27}))
                        .exec(params);
                return i;
            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerUpdate() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s, "UPDATE customers SET "
                        + "ID = ?, "
                        + "SEARCHKEY = ?, "
                        + "TAXID = ?, "
                        + "NAME = ?, "
                        + "TAXCATEGORY = ?, "
                        + "CARD = ?, "
                        + "MAXDEBT = ?, "
                        + "ADDRESS = ?, "
                        + "ADDRESS2 = ?, "
                        + "POSTAL = ?, "
                        + "CITY = ?, "
                        + "REGION = ?, "
                        + "COUNTRY = ?, "
                        + "FIRSTNAME = ?, "
                        + "LASTNAME = ?, "
                        + "EMAIL = ?, "
                        + "PHONE = ?, "
                        + "PHONE2 = ?, "
                        + "FAX = ?,  "
                        + "NOTES = ?,"
                        + "VISIBLE = ?, "
                        + "CURDATE = ?, "
                        + "CURDEBT = ?, "
                        + "IMAGE = ?, "
                        + "ISVIP = ?, "
                        + "DISCOUNT = ?, "
                        + "MEMODATE = ? "
                        + "SITEGUID = ? "
                        + "WHERE ID = ?", new SerializerWriteBasicExt(customersRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5,
                                    6, 7, 8, 9, 10,
                                    11, 12, 13, 14, 15,
                                    16, 17, 18, 19, 20,
                                    21, 22, 23, 24, 25,
                                    26, 27, 0}))
                        .exec(params);

                /*
                 * Use this block workflow as template to pump LOYALTY, MEMBERSHIP & etc updates to internal or external
                 * DB table if (i > 0) { if (((Boolean)values[n0])) { if (new PreparedSentence(s , "UPDATE tablename SET
                 * FIELD = ? WHERE CUSTOMER = ?" , new SerializerWriteBasicExt(customersRow.getDatas() , new int[] {n1,
                 * 0})).exec(params) == 0) { new PreparedSentence(s , "INSERT INTO other_tablename (CUSTOMER, FIELD)
                 * VALUES (?, ?)" , new SerializerWriteBasicExt(productsRow.getDatas(), new int[] {0,
                 * n1})).exec(params); } } else { new PreparedSentence(s , "DELETE FROM FIELD WHERE CUSTOMER = ?" , new
                 * SerializerWriteBasicExt(customersRow.getDatas(), new int[] {0})).exec(params); } }
                 */
                return i;
            }

        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerDelete() {
        return new SentenceExecTransaction(s) {

            @Override
            public int execInTransaction(Object params) throws BasicException {
                return new PreparedSentence(s, "DELETE FROM customers WHERE ID = ?", new SerializerWriteBasicExt(
                        customersRow.getDatas(),
                        new int[]{0}
                )).exec(params);
            }

        };
    }

    /**
     *
     * @param params
     * @return
     * @throws com.openbravo.basic.BasicException
     */
    public final int insertDatabaseChangeLog(Object params) throws BasicException {
        Object[] values = (Object[]) params;
        final Datas[] datas = new Datas[10];
        datas[0] = Datas.STRING;
        datas[1] = Datas.STRING;
        datas[2] = Datas.STRING;
        datas[3] = Datas.STRING;
        datas[4] = Datas.STRING;
        datas[5] = Datas.STRING;
        datas[6] = Datas.STRING;
        datas[7] = Datas.STRING;
        datas[8] = Datas.INT;
        datas[9] = Datas.INT;
        return new StaticSentence(s,
                "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                new SerializerWriteBasicExt(datas, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}))
                .exec(params);
    }

    public final SentenceExec insertDatabaseChangeLog2() throws BasicException {
        return new StaticSentence(s,
                "INSERT INTO databasechangelog (ID, PROCESS, TABLENAME, TABLE_PK_ID,  TABLEPK_NAME, TABLE_SCND_ID, TABLE_SCND_NAME, SITEGUID, SFLAG, LFLAG) VALUES (?,?,?,?,?,?,?,?,?,? )",
                new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.INT, Datas.INT));

    }

    private interface StockDiaryUpdate {

        void execute(final String productId, final String attrSetInstanceId, String attrStockId, final Double qty, final Double price, String Siteguid);

    }

}
