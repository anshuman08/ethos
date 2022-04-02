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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.util.UUID;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author adrianromero
 */
public final class LocationsView extends javax.swing.JPanel implements EditorRecord {

    // private DirtyManager m_Dirty = new DirtyManager();    
    private String m_sID;
    private Object m_process;
    private String g_siteguid;
    private DataLogicSales dlSales;

    /**
     * Creates new form LocationsEditor
     *
     * @param app
     * @param dirty
     */
    public LocationsView(AppView app, DirtyManager dirty) {
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        initComponents();

        m_jName.getDocument().addDocumentListener(dirty);
        m_jAddress.getDocument().addDocumentListener(dirty);

        //databsechnagelog
        try {
            g_siteguid = dlSales.getSiteGuid().list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");

        } catch (BasicException ex) {
            Logger.getLogger(LocationsView.class.getName()).log(Level.SEVERE, null, ex);
        }

        writeValueEOF();
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {

        m_sID = null;
        m_jName.setText(null);
        m_jAddress.setText(null);

        m_jName.setEnabled(false);
        m_jAddress.setEnabled(false);
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
        m_jAddress.setText(null);

        m_jName.setEnabled(true);
        m_jAddress.setEnabled(true);
        //databsechangelog
        m_process = "I";
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        Object[] location = (Object[]) value;
        m_sID = Formats.STRING.formatValue(location[0]);
        m_jName.setText(Formats.STRING.formatValue(location[1]));
        m_jAddress.setText(Formats.STRING.formatValue(location[2]));

        m_jName.setEnabled(false);
        m_jAddress.setEnabled(false);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        Object[] location = (Object[]) value;
        m_sID = Formats.STRING.formatValue(location[0]);
        m_jName.setText(Formats.STRING.formatValue(location[1]));
        m_jAddress.setText(Formats.STRING.formatValue(location[2]));

        m_jName.setEnabled(true);
        m_jAddress.setEnabled(true);
        //databsechangelog
        m_process = "U";
    }

    /**
     *
     * @return @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object[] location = new Object[4];
        location[0] = m_sID;
        location[1] = m_jName.getText();
        location[2] = m_jAddress.getText();
        location[3] = g_siteguid;

        Object[] data = new Object[10];
        data[0] = UUID.randomUUID().toString();
        data[1] = m_process;
        data[2] = "locations";
        data[3] = location[0];
        data[4] = "id";
        data[5] = m_jName.getText();
        data[6] = "name";
        data[7] = g_siteguid;
        data[8] = 0;
        data[9] = 0;
        dlSales.insertDatabaseChangeLog(data);

        return location;
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
     *
     */
    @Override
    public void refresh() {
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
        jLabel3 = new javax.swing.JLabel();
        m_jAddress = new javax.swing.JTextField();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.locationname")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(220, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.locationaddress")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jAddress.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jAddress.setPreferredSize(new java.awt.Dimension(220, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = m_sID;
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);

            JOptionPane.showMessageDialog(null,
                    AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel2MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField m_jAddress;
    private javax.swing.JTextField m_jName;
    // End of variables declaration//GEN-END:variables

}
