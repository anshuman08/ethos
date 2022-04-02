/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.inventory.AttributedProductStock;
import com.openbravo.pos.inventory.ProductsBundleInfo;
import com.openbravo.pos.inventory.StockAttributedEditor.EachRowEditor;
import edu.emory.mathcs.backport.java.util.Collections;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 */
public class JAttributedProductStockSelection extends javax.swing.JDialog {

    private static final Logger LOG = Logger.getLogger(JAttributedProductStockSelection.class.getName());

    private static final long serialVersionUID = -2815801473606090469L;

    private final DataLogicSales dlSales;

    private final List<AttributedProductStockSelection> productStockSelections = new ArrayList<>();

    private final EachRowEditor rowEditor;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public JAttributedProductStockSelection(final DataLogicSales dlSales) {
        super();
        this.dlSales = dlSales;
        initComponents();

        setModal(true);
        final Dimension size = new Dimension(600, 300);
        this.setSize(size);
        this.setPreferredSize(size);
        this.setLocationRelativeTo(null);

        // do nothing on close
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        //set row editors
        this.rowEditor = new EachRowEditor(attributedProductStockSelectionjTable);
        attributedProductStockSelectionjTable.getColumnModel()
                .getColumn(1)
                .setCellEditor(rowEditor);
    }

    public static List<AttributedProductStockSelection> showSelectAttributedStock(
            final DataLogicSales dlSales, final String productId) {
        final JAttributedProductStockSelection selectionPanel = new JAttributedProductStockSelection(dlSales);
        return selectionPanel.selectAttributedStock(productId);
    }

    private List<AttributedProductStockSelection> selectAttributedStock(final String productId) {
        final DefaultTableModel tableModel = (DefaultTableModel) attributedProductStockSelectionjTable.getModel();
        final int rows = tableModel.getRowCount();
        for (int row = (rows - 1); row >= 0; row--) {
            tableModel.removeRow(row);
        }

        if (!configureProductBundles(productId)) {
            return Collections.emptyList();
        }

        this.setVisible(true);

        //Disposed
        return productStockSelections;
    }

    private boolean configureProductBundles(final String productId) {
//        LOG.log(Level.INFO, "Configuring product stock for: {0}", productId);
        try {
            final List<ProductsBundleInfo> productsBundleInfos = dlSales.getProductsBundle(productId);
            if (productsBundleInfos == null || productsBundleInfos.isEmpty()) {
                return false;
            }

            //Configure selection
            final Map<String, Double> productQuantities = productsBundleInfos.stream()
                    .collect(toMap(ProductsBundleInfo::getProductBundleId, ProductsBundleInfo::getQuantity));
            final List<AttributedProductStock> attributedProductStocks = dlSales
                    .getAttributedProductStockByProductIds(productQuantities.keySet());
            if (attributedProductStocks.isEmpty()) {
                return false;
            }

            final AtomicInteger count = new AtomicInteger(0);
            attributedProductStocks.stream()
                    .collect(groupingBy(AttributedProductStock::getProductName))
                    .forEach((productName, pStocks) -> {
                        final int qty = pStocks.stream()
                                .map((pStock) -> {
                                    return productQuantities.getOrDefault(pStock.getProductId(), 0.0).intValue();
                                })
                                .findFirst()
                                .orElse(1);
                        addRow(count.getAndIncrement(), productName, pStocks, qty);
                    });

            return true;
        } catch (BasicException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private void addRow(final int rowIndex,
                        final String productName,
                        final List<AttributedProductStock> pStocks,
                        final int qty) {
        final DefaultTableModel tableModel = (DefaultTableModel) attributedProductStockSelectionjTable.getModel();
        final List<AttributedProductStockSelection> selections = pStocks.stream()
                .map((pStock) -> {
                    return new AttributedProductStockSelection(pStock, qty);
                })
                .collect(toList());
        final Object[] rowValue = {productName, null};
        final ComboBoxValModel valModel = new ComboBoxValModel(selections);
        final JComboBox box = new JComboBox(valModel);
        rowEditor.setEditor(rowIndex, new DefaultCellEditor(box));

        tableModel.addRow(rowValue);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        attributedProductStockSelectionjTable = new javax.swing.JTable();
        canceljButton = new javax.swing.JButton();
        continuejButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Please select Product Stock for the selected product.");

        attributedProductStockSelectionjTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Product", "Stock Options"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        attributedProductStockSelectionjTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(attributedProductStockSelectionjTable);
        if (attributedProductStockSelectionjTable.getColumnModel().getColumnCount() > 0) {
            attributedProductStockSelectionjTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        }

        canceljButton.setText("Cancel");
        canceljButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canceljButtonActionPerformed(evt);
            }
        });

        continuejButton.setText("Continue");
        continuejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                continuejButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(continuejButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(canceljButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {canceljButton, continuejButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(continuejButton)
                    .addComponent(canceljButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void continuejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continuejButtonActionPerformed
        // TODO add your handling code here:
        final int rows = attributedProductStockSelectionjTable.getRowCount();
        for (int row = 0; row < rows; row++) {
            final AttributedProductStockSelection selection
                    = (AttributedProductStockSelection) attributedProductStockSelectionjTable.getValueAt(row, 1);
            if (selection == null) {
                JOptionPane.showMessageDialog(this, "Please select value for row <" + row + ">");
                return;
            }

            productStockSelections.add(selection);
        }

        //all done, dispose
        this.dispose();
    }//GEN-LAST:event_continuejButtonActionPerformed

    private void canceljButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canceljButtonActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_canceljButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable attributedProductStockSelectionjTable;
    private javax.swing.JButton canceljButton;
    private javax.swing.JButton continuejButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

}
