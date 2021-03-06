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
package com.openbravo.pos.sales.shared;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.JPanelTicket;
import com.openbravo.pos.sales.JTicketsBag;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.mobileorders.*;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagShared extends JTicketsBag {

    private String m_sCurrentTicket = null;
    private DataLogicReceipts dlReceipts = null;
    private DataLogicSales dlSales = null;
    private DatalogicMobileAppSales dlMobileSales = null;
    private MobileAppInfo mobileOrders;
    private MobileAppDetailInfo mobileOrderDetails;
    private DataLogicSystem dlSystem;
    private Boolean showList;
    private String g_siteguid;

    /**
     * Creates new form JTicketsBagShared
     *
     * @param app
     * @param panelticket
     */
    public JTicketsBagShared(AppView app, TicketsEditor panelticket) {

        super(app, panelticket);

        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlMobileSales = (DatalogicMobileAppSales) app.getBean("com.openbravo.pos.sales.mobileorders.DatalogicMobileAppSales");        

        initComponents();
        
        //databsechnagelog
        try {
            g_siteguid = dlSales.getSiteGuid().list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");

        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagShared.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_jReprintTickets.setVisible(false);

    }

    /**
     *
     */
    @Override
    public void activate() {
        mobileOrders = new MobileAppInfo();
        m_sCurrentTicket = null;
        selectValidTicket();

        m_jDelTicket.setEnabled(m_App.getAppUserView().getUser().hasPermission(
                "com.openbravo.pos.sales.JPanelTicketEdits"));

    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {

        saveCurrentTicket();
        mobileOrders = null;
        m_sCurrentTicket = null;
        m_panelticket.setActiveTicket(null, null);

        return true;
    }

    /**
     *
     */
    @Override
    public void deleteTicket() {
        if (m_sCurrentTicket == null) {

            dlSystem.execTicketRemoved(
                    new Object[]{
                        m_App.getAppUserView().getUser().getName(),
                        "Void",
                        "Ticket Deleted",
                        0.0,
                        g_siteguid
                    });
            try {
                dlSales.insertDatabaseChangeLog2().exec(UUID.randomUUID().toString(),
                        "I", "lineremoved", m_App.getAppUserView().getUser().getName(),
                        "name", dlSystem.getlastLineremoved(), "autoid", g_siteguid, 0, 0);
            } catch (BasicException ex) {
                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        m_sCurrentTicket = null;
        selectValidTicket();
    }

    /**
     *
     */
//    @Override
    public void deleteTickets() {

        dlSystem.execTicketRemoved(
                new Object[]{
                    m_App.getAppUserView().getUser().getName(),
                    "Void",
                    "Ticket Deleted",
                    0.0,
                    g_siteguid
                });
        try {
            dlSales.insertDatabaseChangeLog2().exec(UUID.randomUUID().toString(),
                    "I", "lineremoved", m_App.getAppUserView().getUser().getName(),
                    "name", "Ticket Deleted", "productname", g_siteguid, 0, 0);
        } catch (BasicException ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }

        m_sCurrentTicket = null;
        selectValidTicket();
    }

    public void updateCount() {
        try {
            List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
            int count = l.size();

            if (count > 0) {
                m_jListTickets.setText(Integer.toString(count));
            } else {
                m_jListTickets.setText("");
            }
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
            m_jListTickets.setText("");
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getBagComponent() {
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getNullComponent() {
        return new JPanel();
    }

    private void saveCurrentTicket() {

        if (m_sCurrentTicket != null) {
            try {
                dlReceipts.insertSharedTicket(m_sCurrentTicket,
                        m_panelticket.getActiveTicket(),
                        m_panelticket.getActiveTicket().getPickupId());
                m_jListTickets.setText("*");
                TicketInfo l = dlReceipts.getSharedTicket(m_sCurrentTicket);
                if (l.getLinesCount() == 0) {
                    dlReceipts.deleteSharedTicket(m_sCurrentTicket);
                }
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }

        updateCount();
    }

    private void setActiveTicket(String id) throws BasicException {

        TicketInfo ticket = dlReceipts.getSharedTicket(id);
        if (ticket == null) {
            m_jListTickets.setText("");
            throw new BasicException(AppLocal.getIntString("message.noticket"));
        } else {
            dlReceipts.getPickupId(id);
            Integer pickUp = dlReceipts.getPickupId(id);
            dlReceipts.deleteSharedTicket(id);
            m_sCurrentTicket = id;
            m_panelticket.setActiveTicket(ticket, null);
            ticket.setPickupId(pickUp);
        }

        updateCount();
    }

    private void setActiveReprintTicket(String id) throws BasicException {

        TicketInfo ticket = dlSales.getReprintTicket(id);
        m_sCurrentTicket = id;
    }

    private void selectValidTicket() {

        newTicket();
        updateCount();

        try {
            List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
            if (l.isEmpty()) {
                m_jListTickets.setText("");
                newTicket();
            } else {
                showList = m_App.getAppUserView().getUser().hasPermission("sales.ShowList");
                if (showList) {
                    m_jListTickets.doClick();
                }
            }
        } catch (BasicException e) {
            new MessageInf(e).show(this);
            newTicket();
        }
    }

    private void newTicket() {

        saveCurrentTicket();

        TicketInfo ticket = new TicketInfo();
        m_sCurrentTicket = UUID.randomUUID().toString();
        m_panelticket.setActiveTicket(ticket, null);

        updateCount();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jNewTicket = new javax.swing.JButton();
        m_jDelTicket = new javax.swing.JButton();
        m_jListTickets = new javax.swing.JButton();
        m_jReprintTickets = new javax.swing.JButton();
        m_jHold = new javax.swing.JButton();
        m_jMobileOrders = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        m_jNewTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_new.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jNewTicket.setToolTipText(bundle.getString("tooltip.addnewticket")); // NOI18N
        m_jNewTicket.setFocusPainted(false);
        m_jNewTicket.setFocusable(false);
        m_jNewTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jNewTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jNewTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jNewTicket.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jNewTicket.setRequestFocusEnabled(false);
        m_jNewTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jNewTicketActionPerformed(evt);
            }
        });
        jPanel1.add(m_jNewTicket);

        m_jDelTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_delete.png"))); // NOI18N
        m_jDelTicket.setToolTipText(bundle.getString("tooltip.delete")); // NOI18N
        m_jDelTicket.setFocusPainted(false);
        m_jDelTicket.setFocusable(false);
        m_jDelTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jDelTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jDelTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jDelTicket.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jDelTicket.setRequestFocusEnabled(false);
        m_jDelTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDelTicketActionPerformed(evt);
            }
        });
        jPanel1.add(m_jDelTicket);

        m_jListTickets.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jListTickets.setForeground(new java.awt.Color(255, 0, 153));
        m_jListTickets.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_pending.png"))); // NOI18N
        m_jListTickets.setToolTipText(bundle.getString("tooltip.layaway")); // NOI18N
        m_jListTickets.setFocusPainted(false);
        m_jListTickets.setFocusable(false);
        m_jListTickets.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jListTickets.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        m_jListTickets.setIconTextGap(1);
        m_jListTickets.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jListTickets.setRequestFocusEnabled(false);
        m_jListTickets.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jListTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListTicketsActionPerformed(evt);
            }
        });
        jPanel1.add(m_jListTickets);

        m_jReprintTickets.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jReprintTickets.setForeground(new java.awt.Color(255, 0, 153));
        m_jReprintTickets.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reprint24.png"))); // NOI18N
        m_jReprintTickets.setToolTipText(bundle.getString("tooltip.reprint")); // NOI18N
        m_jReprintTickets.setFocusPainted(false);
        m_jReprintTickets.setFocusable(false);
        m_jReprintTickets.setIconTextGap(1);
        m_jReprintTickets.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jReprintTickets.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jReprintTickets.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jReprintTickets.setRequestFocusEnabled(false);
        m_jReprintTickets.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jReprintTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jReprintTicketsActionPerformed(evt);
            }
        });
        jPanel1.add(m_jReprintTickets);

        m_jHold.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jHold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logout.png"))); // NOI18N
        m_jHold.setToolTipText(bundle.getString("tooltip.quicklogoff")); // NOI18N
        m_jHold.setFocusPainted(false);
        m_jHold.setFocusable(false);
        m_jHold.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jHold.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jHold.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jHold.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jHold.setRequestFocusEnabled(false);
        m_jHold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jHoldActionPerformed(evt);
            }
        });
        jPanel1.add(m_jHold);

        m_jMobileOrders.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jMobileOrders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/mobile.png"))); // NOI18N
        m_jMobileOrders.setToolTipText("<html><center><h4>Mobile Orders");
        m_jMobileOrders.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jMobileOrders.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jMobileOrders.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jMobileOrders.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jMobileOrders.setRequestFocusEnabled(false);
        m_jMobileOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jMobileOrdersActionPerformed(evt);
            }
        });
        jPanel1.add(m_jMobileOrders);

        add(jPanel1, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jListTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListTicketsActionPerformed

        SwingUtilities.invokeLater(() -> {
            try {
                if (!m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")) {
                    JOptionPane.showMessageDialog(null,
                            AppLocal.getIntString("message.sharedticket"),
                            AppLocal.getIntString("message.sharedtickettitle"),
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    
                    if ("0".equals(m_App.getAppUserView().getUser().getRole())
                            || "1".equals(m_App.getAppUserView().getUser().getRole())
                            || m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")
                            || m_App.getAppUserView().getUser().hasPermission("sales.Override")) {
                        List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
                        JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(JTicketsBagShared.this);
                        String id = listDialog.showTicketsList(l, dlReceipts);
                        
                        if (id != null) {
                            saveCurrentTicket();
                            setActiveTicket(id);
                        }
                    } else {
                        
                        String appuser = m_App.getAppUserView().getUser().getId();
                        List<SharedTicketInfo> l = dlReceipts.getUserSharedTicketList(appuser);
                        
                        JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(JTicketsBagShared.this);
                        
                        String id = listDialog.showTicketsList(l, dlReceipts);
                        
                        if (id != null) {
                            saveCurrentTicket();
                            setActiveTicket(id);
                        }
                    }
                }
            } catch (BasicException e) {
                new MessageInf(e).show(JTicketsBagShared.this);
                newTicket();
            }
        });

    }//GEN-LAST:event_m_jListTicketsActionPerformed

    private void m_jDelTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDelTicketActionPerformed

        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannadelete"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.YES_OPTION) {
// capture whole ticket delete into linesremoved            
            deleteTickets();
        }

    }//GEN-LAST:event_m_jDelTicketActionPerformed

    private void m_jNewTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jNewTicketActionPerformed

        newTicket();

    }//GEN-LAST:event_m_jNewTicketActionPerformed

    private void m_jHoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jHoldActionPerformed
        deactivate();
        ((JRootApp) m_App).closeAppView();
    }//GEN-LAST:event_m_jHoldActionPerformed

    private void m_jReprintTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jReprintTicketsActionPerformed
        SwingUtilities.invokeLater(() -> {
            try {
                if (!m_App.getAppUserView().getUser().hasPermission("sales.ReprintTicket")) {
                    JOptionPane.showMessageDialog(null,
                            AppLocal.getIntString("message.reprintticket"),
                            AppLocal.getIntString("message.reprinttickettitle"),
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    
                    if ("0".equals(m_App.getAppUserView().getUser().getRole())
                            || "1".equals(m_App.getAppUserView().getUser().getRole())
                            || m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")
                            || m_App.getAppUserView().getUser().hasPermission("sales.Override")) {
                        List<ReprintTicketInfo> l = dlSales.getReprintTicketList();
                        JTicketsReprintList listDialog = JTicketsReprintList.newJDialog(JTicketsBagShared.this);
                        String id = listDialog.showTicketsList(l, dlSales);
                        
                    } else {
                        
                        String appuser = m_App.getAppUserView().getUser().getId();
                        List<ReprintTicketInfo> l = dlSales.getReprintTicketList();
                        
                        JTicketsReprintList listDialog = JTicketsReprintList.newJDialog(JTicketsBagShared.this);
                        
                        String id = listDialog.showTicketsList(l, dlSales);
                        
                        if (id != null) {
                            saveCurrentTicket();
                            setActiveReprintTicket(id);
                        }
                    }
                }
            } catch (BasicException e) {
                new MessageInf(e).show(JTicketsBagShared.this);
                newTicket();
            }
        });
    }//GEN-LAST:event_m_jReprintTicketsActionPerformed
