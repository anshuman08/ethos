package com.openbravo.pos.inventory;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import edu.emory.mathcs.backport.java.util.Collections;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 */
public class StockAttributedEditor extends javax.swing.JPanel {

    private static final long serialVersionUID = 2123204713988916934L;

    private static final AttributeSetInfo NULL_SET_INFO
            = new AttributeSetInfo("null-attrset-id", "(Select Attribute Set)");

    private static final AttrValueInfo NULL_VALUE_INFO
            = new AttrValueInfo("null-attrvalue-id", "null-id", "(Select Value)");

    private final DataLogicSales dlSales;

    private final DirtyManager dirty;

    private final Map<AttributedProductStock, Map<String, AttributedProductStockAttribute>> attributedProductStockAttributes;

    private final Map<String, JComboBox> attributeValueInfos;

    private AttributedProductStock currentProductStock;

    /**
     * Creates new form AttributedStockEditor
     */
    public StockAttributedEditor() {
        initComponents();
        this.dlSales = null;
        this.dirty = null;
        this.attributedProductStockAttributes = new HashMap<>();
        this.attributeValueInfos = new HashMap<>();
    }

    /**
     * Creates new form AttributedStockEditor
     */
    public StockAttributedEditor(final AppView app, final DirtyManager dirty) {
        initComponents();
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        this.dirty = dirty;
        attributedProductStockAttributes = new HashMap<>();
        this.attributeValueInfos = new HashMap<>();

        attributeValueTables.setModel(getAttributeValueTableModel());
        attributeValueTables.setRowHeight(26);
        attributeValueTables.getColumnModel().getColumn(1).setCellEditor(getAttrCellEditor());

        final JCheckBox selectionCheckBox = new JCheckBox();
        selectionCheckBox.addActionListener(dirty);
        attributeValueTables.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(selectionCheckBox));

        //Add dirty manager listeners
        productStockPriceField.getDocument().addDocumentListener(dirty);
        productStockQtyField.getDocument().addDocumentListener(dirty);
        attributeSetSelectionBox.addActionListener(dirty);

