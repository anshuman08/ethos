//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2017 uniCenta
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.ticket;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.StringUtils;

import static java.lang.String.format;
import java.sql.Connection;
import java.sql.ResultSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author adrianromero
 */
public class TicketLineInfo implements SerializableWrite, SerializableRead, Serializable {

    private static final long serialVersionUID = 6608012948284450199L;

    private String m_sTicket;

    private int m_iLine;

    private double multiply;

    private double price;

    private TaxInfo tax;
    private Properties attributes;

    private String productid;

    private String attsetinstid;

    private String attrProductStockId;

    private Boolean updated = false;

//    private Boolean keep = false;
    private double newprice = 0.0;
    
    public double m_productprice = 0.0;
    public String m_siteguid;
    
    public double discount;


    /**
     * Creates new TicketLineInfo
     *
     * @param productid
     * @param dMultiply
     * @param dPrice
     * @param tax
     * @param props
     * @param m_siteguid
     */
    public TicketLineInfo(String productid, double dMultiply, double dPrice,
                          TaxInfo tax, Properties props) {
        init(productid, null, dMultiply, dPrice, tax, props,m_siteguid);
    }

    /**
     *
     * @param productid
     * @param dMultiply
     * @param dPrice
     * @param tax
     */
    public TicketLineInfo(String productid, double dMultiply, double dPrice, TaxInfo tax) {
        init(productid, null, dMultiply, dPrice, tax, new Properties(),m_siteguid);
    }

    /**
     *
     * @param productid
     * @param productname
     * @param producttaxcategory
     * @param dMultiply
     * @param dPrice
     * @param tax
     */
    public TicketLineInfo(String productid, String productname, String producttaxcategory, double dMultiply,
                          double dPrice, TaxInfo tax) {
        Properties props = new Properties();
        props.setProperty("product.name", productname);
        props.setProperty("product.taxcategoryid", producttaxcategory);
        init(productid, null, dMultiply, dPrice, tax, props,m_siteguid);
    }

    /**
     *
     * @param productname
     * @param producttaxcategory
     * @param dMultiply
     * @param dPrice
     * @param tax
     * @param m_siteguid
     */
    public TicketLineInfo(String productname, String producttaxcategory, double dMultiply, double dPrice, TaxInfo tax) {

        Properties props = new Properties();
        props.setProperty("product.name", productname);
        props.setProperty("product.taxcategoryid", producttaxcategory);
        init(null, null, dMultiply, dPrice, tax, props,m_siteguid);
    }

    /**
     *
     */
    public TicketLineInfo() {

        init(null, null, 0.0, 0.0, null, new Properties(),null);
    }

    /**
     *
     * @param product
     * @param dMultiply
     * @param dPrice
     * @param tax
     * @param attributes
     * @param m_siteguid
     */
    public TicketLineInfo(ProductInfoExt product, double dMultiply, double dPrice, TaxInfo tax, Properties attributes,String m_siteguid) {
        
        String pid;

        if (product == null) {
            pid = null;
            tax = null;
        } else {
            pid = product.getID();
            attributes.setProperty("product.name", product.getName());

            if (product.getMemoDate() == null) {
                attributes.setProperty("product.memodate", "1900-01-01 00:00:01");
            } else {
                attributes.setProperty("product.memodate", product.getMemoDate());
            }

            attributes.setProperty("product.com", product.isCom() ? "true" : "false");
            attributes.setProperty("product.constant", product.isConstant() ? "true" : "false");

            if (product.getPrinter() == null) {
                attributes.setProperty("product.printer", "1");
            } else {
                attributes.setProperty("product.printer", product.getPrinter());
            }

            attributes.setProperty("product.service", product.isService() ? "true" : "false");
            attributes.setProperty("product.vprice", product.isVprice() ? "true" : "false");
            attributes.setProperty("product.verpatrib", product.isVerpatrib() ? "true" : "false");

            if (product.getTextTip() != null) {
                attributes.setProperty("product.texttip", product.getTextTip());
            }

            attributes.setProperty("product.warranty", product.getWarranty() ? "true" : "false");

            if (product.getAttributeSetID() != null) {
                attributes.setProperty("product.attsetid", product.getAttributeSetID());
            }

            attributes.setProperty("product.taxcategoryid", product.getTaxCategoryID());

            if (product.getCategoryID() != null) {
                attributes.setProperty("product.categoryid", product.getCategoryID());
            }

            if ("true".equals(attributes.getProperty("updated"))) {
                attributes.setProperty("updated", "false");
            } else {
                attributes.setProperty("updated", "true");
            }
        }

         init(pid, null, dMultiply, dPrice, tax, attributes, m_siteguid);
    }

