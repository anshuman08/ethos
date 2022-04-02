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
package com.openbravo.pos.mant;

import java.awt.Component;
import java.awt.Image;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import java.util.logging.Level;

/**
 *
 * @author adrianromero
 */
public final class PlacesEditor extends JPanel implements EditorRecord {

    private static final Logger LOG = Logger.getLogger(PlacesEditor.class.getName());

    private static final long serialVersionUID = 1799174853222988794L;

    private SentenceList m_sentfloor;

    private ComboBoxValModel m_FloorModel;

    private SentenceList tableArrangmentDesigns;

    private ComboBoxValModel tableArrangmentDesignsModel;

    private String m_sID;

    private final DataLogicSales dataLogicSales;

    private Object m_process;
    private String g_siteguid;

    /**
     * Creates new form PlacesEditor
     *
     * @param dlSales
     * @param dirty
     */
    public PlacesEditor(DataLogicSales dlSales, DirtyManager dirty) {
        initComponents();

        this.dataLogicSales = dlSales;
        m_sentfloor = dlSales.getFloorsList();
        tableArrangmentDesigns = dlSales.getTableArrangements();

//        m_sentfloor = dlSales.getFloorTablesList();
        m_FloorModel = new ComboBoxValModel();
        tableArrangmentDesignsModel = new ComboBoxValModel();

        m_jName.getDocument().addDocumentListener(dirty);
        m_jFloor.addActionListener(dirty);
        m_jX.getDocument().addDocumentListener(dirty);
        m_jY.getDocument().addDocumentListener(dirty);
        tableDesignDropdowns.addActionListener(dirty);
        //databsechnagelog
        try {
            g_siteguid = dlSales.getSiteGuid().list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");

        } catch (BasicException ex) {
            Logger.getLogger(PlacesEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        writeValueEOF();
    }

    /**
     *
     * @throws BasicException
     */
    public void activate() throws BasicException {
        m_FloorModel = new ComboBoxValModel(m_sentfloor.list());
        m_jFloor.setModel(m_FloorModel);

        final List designs = tableArrangmentDesigns.list();
        tableArrangmentDesignsModel = new ComboBoxValModel(designs);
        tableDesignDropdowns.setModel(tableArrangmentDesignsModel);
    }

    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {

        m_sID = null;
        m_jName.setText(null);
        m_FloorModel.setSelectedKey(null);
        tableArrangmentDesignsModel.setSelectedKey(null);
        m_jX.setText(null);
        m_jY.setText(null);

        m_jName.setEnabled(false);
        m_jFloor.setEnabled(false);
        m_jX.setEnabled(false);
        m_jY.setEnabled(false);
        //databsechangelog
        m_process = null;
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {

        m_sID = UUID.randomUUID().toString();
        m_jName.setText(null);
        m_FloorModel.setSelectedKey(null);
        tableArrangmentDesignsModel.setSelectedKey(null);
        m_jX.setText(null);
        m_jY.setText(null);

        m_jName.setEnabled(true);
        m_jFloor.setEnabled(true);
        m_jX.setEnabled(true);
        m_jY.setEnabled(true);
        //databsechangelog
        m_process = "I";
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        Object[] place = (Object[]) value;
        m_sID = Formats.STRING.formatValue(place[0]);
        m_jName.setText(Formats.STRING.formatValue(place[1]));
        m_jX.setText(Formats.INT.formatValue(place[2]));
        m_jY.setText(Formats.INT.formatValue(place[3]));
        m_FloorModel.setSelectedKey(place[4]);
        setSelectedDesign(place);

        m_jName.setEnabled(false);
        m_jFloor.setEnabled(false);
        m_jX.setEnabled(false);
        m_jY.setEnabled(false);
        //databsechangelog
        m_process = "D";
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        Object[] place = (Object[]) value;
        m_sID = Formats.STRING.formatValue(place[0]);
        m_jName.setText(Formats.STRING.formatValue(place[1]));
        m_jX.setText(Formats.INT.formatValue(place[2]));
        m_jY.setText(Formats.INT.formatValue(place[3]));
        m_FloorModel.setSelectedKey(place[4]);
        setSelectedDesign(place);

        m_jName.setEnabled(true);
        m_jFloor.setEnabled(true);
        m_jX.setEnabled(true);
        m_jY.setEnabled(true);
        //databsechangelog
        m_process = "U";
    }

    private void setSelectedDesign(final Object[] place) {
        if (place.length > 5) {
            final String designId = Formats.STRING.formatValue(place[5]);
            tableArrangmentDesignsModel.setSelectedKey(designId);

            final TableArrangement ta = dataLogicSales.getTableArrangement(designId);
            if (ta != null) {
                setSelectedIcon(ta);
                return;
            }

        }

        tableDesignImageViewPanel.setIcon(null);
    }

    /**
     *
     * @return @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object[] place = new Object[7];
        place[0] = m_sID;
        place[1] = m_jName.getText();
        place[2] = Formats.INT.parseValue(m_jX.getText());
        place[3] = Formats.INT.parseValue(m_jY.getText());
        place[4] = m_FloorModel.getSelectedKey();
        place[5] = tableArrangmentDesignsModel.getSelectedKey();
        place[6] = g_siteguid;
        
        Object[] data = new Object[10];
        data[0] = UUID.randomUUID().toString();
        data[1] = m_process;
        data[2] = "places";
        data[3] = place[0];
        data[4] = "id";
        data[5] = m_jName.getText();
        data[6] = "name";
        data[7] = g_siteguid;
        data[8] = 0;
        data[9] = 0;
        dataLogicSales.insertDatabaseChangeLog(data);

        return place;
        
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        floorLabel = new javax.swing.JLabel();
        m_jFloor = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jX = new javax.swing.JTextField();
        m_jY = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tableDesignImageViewPanel = new javax.swing.JLabel();
        tableDesignLabel = new javax.swing.JLabel();
        tableDesignDropdowns = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        floorLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        floorLabel.setText(AppLocal.getIntString("label.placefloor")); // NOI18N
        floorLabel.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jFloor.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jFloor.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.placeposition")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Across");
        jLabel5.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Down");
        jLabel3.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jX.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jX.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jY.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jY.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("<html>Position buttons in your Table plan graphic set in the Floor option <br><br> Start Position 0, 0 is Top Left");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel7.setMinimumSize(new java.awt.Dimension(50, 40));
        jLabel7.setPreferredSize(new java.awt.Dimension(489, 40));

        tableDesignImageViewPanel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tableDesignImageViewPanel.setForeground(new java.awt.Color(0, 102, 255));
        tableDesignImageViewPanel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tableDesignImageViewPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/restaurant_floor_sml.png"))); // NOI18N
        tableDesignImageViewPanel.setText(" ");
        tableDesignImageViewPanel.setBorder(new javax.swing.border.MatteBorder(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/restaurant_floor_sml.png")))); // NOI18N

        tableDesignLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        tableDesignLabel.setText(AppLocal.getIntString("label.tableDesign"));
        tableDesignLabel.setPreferredSize(new java.awt.Dimension(110, 30));

        tableDesignDropdowns.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        tableDesignDropdowns.setPreferredSize(new java.awt.Dimension(0, 30));
        tableDesignDropdowns.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tableDesignDropdownsItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSeparator1)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(floorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tableDesignLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_jName, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                            .addComponent(m_jFloor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tableDesignDropdowns, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(m_jX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(tableDesignImageViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jY, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(m_jName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(floorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableDesignLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tableDesignDropdowns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tableDesignImageViewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableDesignDropdownsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableDesignDropdownsItemStateChanged
        // TODO add your handling code here:
        final TableArrangement selectedTableArrangement = (TableArrangement) evt.getItem();
        setSelectedIcon(selectedTableArrangement);
    }//GEN-LAST:event_tableDesignDropdownsItemStateChanged

    private void setSelectedIcon(final TableArrangement selectedTableArrangement) {
        final Image scaledImage = selectedTableArrangement.getImage().getScaledInstance(200, 180, Image.SCALE_SMOOTH);
        final ImageIcon designIcon = new ImageIcon(scaledImage);
        tableDesignImageViewPanel.setIcon(designIcon);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel floorLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox m_jFloor;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jX;
    private javax.swing.JTextField m_jY;
    private javax.swing.JComboBox tableDesignDropdowns;
    private javax.swing.JLabel tableDesignImageViewPanel;
    private javax.swing.JLabel tableDesignLabel;
    // End of variables declaration//GEN-END:variables

}