//Mobile orders
    public void setCurrentOrder(String id) {
        try {
            mobileOrders = dlMobileSales.getOrder(Integer.parseInt(id));
            if (mobileOrders.orderType.equals("delivery")) {

                setDeliveryOrder(id);

            }

        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagShared.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
// Mobile Delivery Orders 
// Inserting the ticket    
    private void setDeliveryOrder(String id) {
        try {
            TicketInfo ticket = new TicketInfo();
            
            ProductInfoExt product = new ProductInfoExt();
            
            mobileOrders = dlMobileSales.getOrder(Integer.parseInt(id));            
            List<MobileAppDetailInfo> ls = dlMobileSales.getOrderDetails(Integer.parseInt(id));
            for (int i = 0; i < ls.size(); i++) {

                product = dlSales.getProductInfo(ls.get(i).getProductId());
                TaxInfo tax = null;
                ticket.insertLine(i, new TicketLineInfo(product, ls.get(i).quantity, ls.get(i).price, tax,
                        (java.util.Properties) (product.getProperties().clone()), g_siteguid));
            }
            ticket.setTicketType(4);
            
            ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            
            ticket.setTicketId(Integer.parseInt(id));

                dlReceipts.insertSharedTicket(id, ticket, ticket.getPickupId());

                mobileOrders.setOrder_status("rinitiated");

                dlMobileSales.setOrderUpdate(mobileOrders);

        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagShared.class.getName()).log(Level.SEVERE, null, ex);
        }
        //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    private void m_jMobileOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jMobileOrdersActionPerformed
       SwingUtilities.invokeLater(()->{
           try{
//              if (!m_App.getAppUserView().getUser().hasPermission("sales.MobileList")) {
//                    JOptionPane.showMessageDialog(null,
//                            AppLocal.getIntString("message.mobileorders"),
//                            AppLocal.getIntString("message.mobileordertitle"),
//                            JOptionPane.INFORMATION_MESSAGE);
//                } else {
//                    
                    if ("0".equals(m_App.getAppUserView().getUser().getRole())
                            || "1".equals(m_App.getAppUserView().getUser().getRole())
                            || m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")
                            || m_App.getAppUserView().getUser().hasPermission("sales.Override")) {
                        List<MobileAppInfo> l = dlMobileSales.getOrders();
                        JMobileOrderList listDialog = JMobileOrderList.newJDialog(JTicketsBagShared.this);
                        String id = listDialog.showTicketsList(l, dlMobileSales);
                        
                        
                        if (id != null) {
                            setCurrentOrder(id);
                            saveCurrentTicket();
                            setActiveTicket(id);
                        }
                        
                        
                    } else {
                        
                        String appuser = m_App.getAppUserView().getUser().getId();
                        List<MobileAppInfo> l = dlMobileSales.getOrders();
                        
                        JMobileOrderList listDialog = JMobileOrderList.newJDialog(JTicketsBagShared.this);
                        
                        String id = listDialog.showTicketsList(l, dlMobileSales);
                        
                       if (id != null) {
                           setCurrentOrder(id);
                            saveCurrentTicket();
                            setActiveTicket(id);
                        }
                    }
//                }
           }catch (BasicException e){
               new MessageInf(e).show(JTicketsBagShared.this);
           }
       });
       
    }//GEN-LAST:event_m_jMobileOrdersActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton m_jDelTicket;
    private javax.swing.JButton m_jHold;
    private javax.swing.JButton m_jListTickets;
    private javax.swing.JButton m_jMobileOrders;
    private javax.swing.JButton m_jNewTicket;
    private javax.swing.JButton m_jReprintTickets;
    // End of variables declaration//GEN-END:variables

}