    /**
     *
     * @param oProduct
     * @param dPrice
     * @param tax
     * @param attributes
     * @param m_siteguid
     */
    public TicketLineInfo(ProductInfoExt oProduct, double dPrice, TaxInfo tax, Properties attributes, String m_siteguid) {
        this(oProduct, 1.0, dPrice, tax, attributes,m_siteguid);
    }

    /**
     *
     * @param line
     */
    public TicketLineInfo(TicketLineInfo line) {
        init(line.productid, line.attsetinstid, line.multiply, line.price,
             line.tax, (Properties) line.attributes.clone(),m_siteguid);
    }

    private void init(String productid, String attsetinstid, double dMultiply,
                      double dPrice, TaxInfo tax, Properties attributes,String m_siteguid) {
        
        this.productid = productid;
        this.attsetinstid = attsetinstid;
        multiply = dMultiply;
        price = dPrice;
        this.tax = tax;
        this.attributes = attributes;
        this.m_siteguid = m_siteguid;
//        tax = tax;
//        attributes = attributes;

        m_sTicket = null;
        m_iLine = -1;
       

    }

    void setTicket(String ticket, int line) {
        m_sTicket = ticket;
        m_iLine = line;
    }

    /**
     *
     * @param dp
     * @throws BasicException
     */
    @Override
    public void writeValues(DataWrite dp) throws BasicException {
        dp.setString(1, m_sTicket);
        dp.setInt(2, m_iLine);
        dp.setString(3, productid);
        dp.setString(4, attsetinstid);
        dp.setDouble(5, multiply);
        dp.setDouble(6, price);
        dp.setDouble(7, m_productprice);
        dp.setString(8, tax.getId());
        dp.setString(9, attrProductStockId);

        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            attributes.storeToXML(o, AppLocal.APP_NAME, "UTF-8");
            dp.setBytes(10, o.toByteArray());
        } catch (IOException e) {
            dp.setBytes(10, null);
        }
        dp.setString(11, m_siteguid);
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sTicket = dr.getString(1);
        m_iLine = dr.getInt(2);
        productid = dr.getString(3);
        attsetinstid = dr.getString(4);
        multiply = dr.getDouble(5);
        price = dr.getDouble(6);
        attrProductStockId = dr.getString(7);
        tax = new TaxInfo(
                dr.getString(8),
                dr.getString(9),
                dr.getString(10),
                dr.getString(11),
                dr.getString(12),
                dr.getDouble(13),
                dr.getBoolean(14),
                dr.getInt(15));
        attributes = new Properties();
//        m_siteguid = m_siteguid;

