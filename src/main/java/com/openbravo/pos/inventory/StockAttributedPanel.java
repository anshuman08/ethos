package com.openbravo.pos.inventory;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.ProductInfoExt;

import static java.util.UUID.randomUUID;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 */
public class StockAttributedPanel extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(StockAttributedPanel.class.getName());

    private static final long serialVersionUID = -4030891505487557132L;

    public static final AttributedProductStock NULL_PRODUCT_STOCK
            = new AttributedProductStock("null-id", "(Select Product Stock)", "null-product");

    private AppView app;

    private DirtyManager dirty;

    private DataLogicSales dlSales;

    private String productId;

    private Map<AttributedProductStock, List<AttributedProductStockAttribute>> productStockAttributes = new HashMap<>();

    /**
     * Creates new form StockAttributedPanel
     */
    public StockAttributedPanel() {
        initComponents();
    }

    /**
     * Creates new form StockAttributedPanel
     * @param app
     * @param dirty
     */
    public StockAttributedPanel(final AppView app, final DirtyManager dirty) {
        this.app = app;
        this.dirty = dirty;
        this.dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        initComponents();
        productStocksBox.addItemListener(dirty);
        productStocksBox.addItemListener((event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                final AttributedProductStock selectedProductStock = (AttributedProductStock) event.getItem();

                LOG.log(Level.INFO, "Selected product stock: {0}", selectedProductStock);

                if (selectedProductStock.equals(NULL_PRODUCT_STOCK)) {
                    stockAttributedEditor1.clearStockAttributeEdits();
                    return;
                }

                setDefaultPrice(selectedProductStock);

                stockAttributedEditor1.addValueForEdit(selectedProductStock);
            }
        });

    }

    public void writeValueEOF() {
        stockAttributedEditor1.writeValueEOF();
        addNewProductStockjButton.setEnabled(false);
        productStocksBox.setSelectedItem(NULL_PRODUCT_STOCK);
    }

    public void writeValueInsert() {
        stockAttributedEditor1.writeValueInsert();
        addNewProductStockjButton.setEnabled(false);
        productStocksBox.setSelectedItem(NULL_PRODUCT_STOCK);
    }

    public Object[] createValue() {
        return stockAttributedEditor1.getProductStockAttributes()
                .entrySet()
                .stream()
                .toArray();
    }

    public void writeValueEdit(String productId) {
        selectProduct(productId, true);
    }

    public void writeValueDelete(String productId) {
        selectProduct(productId, false);
    }

    private void selectProduct(final String productId, final boolean enableAddStock) {
        this.productId = productId;
        addNewProductStockjButton.setEnabled(enableAddStock);
        stockAttributedEditor1.clearStockAttributeEdits();
        try {
            final List<AttributedProductStock> productStocks
                    = new ArrayList<>(dlSales.getAttributedProductStock().list(productId));
            final AttributedProductStock firstProductStock = productStocks.stream()
                    .findFirst()
                    .orElse(null);
            productStocks.add(0, NULL_PRODUCT_STOCK);

            final ComboBoxValModel valModel = new ComboBoxValModel(productStocks);
            productStocksBox.setModel(valModel);
            if (firstProductStock != null) {
                setDefaultPrice(firstProductStock);
                stockAttributedEditor1.addValueForEdit(firstProductStock);
                productStocksBox.setSelectedItem(firstProductStock);
            } else {
                productStocksBox.setSelectedItem(NULL_PRODUCT_STOCK);
            }
        } catch (final BasicException ex) {
            Logger.getLogger(StockAttributedPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setDefaultPrice(final AttributedProductStock productStock) {
        if (productStock.getPrice() != 0.0) {
            return;
        }

        try {
            final ProductInfoExt product = dlSales.getProductInfo(productId);
            productStock.setPrice(product.getPriceSell());
        } catch (final BasicException ex) {
            Logger.getLogger(StockAttributedPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productStocksLabel = new javax.swing.JLabel();
        productStocksBox = new javax.swing.JComboBox<>();
        addNewProductStockjButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        stockAttributedEditor1 = new com.openbravo.pos.inventory.StockAttributedEditor(app, dirty);

        productStocksLabel.setText("Product Stocks");

        addNewProductStockjButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/editnew.png"))); // NOI18N
        addNewProductStockjButton.setText("Add New Stock");
        addNewProductStockjButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        addNewProductStockjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewProductStockjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(productStocksLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(productStocksBox, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addNewProductStockjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(stockAttributedEditor1, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addNewProductStockjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(productStocksLabel)
                        .addComponent(productStocksBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stockAttributedEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addNewProductStockjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewProductStockjButtonActionPerformed
        try {
            // TODO add your handling code here:
            final String stockName = JOptionPane.showInputDialog(this, "Please add new Product Stock Name");
            if (stockName == null || stockName.trim().isEmpty()) {
                return;
            }

            AttributedProductStock productStock = (AttributedProductStock) dlSales
                    .getAttributedProductStockByName()
                    .find(stockName, productId);
            if (productStock == null) {
                productStock = new AttributedProductStock(randomUUID().toString(), stockName, productId);
            }

            setDefaultPrice(productStock);

            stockAttributedEditor1.clearStockAttributeEdits();

            if (stockAttributedEditor1.addValueForEdit(productStock)) {
                final ComboBoxValModel model = (ComboBoxValModel) productStocksBox.getModel();
                model.addIfAbsent(productStock);
                productStocksBox.setSelectedItem(productStock);
            }
        } catch (BasicException ex) {
            Logger.getLogger(StockAttributedPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_addNewProductStockjButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewProductStockjButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox<String> productStocksBox;
    private javax.swing.JLabel productStocksLabel;
    private com.openbravo.pos.inventory.StockAttributedEditor stockAttributedEditor1;
    // End of variables declaration//GEN-END:variables

}
