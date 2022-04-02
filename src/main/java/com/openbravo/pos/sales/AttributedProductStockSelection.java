package com.openbravo.pos.sales;

import com.openbravo.data.loader.IKeyed;
import com.openbravo.pos.inventory.AttributedProductStock;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Oct 4, 2017, 10:46:52 PM
 */
public class AttributedProductStockSelection implements IKeyed {

    private final AttributedProductStock productStock;

    private final int quantityInBunlde;

    public AttributedProductStockSelection(AttributedProductStock productStock, int quantityInBunlde) {
        this.productStock = productStock;
        this.quantityInBunlde = quantityInBunlde;
    }

    @Override
    public Object getKey() {
        return productStock.getId();
    }

    public AttributedProductStock getProductStock() {
        return productStock;
    }

    public int getQuantityInBunlde() {
        return quantityInBunlde;
    }

    @Override
    public String toString() {
        return productStock.getName();
    }

}