        try {
            byte[] img = dr.getBytes(16);
            if (img != null) {
                attributes.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
    }

    /**
     *
     * @return
     */
    public TicketLineInfo copyTicketLine() {
        TicketLineInfo l = new TicketLineInfo();
        l.productid = productid;
        l.attsetinstid = attsetinstid;
        l.multiply = multiply;
        l.price = price;
        l.tax = tax;
        l.attrProductStockId = attrProductStockId;
        l.attributes = (Properties) attributes.clone();
        l.m_siteguid = m_siteguid;

        return l;
    }

    /**
     *
     * @return
     */
    public int getTicketLine() {
        return m_iLine;
    }
// These are the Lookups

    public String getProductID() {
        return productid;
    }
    
     public String getSiteguidID() {
        return m_siteguid;
    }

    public String getProductCategoryID() {
        return (attributes.getProperty("product.categoryid"));
    }

    public String getProductAttSetId() {
        return attributes.getProperty("product.attsetid");
    }

    public String getProductAttSetInstId() {
        return attsetinstid;
    }

    public String getProductAttSetInstDesc() {
        return attributes.getProperty("product.attsetdesc", "");
    }

    public String getProductTaxCategoryID() {
        return (attributes.getProperty("product.taxcategoryid"));
    }

    public TaxInfo getTaxInfo() {
        return tax;
    }

    public void setTaxInfo(TaxInfo oTaxInfo) {
        tax = oTaxInfo;
    }

// These appear on Printed TicketLine
    public String getProductName() {
        return attributes.getProperty("product.name");
    }

    public String getProductMemoDate() {
        return attributes.getProperty("product.memodate");
    }

    public double getPrice() {
        return price;
    }
    
    public double getOrigPrice() {
        return m_productprice;
    }
    
     public double getpreviewOrigPrice() {
        return m_productprice * multiply;
    }        
            
    public double getMultiply() {
        return multiply;
    }

    public double getTaxRate() {
        return tax == null ? 0.0 : tax.getRate();
    }

    public double getNewPrice() {
        newprice = price * (1.0 + getTaxRate());
        return price;
    }

// These are the Summaries
    public double getPriceTax() {
// System.out.println("getPriceTax: " + price * getTaxRate());
        return price * (1.0 + getTaxRate());
    }

    public Properties getProperties() {
        return attributes;
    }

    public String getProperty(String key) {
        return attributes.getProperty(key);
    }

    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }

// These are Ticket Totals
    public double getTax() {
        return price * multiply * getTaxRate();
    }
    public double gethalfTax() {
        return price * multiply * getTaxRate()/2;
    }
    // This is ticketlines m with tax
    public double getValue() {
        return price * multiply * (1.0 + getTaxRate());
    }
    
    public double getOrigValue() {
        return getOrigPrice() * multiply ;
    }
    
    // This is ticketlines price without tax
    public double getSubValue() {
        return price * multiply;
    }
    
    public double getDiscount() {
        //discount=(price*multiply)-(getPrice()*multiply);
        System.out.println("************************* " + discount );
        return discount;
    }
    
    public double getDisSubValue(){
        return discount + (price * multiply);
    }

// SETTERS
    public void setPrice(double dValue) {
        price = dValue;
    }
    
    public void setDiscount(double dDiscount){
        discount = dDiscount;
    }

    public void setPriceTax(double dValue) {
        price = dValue / (1.0 + getTaxRate());
    }

