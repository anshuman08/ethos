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
//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2017 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
package com.openbravo.pos.sales.restaurant;

import bsh.Interpreter;
import bsh.EvalError;
import com.alee.extended.time.ClockType;
import com.alee.extended.time.WebClock;
import com.alee.managers.notification.NotificationIcon;
import com.alee.managers.notification.NotificationManager;
import com.alee.managers.notification.WebNotification;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.JPanelTicket;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.sales.mobileorders.DatalogicMobileAppSales;
import com.openbravo.pos.sales.mobileorders.MobileAppInfo;
import com.openbravo.pos.sales.restaurant.dao.PosSynchronizationPoll;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.json.JSONObject;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagRestaurant extends javax.swing.JPanel {

    private final AppView m_App;
    private final JTicketsBagRestaurantMap m_restaurant;
    private List<TicketLineInfo> m_aLines;
    private TicketLineInfo line;
    private TicketInfo ticket;
    private final Object ticketExt;
    private DataLogicSystem m_dlSystem = null;
    private final DeviceTicket m_TP;
    private final TicketParser m_TTP2;
    private final RestaurantDBUtils restDB;

    private final DataLogicSystem dlSystem = null;
    private final DataLogicReceipts dlReceipts = null;
    private DatalogicMobileAppSales m_dlMobile = null;
    private final MobileAppInfo mobileAppinfo;
    private DataLogicSales dlSales = null;

    private TicketParser m_TTP;

    private SentenceList senttax;
    private ListKeyed taxcollection;
    private TaxesLogic taxeslogic;

    public String siteguid = null;

    private Interpreter i;

    /**
     * Creates new form JTicketsBagRestaurantMap
     *
     * @param app
     * @param restaurant
     */
    public JTicketsBagRestaurant(AppView app, JTicketsBagRestaurantMap restaurant) {
        super();
        m_App = app;
        m_restaurant = restaurant;

        initComponents();

        ticketExt = null;

        restDB = new RestaurantDBUtils(m_App);

        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        DataLogicReceipts m_dlReceipts = (DataLogicReceipts) m_App.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        m_dlMobile = (DatalogicMobileAppSales) m_App.getBean("com.openbravo.pos.sales.mobileorders.DatalogicMobileAppSales");

        try {

            //siteguid
            siteguid = dlSales.getSiteGuid().list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");

        } catch (BasicException ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        mobileAppinfo = null;

        m_TP = new DeviceTicket();
        m_TTP2 = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);

        j_btnKitchen.setVisible(false);

        m_TablePlan.setVisible(m_App.getAppUserView().getUser().
                hasPermission("sales.TablePlan"));

    }

    /**
     *
     */
    public void activate() {

        m_DelTicket.setEnabled(m_App.getAppUserView().getUser()
                .hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));

        m_TablePlan.setEnabled(m_App.getAppUserView().getUser()
                .hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        m_TablePlan.setVisible(true);
    }

    /**
     *
     * @param pTicket
     * @return
     */
    public String getPickupString(TicketInfo pTicket) {
        if (pTicket == null) {
            return ("0");
        }
        String tmpPickupId = Integer.toString(pTicket.getPickupId());
        String pickupSize = (m_App.getProperties().getProperty("till.pickupsize"));
        if (pickupSize != null && (Integer.parseInt(pickupSize) >= tmpPickupId.length())) {
            while (tmpPickupId.length() < (Integer.parseInt(pickupSize))) {
                tmpPickupId = "0" + tmpPickupId;
            }
        }
        return (tmpPickupId);
    }

    /**
     *
     * @param resource
     */
    public void printTicket(String resource) {
        printTicket(resource, ticket, m_restaurant.getTable());
        printNotify();
        j_btnKitchen.setEnabled(false);
    }

    private void printTicket(String sresourcename, TicketInfo ticket, String table) {
        if (ticket != null) {

            if (ticket.getPickupId() == 0) {
                try {
                    ticket.setPickupId(dlSales.getNextPickupIndex());
                } catch (BasicException e) {
                    ticket.setPickupId(0);
                }
            }

            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);

                script.put("ticket", ticket);
                script.put("place", m_restaurant.getTableName());
                script.put("pickupid", getPickupString(ticket));

                m_TTP2.printTicket(script.eval(m_dlSystem.getResourceAsXML(sresourcename)).toString());

            } catch (ScriptException | TicketPrinterException e) {
                JMessageDialog.showMessage(this,
                        new MessageInf(MessageInf.SGN_NOTICE,
                                AppLocal.getIntString("message.cannotprint"), e));
            }
        }
    }

    public void printNotify() {
        final WebNotification notificationPopup = new WebNotification();
        notificationPopup.setIcon(NotificationIcon.information);
        notificationPopup.setDisplayTime(4000);

        final WebClock clock = new WebClock();
        clock.setClockType(ClockType.timer);
        clock.setTimeLeft(5000);
        clock.setTimePattern("'Printed successfully'");
        notificationPopup.setContent(clock);

        NotificationManager.showNotification(notificationPopup);
        clock.start();
    }

    public int cancelOrder(TicketInfo ticket) {
        int j = 0;
        try {
            MobileAppInfo mobileAppinfo = new MobileAppInfo();
            mobileAppinfo = m_dlMobile.getOrder(ticket.getTicketId());
            mobileAppinfo.setOrder_status("cancelled");
            HashMap<String, String> content = new HashMap<>();
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImY4NTYxMmFkOTVmYTYxZDk4YTI2OTg5OTNiMzE1MTZkMDUxM2JhZGE0NDU2ZTIxOTVhYjgyNDVhYzk5N2Q1MmE4NDgxMDhmYjQxNGM1YzcyIn0.eyJhdWQiOiIxIiwianRpIjoiZjg1NjEyYWQ5NWZhNjFkOThhMjY5ODk5M2IzMTUxNmQwNTEzYmFkYTQ0NTZlMjE5NWFiODI0NWFjOTk3ZDUyYTg0ODEwOGZiNDE0YzVjNzIiLCJpYXQiOjE1NDY3MDY3MjIsIm5iZiI6MTU0NjcwNjcyMiwiZXhwIjoxNTc4MjQyNzIxLCJzdWIiOiIxIiwic2NvcGVzIjpbIioiXX0.GrLHc7PEodeFOrKOqc1ZVa8lfXpB9hVwrXiVcqXwk20RwGZEh3ZjDRwENCkr0asWI6ss-22iWRCX8xBBGjV0f5Kuw6pbI1u2fCi1bFge0OJVoGH9E4fmUSo1B8FOm86_7xyCu4VkFvajTeWySDNUFJIKxlMc0Bev1nloqqhutzdAMgCFFwrTZNMyrq17khcSHe4aoqTz5jjPFmLSC9jbKiRorlORkJKRFLMWoeg9yQwRQfH7f_Bzp_G1d93uBHMdA1PYnFbCTaSutynzMgSHukpbENb0Yx8hU5KOqhfzaHhPIkzXbLEf1Mnt9jWwGMco5v7QFBwUJz3f1isqdVJ4xx9_9b2Q80c-LWHMamQy_wRQ9o8DkfIqilcQJVB7GOJCavcVHu57CMuWfLKsEEAQ5NIJP_PjwdRIx9iTVgWS63JKIKv5vCE1w9ljm-1bQHg10NFfDHutQs7WL0Ud2Ox1Ls-0XBjWJz-umK7eeRwWHul9urQpdMYEBQ4d5PUn4SIjw89Tyu7XjePPmyrubz2ZAOj80q-sS446ictN90NLyZGvy2bo_lzrcXOMflcZfs9wO-xVzHIFPCUsq64CJBQ7jUEYfoduCpYu8KwrCH61Xi1JxKJUhF0zX0gJJxR_mQ_q2e92DxZ30bxApibb4WlkVfVzRYgAWM4i6mqhuTEdS6M";
            URL url1 = new URL("http://apis.ethosmiracle.com/api/orders/update");
            // 2. Open connection
            HttpURLConnection conn;
            conn = (HttpURLConnection) url1.openConnection();
            // 3. Specify POST method
            conn.setRequestMethod("POST");
            // 4. Set the headers
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("siteguid", siteguid);
            json.put("orderNo", mobileAppinfo.getOrderNo());
            json.put("totalValue", mobileAppinfo.getTotal_value());
            json.put("totalTax", mobileAppinfo.getTotalTax());
            json.put("totalItems", mobileAppinfo.getTotalItems());
            json.put("totalDiscount", mobileAppinfo.getTotalDiscount());
            json.put("orderStatus", "cancelled");
            json.put("paymentMethod", mobileAppinfo.getPayment_method());
            json.put("transactionId", mobileAppinfo.getTransaction_id());
            json.put("paymentStatus", mobileAppinfo.getPayment_status());
            json.put("couponDiscount", mobileAppinfo.getCouponDiscount());

            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11" + json);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : json.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            conn.getOutputStream().write(postDataBytes);
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                sb.append((char) c);
            }
            JSONObject obj = new JSONObject(sb.toString());
            if (obj.get("status").toString().equals("1")) {
                m_dlMobile.setOrderUpdate(mobileAppinfo);
                j = 1;
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagRestaurant.class.getName()).log(Level.SEVERE, null, ex);
        }

        return j;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_TablePlan = new javax.swing.JButton();
        m_MoveTable = new javax.swing.JButton();
        m_DelTicket = new javax.swing.JButton();
        j_btnKitchen = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setMinimumSize(new java.awt.Dimension(250, 50));
        setPreferredSize(new java.awt.Dimension(350, 50));

        m_TablePlan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/tables.png"))); // NOI18N
        m_TablePlan.setToolTipText("Go to Table Plan");
        m_TablePlan.setFocusPainted(false);
        m_TablePlan.setFocusable(false);
        m_TablePlan.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_TablePlan.setMaximumSize(new java.awt.Dimension(50, 40));
        m_TablePlan.setMinimumSize(new java.awt.Dimension(50, 40));
        m_TablePlan.setPreferredSize(new java.awt.Dimension(80, 45));
        m_TablePlan.setRequestFocusEnabled(false);
        m_TablePlan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_TablePlanActionPerformed(evt);
            }
        });
        add(m_TablePlan);

        m_MoveTable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/movetable.png"))); // NOI18N
        m_MoveTable.setToolTipText("Move Table");
        m_MoveTable.setFocusPainted(false);
        m_MoveTable.setFocusable(false);
        m_MoveTable.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_MoveTable.setMaximumSize(new java.awt.Dimension(50, 40));
        m_MoveTable.setMinimumSize(new java.awt.Dimension(50, 40));
        m_MoveTable.setPreferredSize(new java.awt.Dimension(80, 45));
        m_MoveTable.setRequestFocusEnabled(false);
        m_MoveTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_MoveTableActionPerformed(evt);
            }
        });
        add(m_MoveTable);

        m_DelTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_delete.png"))); // NOI18N
        m_DelTicket.setToolTipText("Delete Current Order");
        m_DelTicket.setFocusPainted(false);
        m_DelTicket.setFocusable(false);
        m_DelTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_DelTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_DelTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_DelTicket.setPreferredSize(new java.awt.Dimension(80, 45));
        m_DelTicket.setRequestFocusEnabled(false);
        m_DelTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_DelTicketActionPerformed(evt);
            }
        });
        add(m_DelTicket);

        j_btnKitchen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24.png"))); // NOI18N
        j_btnKitchen.setToolTipText("Send to Kichen Printer");
        j_btnKitchen.setMargin(new java.awt.Insets(0, 4, 0, 4));
        j_btnKitchen.setMaximumSize(new java.awt.Dimension(50, 40));
        j_btnKitchen.setMinimumSize(new java.awt.Dimension(50, 40));
        j_btnKitchen.setPreferredSize(new java.awt.Dimension(80, 45));
        j_btnKitchen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_btnKitchenActionPerformed(evt);
            }
        });
        add(j_btnKitchen);
        j_btnKitchen.getAccessibleContext().setAccessibleDescription("Send to Remote Printer");
    }// </editor-fold>//GEN-END:initComponents

    private void m_MoveTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_MoveTableActionPerformed