        //Find all attribute sets
        setAttributeSetOptions();
        attributeSetSelectionBox.addItemListener((event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                final AttributeSetInfo selected = (AttributeSetInfo) event.getItem();
                onAttributeSetSelected(selected);
            }
        });
    }

    public Map<AttributedProductStock, List<AttributedProductStockAttribute>> getProductStockAttributes() {
        //Set the last selected product stock details
        if (currentProductStock != null) {
            setProductStockDetails(currentProductStock);
        }

        return attributedProductStockAttributes.entrySet()
                .stream()
                .filter((e) -> e.getKey().getAttributeSetId() != null)
                .filter((e) -> !e.getKey().getAttributeSetId().equals(NULL_SET_INFO.getId()))
                .filter((e) -> !isDuplicate(e.getKey()))
                .collect(toMap(Entry::getKey, (entry) -> new ArrayList<>(entry.getValue().values())));
    }

    private void onAttributeSetSelected(final AttributeSetInfo attributeSetInfo) {
        try {
            attrCellEditor.resetEditors();

            attributeValueInfos.forEach((id, box) -> {
                ((ComboBoxValModel) box.getModel()).refresh(new ArrayList<>());
            });
            attributeValueInfos.clear();

            //set current product stock attribute set id
            if (currentProductStock != null) {
                currentProductStock.setAttributeSetId(attributeSetInfo.getId());
            }

            final List<AttrUseInfo> attrUseInfos = dlSales.getAttributeUseList(attributeSetInfo.getId()).list();
            final String[] attrIds = attrUseInfos.stream()
                    .map(AttrUseInfo::getAttributeId)
                    .distinct()
                    .toArray(String[]::new);
            if (attrIds.length == 0) {
                return;
            }

            final List<AttrValueInfo> attrValueInfos = dlSales.getAttributeValueList(attrIds).list();
            final Map<String, List<AttrValueInfo>> groupedByAttrId = attrValueInfos.stream()
                    .collect(groupingBy(AttrValueInfo::getAttributeId));
            final List<AttributeInfo> attrInfos = dlSales.getAttributeList(attrIds).list();
            final Map<String, AttributeInfo> groupedByMap = attrInfos.stream()
                    .collect(toMap(AttributeInfo::getId, Function.identity()));
            final AtomicInteger count = new AtomicInteger(0);
            final Object[][] dataModel = new Object[groupedByAttrId.size()][3];

            groupedByAttrId.forEach((attrId, attrValues) -> {

                final Object[] rowValues = new Object[3];
                final int rowIndex = count.getAndIncrement();
                dataModel[rowIndex] = rowValues;

                final AttributeInfo attrInfo = groupedByMap.get(attrId);

                if (attrInfo == null) {
                    return;
                }

                rowValues[0] = attrInfo.toString();
                rowValues[1] = NULL_VALUE_INFO;
                rowValues[2] = false;

                final List<AttrValueInfo> modelAttrValues = new ArrayList<>(attrValues);
                modelAttrValues.add(0, NULL_VALUE_INFO);

                final ComboBoxValModel attrValModel = new ComboBoxValModel(modelAttrValues);
                final JComboBox box = new JComboBox(attrValModel);
                box.addActionListener(dirty);
                attributeValueInfos.put(attrId, box);
                attrCellEditor.setEditor(rowIndex, new DefaultCellEditor(box));
            });
            final DefaultTableModel tableModel = (DefaultTableModel) attributeValueTables.getModel();
            cleanAttributeValues();

            for (Object[] row : dataModel) {
                tableModel.addRow(row);
            }

        } catch (BasicException ex) {
            Logger.getLogger(StockAttributedEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearStockAttributeEdits() {
        currentProductStock = null;
        ((ComboBoxValModel) attributeSetSelectionBox.getModel()).refresh(new ArrayList<>());
        attributedProductStockAttributes.clear();
        attributeValueInfos.clear();
        reInit();
    }

    public boolean addValueForEdit(final AttributedProductStock productStock) {
        if (productStock == currentProductStock) {
            return true;
        }

        //First get current selected product and set attributes
        if (currentProductStock != null) {
            if (!setProductStockDetails(currentProductStock)) {
                return false;
            }
        } else {
            attributedProductStockAttributes.put(productStock, new HashMap<>());
        }

        currentProductStock = productStock;
        //set the selected product infos
        productStockQtyField.setText(String.valueOf(productStock.getQuantity()));
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!product stock" + String.valueOf(productStock.getQuantity()) );
        productStockPriceField.setText(String.valueOf(productStock.getPrice()));
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!product price" + String.valueOf(productStock.getPrice()) );
        
        productStockCountablejCheckBox.setSelected(productStock.isCountable());

        if (productStock.getAttributeSetId() == null) {
            return true;
        }

        try {
            //load attribute infos for the products stock.
            final AttributeSetInfo currentAttributeSetInfo
                    = (AttributeSetInfo) dlSales.getAttributeSet()
                    .list(productStock.getAttributeSetId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (currentAttributeSetInfo != null) {
                attributeSetSelectionBox.setSelectedItem(currentAttributeSetInfo);
                onAttributeSetSelected(currentAttributeSetInfo);

                final Map<String, AttributedProductStockAttribute> productStockAttributesByValueId
                        = attributedProductStockAttributes.computeIfAbsent(productStock, (key) -> new HashMap<>());
                final List<AttributedProductStockAttribute> stockAttributes
                        = dlSales.getAttributedProductStockAttributes().list(productStock.getId());
                final DefaultTableModel tableModel = (DefaultTableModel) attributeValueTables.getModel();
                final int rows = tableModel.getRowCount();
                final Map<String, Integer> attrRows = new HashMap<>();
                for (int r = 0; r < rows; r++) {
                    final String attr = (String) tableModel.getValueAt(r, 0);
                    attrRows.put(attr, r);
                }

                final Map<String, String> attrIdNames = getAttributes(currentAttributeSetInfo.getId());

                stockAttributes.forEach((attr) -> {
                    productStockAttributesByValueId.put(attr.getAttributeValueId(), attr);

                    //Update attribute value row information.
                    final AttrValueInfo attrValueInfo = attr.getAttrValueInfo();
                    if (attrValueInfo != null) {
                        final String attrName = attrIdNames.get(attr.getAttrValueInfo().getAttributeId());
                        if (attrName == null) {
                            return;
                        }

                        final Integer row = attrRows.get(attrName);
                        if (row == null) {
                            return;
                        }

                        tableModel.setValueAt(attrValueInfo, row, 1);
                        tableModel.setValueAt(true, row, 2);
                    }
                });
            }

        } catch (BasicException ex) {
            Logger.getLogger(StockAttributedEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    //attr Id=>Name
    private Map<String, String> getAttributes(final String attributeSetInfoId) throws BasicException {
        final List<AttrUseInfo> attrUseInfos = dlSales.getAttributeUseList(attributeSetInfoId).list();
        final String[] attrIds = attrUseInfos.stream()
                .map(AttrUseInfo::getAttributeId)
                .distinct()
                .toArray(String[]::new);
        if (attrIds.length == 0) {
            return Collections.emptyMap();
        }

        final List<AttributeInfo> attrInfos = dlSales.getAttributeList(attrIds).list();
        return attrInfos.stream()
                .collect(toMap(AttributeInfo::getId, AttributeInfo::toString));
    }

    private boolean setProductStockDetails(final AttributedProductStock productStock) {
        final AttributeSetInfo attrSetInfo = (AttributeSetInfo) attributeSetSelectionBox.getSelectedItem();
        if (attrSetInfo.equals(NULL_SET_INFO) && JOptionPane.showConfirmDialog(this,
                                                                               "No Attribute set selected for this product stock. Would you like to select one?",
                                                                               "Product Stock Attribute Set",
                                                                               JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return false;
        }

        productStock.setAttributeSetId(attrSetInfo.getId());

        final boolean countable = productStockCountablejCheckBox.isSelected();
        productStock.setCountable(countable);

        final String qtyStr = productStockQtyField.getText();
        final String sanitizedQtyStr = qtyStr == null || qtyStr.trim().isEmpty() ? "0" : qtyStr.trim();
        final int quantity = Integer.parseInt(sanitizedQtyStr);
        if (countable && quantity == 0 && JOptionPane.showConfirmDialog(this,
                                                                        "Quantity not set for this product stock. Would you like to set it?",
                                                                        "Product Stock Quantity",
                                                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return false;
        }
        productStock.setQuantity(quantity);

        final String priceStr = productStockPriceField.getText();
        final String sanitizedPriceStr = priceStr == null || priceStr.trim().isEmpty() ? "0.0" : priceStr.trim();
        final double price = Double.parseDouble(sanitizedPriceStr);
        if (countable && price == 0.0 && JOptionPane.showConfirmDialog(this,
                                                                       "Price not set for this product stock. Would you like to set it?",
                                                                       "Product Stock Price",
                                                                       JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return false;
        }
        productStock.setPrice(price);

        final DefaultTableModel tableModel = (DefaultTableModel) attributeValueTables.getModel();
        final Map<AttrValueInfo, Boolean> attrValueInfos = new HashMap<>();
        final int rows = tableModel.getRowCount();
        for (int row = 0; row < rows; row++) {
            final AttrValueInfo attrValueInfo = (AttrValueInfo) tableModel.getValueAt(row, 1);
            final boolean selected = Boolean.valueOf(String.valueOf(tableModel.getValueAt(row, 2)));
            if (!attrValueInfo.equals(NULL_VALUE_INFO)) {
                attrValueInfos.put(attrValueInfo, selected);
            }
        }

        if (attrValueInfos.isEmpty() && JOptionPane.showConfirmDialog(this,
                                                                      "No Attribute Values selected from attribute-set. Would you like to select attributes?",
                                                                      "Product Stock Attribute Values",
                                                                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return false;
        }
        final Map<String, AttributedProductStockAttribute> productStockAttributesByValueId
                = attributedProductStockAttributes.computeIfAbsent(productStock, (key) -> new HashMap<>());
        attrValueInfos.entrySet()
                .stream()
                .map((attrEntry) -> {
                    final AttrValueInfo attrValue = attrEntry.getKey();
                    final boolean isSelected = attrEntry.getValue();
                    final AttributedProductStockAttribute attribute = productStockAttributesByValueId
                            .computeIfAbsent(attrValue.getId(), (id) -> newAttribute(attrValue, productStock.getId()));
                    attribute.setMarkedForDelete(!isSelected);

                    return attribute;
                })
                .forEach((stockAttr) -> productStockAttributesByValueId.put(stockAttr.getAttributeValueId(), stockAttr));

        return true;
    }

    private AttributedProductStockAttribute newAttribute(final AttrValueInfo valueInfo, final String productStockId) {
        final AttributedProductStockAttribute stockAttribute = new AttributedProductStockAttribute();
        stockAttribute.setId(UUID.randomUUID().toString());
        stockAttribute.setAttrValueInfo(valueInfo);
        stockAttribute.setAttributeValueId(valueInfo.getId());
        stockAttribute.setAttributedProductStockId(productStockId);

        return stockAttribute;
    }

    public void writeValueEOF() {
        reInit();
    }

    public void writeValueInsert() {
        reInit();
    }

    private void reInit() {
        productStockPriceField.setText(null);
        productStockQtyField.setText(null);
        setAttributeSetOptions();
        cleanAttributeValues();
    }

    private void cleanAttributeValues() {
        final DefaultTableModel tableModel = (DefaultTableModel) attributeValueTables.getModel();
        final int rows = tableModel.getRowCount();
        for (int row = (rows - 1); row >= 0; row--) {
            tableModel.removeRow(row);
        }
    }

    private void setAttributeSetOptions() {
        try {
            final List<AttributeSetInfo> attributeSetInfo = new ArrayList<>(dlSales.getAttributeSetList().list());
            attributeSetInfo.add(0, NULL_SET_INFO);

            final ComboBoxValModel valModel = new ComboBoxValModel(attributeSetInfo);
            attributeSetSelectionBox.setModel(valModel);
            attributeSetSelectionBox.setSelectedItem(NULL_SET_INFO);
        } catch (BasicException ex) {
            Logger.getLogger(StockAttributedEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        attributeSetSelectionBox = new javax.swing.JComboBox<>();
        attributeSetLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        attributeValueTables = new javax.swing.JTable();
        countableStockjLabel = new javax.swing.JLabel();
        productStockPricejLabel = new javax.swing.JLabel();
        productStockPriceField = new javax.swing.JTextField();
        productStockQtyjLabel = new javax.swing.JLabel();
        productStockQtyField = new javax.swing.JTextField();
        productStockCountablejCheckBox = new javax.swing.JCheckBox();

        attributeSetLabel.setText("Attribute Set");

        attributeValueTables.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        attributeValueTables.setColumnSelectionAllowed(true);
        attributeValueTables.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(attributeValueTables);
        attributeValueTables.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        countableStockjLabel.setText("Countable Stock");

        productStockPricejLabel.setText("Stock Price:");

        productStockQtyjLabel.setText("Stock Qty:");

        productStockCountablejCheckBox.setSelected(true);
        productStockCountablejCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                productStockCountablejCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1)
                .addGap(12, 12, 12))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(countableStockjLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(productStockPricejLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .addComponent(attributeSetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productStockQtyjLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(productStockPriceField)
                            .addComponent(attributeSetSelectionBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(productStockQtyField)
                            .addComponent(productStockCountablejCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {attributeSetLabel, countableStockjLabel, productStockPricejLabel});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jSeparator1, jSeparator2});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countableStockjLabel)
                    .addComponent(productStockCountablejCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productStockQtyjLabel)
                    .addComponent(productStockQtyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productStockPricejLabel)
                    .addComponent(productStockPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeSetSelectionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attributeSetLabel))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void productStockCountablejCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_productStockCountablejCheckBoxItemStateChanged
        // TODO add your handling code here:
        final boolean stateChange = evt.getStateChange() == ItemEvent.SELECTED;
        productStockQtyField.setVisible(stateChange);
        productStockQtyjLabel.setVisible(stateChange);
//        productStockPriceField.setVisible(stateChange);
//        productStockPricejLabel.setVisible(stateChange);
        this.validate();
        this.repaint();
    }//GEN-LAST:event_productStockCountablejCheckBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attributeSetLabel;
    private javax.swing.JComboBox<String> attributeSetSelectionBox;
    private javax.swing.JTable attributeValueTables;
    private javax.swing.JLabel countableStockjLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JCheckBox productStockCountablejCheckBox;
    private javax.swing.JTextField productStockPriceField;
    private javax.swing.JLabel productStockPricejLabel;
    private javax.swing.JTextField productStockQtyField;
    private javax.swing.JLabel productStockQtyjLabel;
    // End of variables declaration//GEN-END:variables

    private EachRowEditor attrCellEditor;

    private DefaultTableModel attributeValueTableModel;

    private DefaultTableModel getAttributeValueTableModel() {
        if (attributeValueTableModel != null) {
            return attributeValueTableModel;
        }

        return (attributeValueTableModel = new AttributeValueTableModel());
    }

    private boolean isDuplicate(final AttributedProductStock productStock) {
        //Product is new, so we check if its set of attribute has already been added.
        final Map<String, AttributedProductStockAttribute> attrValueStockAttrMapping = attributedProductStockAttributes
                .getOrDefault(productStock, Collections.emptyMap());
        final Set<String> attrValues = attrValueStockAttrMapping.keySet();
        if (attrValues.isEmpty()) {
            return false;
        }

        final String attrSetId = productStock.getAttributeSetId();
        final String productId = productStock.getProductId();
        try {
            final AttributedProductStock existingProdStock = (AttributedProductStock) dlSales
                    .getAttributedStockProductForAttributeValues(attrValues.toArray(new String[0]))
                    .find(attrSetId, productId);

            if (existingProdStock != null && !existingProdStock.getId().equals(productStock.getId())) {
                final String attrValuesList = attrValueStockAttrMapping.values()
                        .stream()
                        .map(AttributedProductStockAttribute::getAttrValueInfo)
                        .map(AttrValueInfo::getValue)
                        .collect(joining(", "));
                final String message = format("Current Product Stock <%s> \n"
                        + "has already been defined with similar set of attributes <%s> \n"
                        + "in old product stock <%s>.\n"
                        + "The new product stock will be deleted.\n\n"
                        + "Delete duplicated stock?",
                                              productStock.getName(),
                                              attrValuesList,
                                              existingProdStock.getName());
                final int selectedOption = JOptionPane.showConfirmDialog(StockAttributedEditor.this,
                                                                         message,
                                                                         "Product with Duplicate Attributes filtered",
                                                                         JOptionPane.ERROR_MESSAGE,
                                                                         JOptionPane.YES_NO_OPTION);
                productStock.setMarkedForDelete(selectedOption == JOptionPane.YES_OPTION);

                //by extension, if this is deleted, the attributes should also be deleted
                if (productStock.isMarkedForDelete()) {
                    attributedProductStockAttributes.get(productStock)
                            .forEach((attrValId, stockAttr) -> stockAttr.setMarkedForDelete(true));
                }

                //We bother to delete only if this is an old existing product.
                return productStock.isNewStock();
            }
        } catch (BasicException ex) {
            Logger.getLogger(StockAttributedEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private static final class AttributeValueTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 9042611426760383683L;

        private final String[] columnNames = {"Attribute", "Value", "Selected"};

        private final Vector columnIdents = new Vector();

        {
            for (String col : columnNames) {
                columnIdents.add(col);
            }
        }

        private final Class[] columnTypes = {String.class, AttrValueInfo.class, Boolean.class};

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        public Vector getColumnIdentifiers() {
            return columnIdents;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Class[] getColumnTypes() {
            return columnTypes;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

    }

    private TableCellEditor getAttrCellEditor() {
        if (attrCellEditor != null) {
            return attrCellEditor;
        }

        return (attrCellEditor = new EachRowEditor(attributeValueTables));
    }

    public static class EachRowEditor implements TableCellEditor {

        protected Map<Integer, TableCellEditor> editors;

        protected TableCellEditor editor;

        private final TableCellEditor defaultEditor;

        private final JTable table;

        /**
         * Constructs a EachRowEditor. create default editor
         *
         * @see TableCellEditor
         * @see DefaultCellEditor
         */
        public EachRowEditor(JTable table) {
            this.table = table;
            editors = new HashMap<>();
            defaultEditor = new DefaultCellEditor(new JTextField());
        }

        public synchronized void resetEditors() {
            editors.clear();
        }

        /**
         * @param row table row
         * @param editor table cell editor
         */
        public void addEditor(TableCellEditor editor) {
            editors.put(editors.size(), editor);
        }

        /**
         * @param row table row
         * @param editor table cell editor
         */
        public void setEditor(final int index, TableCellEditor editor) {
            editors.put(index, editor);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            //editor = (TableCellEditor)editors.get(new Integer(row));
            //if (editor == null) {
            //  editor = defaultEditor;
            //}
            return editor.getTableCellEditorComponent(table, value, isSelected,
                                                      row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return editor.getCellEditorValue();
        }

        public Object getCellEditorValue(int row) {
            return editors.get(row).getCellEditorValue();
        }

        @Override
        public boolean stopCellEditing() {
            return editor.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            editor.cancelCellEditing();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            selectEditor((MouseEvent) anEvent);
            return editor.isCellEditable(anEvent);
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            editor.addCellEditorListener(l);
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
            editor.removeCellEditorListener(l);
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            selectEditor((MouseEvent) anEvent);
            return editor.shouldSelectCell(anEvent);
        }

        protected void selectEditor(MouseEvent e) {
            int row;
            if (e == null) {
                row = table.getSelectionModel().getAnchorSelectionIndex();
            } else {
                row = table.rowAtPoint(e.getPoint());
            }

            editor = (TableCellEditor) editors.get(row);
            if (editor == null) {
                editor = defaultEditor;
            }
        }

    }

}
