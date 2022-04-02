package com.openbravo.pos.inventory;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 26, 2017, 9:27:30 PM
 */
public class AttributedProductStockAttribute {

    private String id;

    private String attributedProductStockId;

    private String attributeValueId;

    private AttrValueInfo attrValueInfo;

    private boolean newAttribute = true;

    private boolean markedForDelete;

    public String getId() {
        return id;
    }

    public String getAttributedProductStockId() {
        return attributedProductStockId;
    }

    public String getAttributeValueId() {
        return attributeValueId;
    }

    public AttrValueInfo getAttrValueInfo() {
        return attrValueInfo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAttributedProductStockId(String attributedProductStockId) {
        this.attributedProductStockId = attributedProductStockId;
    }

    public void setAttributeValueId(String attributeValueId) {
        this.attributeValueId = attributeValueId;
    }

    public void setAttrValueInfo(AttrValueInfo attrValueInfo) {
        this.attrValueInfo = attrValueInfo;
    }

    public boolean isNewAttribute() {
        return newAttribute;
    }

    public void setMarkedForDelete(boolean markedForDelete) {
        this.markedForDelete = markedForDelete;
    }

    public boolean isMarkedForDelete() {
        return markedForDelete;
    }

    public static AttributedProductStockAttribute readValue(final DataRead dr) {
        try {
            final AttributedProductStockAttribute attribute = new AttributedProductStockAttribute();
            attribute.id = dr.getString(1);
            attribute.attributedProductStockId = dr.getString(2);
            attribute.attributeValueId = dr.getString(3);
            attribute.newAttribute = false;

            final String attributeId = dr.getString(4);
            final String attributeValue = dr.getString(5);
            attribute.attrValueInfo = new AttrValueInfo(attribute.attributeValueId, attributeId, attributeValue);
            return attribute;
        } catch (BasicException ex) {
            Logger.getLogger(AttributedProductStockAttribute.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