// JG 6 Nov 13 - clear Customer from orignal table - Thanks David Kurniawan
        restDB.clearCustomerNameInTableById(m_restaurant.getTable());
        restDB.clearWaiterNameInTableById(m_restaurant.getTable());

        restDB.setTableMovedFlag(m_restaurant.getTable());
        m_restaurant.moveTicket();

    }//GEN-LAST:event_m_MoveTableActionPerformed

    @SuppressWarnings("empty-statement")
    private void m_DelTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_DelTicketActionPerformed

        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannadelete"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        ticket = m_restaurant.getActiveTicket();
        if (res == JOptionPane.YES_OPTION) {
            if (ticket != null) {
                if (ticket.getTicketType() == 4) {
                   cancelOrder(ticket);
                }
            }
            restDB.clearCustomerNameInTableById(m_restaurant.getTable());
            restDB.clearWaiterNameInTableById(m_restaurant.getTable());
            restDB.clearTicketIdInTableById(m_restaurant.getTable());
            m_restaurant.deleteTicket();

        } else {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_NOTICE,
                            AppLocal.getIntString("message.cannotcancel")));
        }
    }//GEN-LAST:event_m_DelTicketActionPerformed

    private void m_TablePlanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_TablePlanActionPerformed

        m_restaurant.newTicket();
    }//GEN-LAST:event_m_TablePlanActionPerformed

    @SuppressWarnings("empty-statement")
    private void j_btnKitchenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_btnKitchenActionPerformed
        ticket = m_restaurant.getActiveTicket();
        String rScript = (m_dlSystem.getResourceAsText("script.SendOrder"));

        Interpreter i = new Interpreter();
        try {
            i.set("ticket", ticket);
            i.set("place", m_restaurant.getTableName());
            i.set("user", m_App.getAppUserView().getUser());
            i.set("sales", this);
            i.set("pickupid", ticket.getPickupId());
            Object result = i.eval(rScript);
        } catch (EvalError ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Autologoff after sales            
        String autoLogoff = (m_App.getProperties().getProperty("till.autoLogoff"));
        String autoLogoffRestaurant = (m_App.getProperties().getProperty("till.autoLogoffrestaurant"));
        if (autoLogoff != null) {
            if (autoLogoff.equals("true")) {
                // check how far to logoof to ie tables or application
                if (autoLogoffRestaurant == null) {
                    ((JRootApp) m_App).closeAppView();
                } else if (autoLogoffRestaurant.equals("true")) {
                    m_restaurant.newTicket();
                } else {
                    ((JRootApp) m_App).closeAppView();
                }
            }
        }
    }//GEN-LAST:event_j_btnKitchenActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton j_btnKitchen;
    private javax.swing.JButton m_DelTicket;
    private javax.swing.JButton m_MoveTable;
    private javax.swing.JButton m_TablePlan;
    // End of variables declaration//GEN-END:variables

}