    public void setMultiply(double dValue) {
        multiply = dValue;
    }

    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }

    public void setProductTaxCategoryID(String taxID) {
        attributes.setProperty("product.taxcategoryid", taxID);
    }

    public void setProductAttSetInstId(String value) {
        attsetinstid = value;
    }

    public void setProductAttrSetInstIds(final List<String> attrSetInstanceIds) {
        setListAttributes("product.attributeSetInstanceIds", attrSetInstanceIds);
    }

    public List<String> getProductAttrSetInstIds() {
        return getListAttribute("product.attributeSetInstanceIds");
    }

    public void addProductAttrSetInstanceIdQtyRatio(final String attrSetInstanceId, final int qtyRatio) {
        attributes.setProperty(format("%s.qtyRatio", attrSetInstanceId), String.valueOf(qtyRatio));
    }

    public int getProductAttrSetInstanceIdQtyRatio(final String attrSetInstanceId) {
        return Integer.parseInt(attributes.getProperty(format("%s.qtyRatio", attrSetInstanceId), "0"));
    }

    public void addProductAttrSetInstanceProductStockId(final String attrSetInstanceId, final String attrProdStockId) {
        attributes.setProperty(format("%s.attrStockId", attrSetInstanceId), attrProdStockId);
    }

    public String getProductAttrSetInstanceProductStockId(final String attrSetInstanceId) {
        return attributes.getProperty(format("%s.attrStockId", attrSetInstanceId));
    }

    public void setProductStockId(final String attrProductStockId) {
        this.attrProductStockId = attrProductStockId;
        attributes.setProperty("product.productStockId", attrProductStockId);
    }

    public String getProductStockId() {
        if (this.attrProductStockId != null) {
            return attrProductStockId;
        }

        return attributes.getProperty("product.productStockId");
    }

    public void setProductStockIds(final Collection<String> attrProductStockIds) {
        setListAttributes("product.productStockIds", new ArrayList<>(attrProductStockIds));
    }

    public List<String> getProductStockIds() {
        return getListAttribute("product.productStockIds");
    }

    private void setListAttributes(final String attributeId, final List<String> attributeValues) {
        final String idsStr = attributeValues.stream()
                .collect(joining(","));
        attributes.setProperty(attributeId, idsStr);
    }

    private List<String> getListAttribute(final String attributeId) {
        final String[] idsStr = attributes.getProperty(attributeId, "").split(",");
        final List<String> list = java.util.Arrays.stream(idsStr)
                .filter((str) -> !str.isEmpty())
                .collect(toList());

        return list;
    }

    public void setProductAttSetInstDesc(String value) {
        if (value == null) {
            attributes.remove(value);
        } else {
            attributes.setProperty("product.attsetdesc", value);
        }
    }

    /**
     *
     * @return
     */
    // Print to actual ${ticketline
    public String printName() {
        return StringUtils.encodeXML(attributes.getProperty("product.name"));
    }

    public String printProductMemoDate() {
        return StringUtils.encodeXML(attributes.getProperty("product.memodate"));
    }

    public String printPrice() {
        return Formats.CURRENCY.formatValue(getPrice());
    }
    
    public String printOrigPrice() {
        return Formats.CURRENCY.formatValue(getOrigPrice());
    }
    public String printpreviewOrigPrice() {
        return Formats.CURRENCY.formatValue(getpreviewOrigPrice());
    }
    public String printPriceTax() {
        return Formats.CURRENCY.formatValue(getPriceTax());
    }
    // This is to brieng Quantity of ticketlines
    public String printMultiply() {
        return Formats.DOUBLE.formatValue(multiply);
    }
    
    // This is to brieng ticketlines prices with tax
    public String printValue() {
        return Formats.CURRENCY.formatValue(getValue());
    }
    
    public String printOrigValue() {
        return Formats.CURRENCY.formatValue(getOrigValue());
    }
    
    public String printDiscount() {
        return Formats.CURRENCY.formatValue(getDiscount());
    }

    public String printTaxRate() {
        return Formats.PERCENT.formatValue(getTaxRate());
    }

    public String printSubValue() {
        return Formats.CURRENCY.formatValue(getSubValue());
    }
    
    public String printhalfTax() {
        return Formats.CURRENCY.formatValue(gethalfTax());
    }

    public String printTax() {
        return Formats.CURRENCY.formatValue(getTax());
    }

    public String printTextTip() {
        return attributes.getProperty("product.texttip");
    }

    public String printPrinter() {
        return StringUtils.encodeXML(attributes.getProperty("product.printer"));
    }

    public boolean isProductCom() {
        return "true".equals(attributes.getProperty("product.com"));
    }

    public boolean isProductService() {
        return "true".equals(attributes.getProperty("product.service"));
    }

    public boolean isProductVprice() {
        return "true".equals(attributes.getProperty("product.vprice"));
    }

    public boolean isProductVerpatrib() {
        return "true".equals(attributes.getProperty("product.verpatrib"));
    }

    public boolean isProductWarranty() {
        return "true".equals(attributes.getProperty("product.warranty"));
    }

    public boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean value) {
        updated = value;
    }

    public boolean isProductBundle() {
        return !getProductAttrSetInstIds().isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.productid);
        hash = 83 * hash + Objects.hashCode(this.attsetinstid);
        hash = 83 * hash + Objects.hashCode(this.getProductAttrSetInstIds());
        hash = 83 * hash + Objects.hashCode(this.getProductStockId());
        hash = 83 * hash + Objects.hashCode(this.getProductStockIds());
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
        final TicketLineInfo other = (TicketLineInfo) obj;
        if (!Objects.equals(this.productid, other.productid)) {
            return false;
        }

        if (!Objects.equals(this.attsetinstid, other.attsetinstid)) {
            return false;
        }

        if (!Objects.equals(this.getProductAttrSetInstIds(), other.getProductAttrSetInstIds())) {
            return false;
        }

        if (!Objects.equals(this.getProductStockIds(), other.getProductStockIds())) {
            return false;
        }

        return Objects.equals(this.getProductStockId(), other.getProductStockId());
    }

}
