/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.beans;

import com.mysql.jdbc.PreparedStatement;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.util.AltEncrypter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.apache.commons.validator.EmailValidator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

/**
 *
 * @author Elegenze
 */
public class JActivationDialog extends javax.swing.JDialog {

    private static LocaleResources m_resources;

    private String db_url;
    private String db_user;
    private String db_password;
    private Connection localConnection;
    private String m_siteguid;

    /**
     * Creates new form JActivationDialog
     *
     * @param parent
     * @param modal
     */
    public JActivationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        init();
    }

    /**
     * Creates new form JActivationDialog
     *
     * @param parent
     * @param modal
     */
    public JActivationDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        init();
    }

    private void init() {

        if (m_resources == null) {
            m_resources = new LocaleResources();
            m_resources.addBundleName("beans_messages");
        }
        AppConfig appConfig = new AppConfig(new String[]{});
        appConfig.load();
        this.db_url = appConfig.getProperty("db.URL");
        this.db_user = appConfig.getProperty("db.user");
        this.db_password = appConfig.getProperty("db.password");
        if (db_user != null && db_password != null && db_password.startsWith("crypt:")) {

            AltEncrypter cypher = new AltEncrypter("cypherkey" + db_user);
            db_password = cypher.decrypt(db_password.substring(6));
        }
        try {
            localConnection = DriverManager.getConnection(db_url, db_user, db_password);
        } catch (SQLException ex) {
            Logger.getLogger(JActivationDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

    }

    private void setTitle(String title, String message, Icon icon) {
        setTitle(title);
        m_lblMessage.setText(message);
        m_lblMessage.setIcon(icon);
    }

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    public static String showActivation(Component parent, String title, String message) {

        Window window = getWindow(parent);

        JActivationDialog myMsg;
        if (window instanceof Frame) {
            myMsg = new JActivationDialog((Frame) window, true);
        } else {
            myMsg = new JActivationDialog((Dialog) window, true);
        }

//        myMsg.setTitle(title, message, icon);
        myMsg.setVisible(true);
        return myMsg.m_siteguid;
    }
    
    
    public static String generateLicenseKey() throws Exception {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        
        String vendor = operatingSystem.getManufacturer();
        String processorSerialNumber = centralProcessor.getProcessorID();
        String processorIdentifier = centralProcessor.getIdentifier();
        int processors = centralProcessor.getLogicalProcessorCount();        
        String delimiter = "#";        
        String licenseKey = vendor + delimiter + processorSerialNumber + delimiter + processorIdentifier + delimiter + processors;
        return licenseKey;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_lblMessage = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jApplicationKey = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jEmailId = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jValidate = new javax.swing.JButton();
        jRenewal_Pay = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText("Application Key");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Email ID");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("AND");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 0, 255));
        jLabel4.setText("<html><center>Enter the Application Key and  the Email address used for  purchasing the Software.\n<br/> In Case you haven't recievied it kindly contact info@ethosteck.com  purchasing the software. \n<br/> If any queries kindly mail us back with purchase invoice on info@ethosteck.com.<br/> It takes us maximum 24 hrs to revert back.</center></html>");

        jValidate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jValidate.setText("Validate");
        jValidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jValidateActionPerformed(evt);
            }
        });

        jRenewal_Pay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customerpay.png"))); // NOI18N
        jRenewal_Pay.setText("Renewal Payment");
        jRenewal_Pay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRenewal_PayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(208, 208, 208))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_lblMessage)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jValidate, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(jRenewal_Pay, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jApplicationKey, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                            .addComponent(jEmailId))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(m_lblMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jApplicationKey, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEmailId, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRenewal_Pay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jValidate, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jValidateActionPerformed
        // TODO add your handling code here:
        EmailValidator validator = EmailValidator.getInstance();
        if (jApplicationKey.getText().isEmpty() && jEmailId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Please Enter Application Key or Email id",
                    "Empty",
                    JOptionPane.WARNING_MESSAGE);

        } else if (jEmailId.getText().length() > 0 && !validator.isValid(jEmailId.getText())) {
            JOptionPane.showMessageDialog(null,
                    "Invalid Email ID",
                    "Wrong",
                    JOptionPane.WARNING_MESSAGE);
        } else {
          m_siteguid =  checkActivation(jApplicationKey.getText(), jEmailId.getText());
        }
    }//GEN-LAST:event_jValidateActionPerformed

    private void jRenewal_PayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRenewal_PayActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jRenewal_PayActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jApplicationKey;
    private javax.swing.JTextField jEmailId;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton jRenewal_Pay;
    private javax.swing.JButton jValidate;
    private javax.swing.JLabel m_lblMessage;
    // End of variables declaration//GEN-END:variables

    private String checkActivation(String appkey, String emid) {
        String siteguid = null;
        String tablename = null;
        try {
            String url = "http://13.235.167.89/apis/activate.php";
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjU3YjJiZTQwYjU0ZjZiYmFlZjc5ZTE4Y2E5YTFmMDJkMzNjODJkZjEzZjNmNGRkZWQ5ZmVlYTgyM2FmZmUxMmIzNzkxZDIxOTNjZjdkYjY4In0.eyJhdWQiOiIxIiwianRpIjoiNTdiMmJlNDBiNTRmNmJiYWVmNzllMThjYTlhMWYwMmQzM2M4MmRmMTNmM2Y0ZGRlZDlmZWVhODIzYWZmZTEyYjM3OTFkMjE5M2NmN2RiNjgiLCJpYXQiOjE1NjA5NjMzNTAsIm5iZiI6MTU2MDk2MzM1MCwiZXhwIjoxNTkyNTg1NzUwLCJzdWIiOiIxIiwic2NvcGVzIjpbIioiXX0.aVPXyzY2ZQ8wONps6BAMxnKgII1CWMfET4NEcwfPU7Z37MnvM-aq1U-o1nvAffG9AE9jgg05h1SUnq5sRga3Vyab8B0p2DEk5blzTSUjnzuK-sWsIKM5EsqtHsAPkBBT5vyBAMXl64-ecxSfUEu03XB6qgny8gzsJI29P1-oDEOt8ztw6xy_uHHSDX1gkNiHOSVTbx8nKw3_CpHACZLdHe30K23CJjMgc8mvP0G7lnypXk9Hjh7Z4SU2qUwIllpcuJzoN2tIj3P-Tb-T6BaLiqFbn3EDBXaTybPfcyjgYp8jgOjn2y4DbczNFjoCVNlGkRsDK3V4ycrepaOfn3_48oxHW8e4KoeMqYUvhz2lNKHHRdwXlYRnZE2Xi1ih9Q_T1efAJH6Ycu0AICY5ccBU3K9OHi5K9CeyE79QD_T_9Ojxq3L4v28u4c0o2Ipy8BixTdU4Wc9UnW4c30JqA-Wp9y1I9N739Ru5u9-GRmA0R6GTOnuawH_8emK_A3gR2b4qTGLKueExRn7mmGx75ORM3AhIe-7fv85gaHer0fj1JBwrjnMVT2kIc3Cg_0TGlxbI5HxuQ6GJ-7OjQCSwlRt3-TQRhwnP0khZ7kyouSuC1AOa-as6PU3YJG732xhwfKdQZDDRVcd5A66WKlpn-Nqg-1PhHs_Apmvikvil9MpqxCY";
            URL url1 = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) url1.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("applicationKey", appkey);
            json.put("emailId", emid);
            json.put("identity",generateLicenseKey());
            
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : json.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            System.out.println("*********************" + json);
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            conn.getOutputStream().write(postDataBytes);
            
            System.out.println("POST data *********************" + postData.toString());
            
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                sb.append((char) c);
            }
            System.out.println("*****************************************" + sb.toString());
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(sb.toString());
            if (obj.get("status").toString().equals("1")) {
                siteguid = obj.get("siteguid").toString();
                String SQl = "update siteguid set siteguid = " + '"' + siteguid + '"';
                java.sql.PreparedStatement sv = localConnection.prepareStatement(SQl);
                sv.execute();
                DatabaseMetaData md = localConnection.getMetaData();
                ResultSet rs = md.getTables(null, null, "%", null);
                while (rs.next()) {
                    tablename = rs.getString(3);
                    java.sql.PreparedStatement tablesData = localConnection.prepareStatement("select * from " + tablename + " where sflag = 0");
                    ResultSet rs_local = tablesData.executeQuery();
                    while (rs_local.next()) {
                        java.sql.PreparedStatement tableUpdate = null;
                        java.sql.PreparedStatement tableInsert = null;
                        switch (tablename) {
                            case "attribute":
                                tableUpdate = localConnection.prepareStatement("update attribute set sflag = 1, siteguid = " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attribute");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();

                                break;
                            case "attributed_stock":
                                tableUpdate = localConnection.prepareStatement("update attributed_stock  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributed_stock");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("product_id"));
                                tableInsert.setString(7, "product_id");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "attributed_stock_attributes":
                                tableUpdate = localConnection.prepareStatement("update attributed_stock_attributes  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributed_stock_attributes");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("attr_value_id"));
                                tableInsert.setString(7, "attr_value_id");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "attributeinstance":
                                tableUpdate = localConnection.prepareStatement("update attributeinstance  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributeinstance");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("attribute_id"));
                                tableInsert.setString(7, "attribute_id");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "attributeset":
                                tableUpdate = localConnection.prepareStatement("update attributeset  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributeset");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "attributesetinstance":
                                tableUpdate = localConnection.prepareStatement("update attributesetinstance  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributesetinstance");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("attributeset_id"));
                                tableInsert.setString(7, "attributeset_id");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "attributeuse":
                                tableUpdate = localConnection.prepareStatement("update attributeuse  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributeuse");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("attribute_id"));
                                tableInsert.setString(7, "attribute_id");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "attributevalue":
                                tableUpdate = localConnection.prepareStatement("update attributevalue  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "attributevalue");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("attribute_id"));
                                tableInsert.setString(7, "attribute_id");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "breaks":
                                tableUpdate = localConnection.prepareStatement("update breaks  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "breaks");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "categories":
                                tableUpdate = localConnection.prepareStatement("update categories  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "categories");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "closedcash":
                                tableUpdate = localConnection.prepareStatement("update closedcash  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "closedcash");
                                tableInsert.setString(4, rs_local.getString("money"));
                                tableInsert.setString(5, "money");
                                tableInsert.setString(6, rs_local.getString("host"));
                                tableInsert.setString(7, "host");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "customers":
                                tableUpdate = localConnection.prepareStatement("update customers  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "customers");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("searchkey"));
                                tableInsert.setString(7, "searchkey");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "draweropened":
                                tableUpdate = localConnection.prepareStatement("update draweropened  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "draweropened");
                                tableInsert.setString(4, rs_local.getString("name"));
                                tableInsert.setString(5, "name");
                                tableInsert.setString(6, rs_local.getString("ticketid"));
                                tableInsert.setString(7, "ticketid");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
//                            case "floors":
//                                tableUpdate = localConnection.prepareStatement("update floors  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
//                                tableUpdate.execute();
//                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//                                tableInsert.setString(1, UUID.randomUUID().toString());
//                                tableInsert.setString(2, "I");
//                                tableInsert.setString(3, "floors");
//                                tableInsert.setString(4, rs_local.getString("id"));
//                                tableInsert.setString(5, "id");
//                                tableInsert.setString(6, rs_local.getString("name"));
//                                tableInsert.setString(7, "name");
//                                tableInsert.setString(8, siteguid);
//                                tableInsert.setInt(9, 0);
//                                tableInsert.setInt(10, 0);
//                                tableInsert.execute();
//                                break;
                            case "lineremoved":
                                tableUpdate = localConnection.prepareStatement("update lineremoved  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "lineremoved");
                                tableInsert.setString(4, rs_local.getString("autoid"));
                                tableInsert.setString(5, "autoid");
                                tableInsert.setString(6, rs_local.getString("productname"));
                                tableInsert.setString(7, "productname");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "locations":
                                tableUpdate = localConnection.prepareStatement("update locations  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "locations");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "people":
                                tableUpdate = localConnection.prepareStatement("update people  set sflag = 1 , siteguid = " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "people");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "places":
                                tableUpdate = localConnection.prepareStatement("update places  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "places");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "payments":
                                tableUpdate = localConnection.prepareStatement("update payments  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "payments");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("receipt"));
                                tableInsert.setString(7, "receipt");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "products":
                                tableUpdate = localConnection.prepareStatement("update products  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "products");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("reference"));
                                tableInsert.setString(7, "reference");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "products_bundle":
                                tableUpdate = localConnection.prepareStatement("update products_bundle  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "products_bundle");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("product"));
                                tableInsert.setString(7, "product");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "receipts":
                                tableUpdate = localConnection.prepareStatement("update receipts  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "receipts");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("money"));
                                tableInsert.setString(7, "money");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "reservation_customers":
                                tableUpdate = localConnection.prepareStatement("update reservation_customers  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "reservation_customers");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("customer"));
                                tableInsert.setString(7, "customer");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "reservations":
                                tableUpdate = localConnection.prepareStatement("update reservations  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "reservations");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("title"));
                                tableInsert.setString(7, "title");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "shift_break":
                                tableUpdate = localConnection.prepareStatement("update shift_break  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "shift_break");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("shiftid"));
                                tableInsert.setString(7, "shiftid");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "shifts":
                                tableUpdate = localConnection.prepareStatement("update shifts  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "shifts");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("pplid"));
                                tableInsert.setString(7, "pplid");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "stockcurrent":
                                tableUpdate = localConnection.prepareStatement("update stockcurrent  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "stockcurrent");
                                tableInsert.setString(4, rs_local.getString("linenumber"));
                                tableInsert.setString(5, "linenumber");
                                tableInsert.setString(6, siteguid);
                                tableInsert.setString(7, "siteguid");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "stockdiary":
                                tableUpdate = localConnection.prepareStatement("update stockdiary  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "stockdiary");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("product"));
                                tableInsert.setString(7, "product");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "stocklevel":
                                tableUpdate = localConnection.prepareStatement("update stocklevel  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "stocklevel");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("product"));
                                tableInsert.setString(7, "product");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "suppliers":
                                tableUpdate = localConnection.prepareStatement("update suppliers  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "suppliers");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("searchkey"));
                                tableInsert.setString(7, "searchkey");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "taxcategories":
                                tableUpdate = localConnection.prepareStatement("update taxcategories  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "taxcategories");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "taxcustcategories":
                                tableUpdate = localConnection.prepareStatement("update taxcustcategories  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "taxcustcategories");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "taxes":
                                tableUpdate = localConnection.prepareStatement("update taxes  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "taxes");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("category"));
                                tableInsert.setString(7, "category");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "taxlines":
                                tableUpdate = localConnection.prepareStatement("update taxlines  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "taxlines");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("receipt"));
                                tableInsert.setString(7, "receipt");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "thirdparties":
                                tableUpdate = localConnection.prepareStatement("update thirdparties  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "thirdparties");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "ticketlines":
                                tableUpdate = localConnection.prepareStatement("update ticketlines  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "ticketlines");
                                tableInsert.setString(4, rs_local.getString("ticket"));
                                tableInsert.setString(5, "ticket");
                                tableInsert.setString(6, rs_local.getString("line"));
                                tableInsert.setString(7, "line");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "tickets":
                                tableUpdate = localConnection.prepareStatement("update tickets  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "tickets");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("ticketid"));
                                tableInsert.setString(7, "ticketid");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "uom":
                                tableUpdate = localConnection.prepareStatement("update uom  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "uom");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("name"));
                                tableInsert.setString(7, "name");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            case "vouchers":
                                tableUpdate = localConnection.prepareStatement("update vouchers  set sflag = 1 , siteguid =  " + '"' + siteguid + '"');
                                tableUpdate.execute();
                                tableInsert = localConnection.prepareStatement("insert into databasechangelog (`id`,`process`, `tablename`,`table_pk_id`,`tablepk_name` , `table_scnd_id`, `table_scnd_name`, `siteguid`, `sflag` , `lflag`) values (? , ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                tableInsert.setString(1, UUID.randomUUID().toString());
                                tableInsert.setString(2, "I");
                                tableInsert.setString(3, "vouchers");
                                tableInsert.setString(4, rs_local.getString("id"));
                                tableInsert.setString(5, "id");
                                tableInsert.setString(6, rs_local.getString("customer"));
                                tableInsert.setString(7, "customer");
                                tableInsert.setString(8, siteguid);
                                tableInsert.setInt(9, 0);
                                tableInsert.setInt(10, 0);
                                tableInsert.execute();
                                break;
                            default:

                                break;

                        }

                    }
                }
                JOptionPane.showMessageDialog(JActivationDialog.this,
                obj.get("message").toString());
                
            }else{
                 JOptionPane.showMessageDialog(JActivationDialog.this,
                obj.get("message").toString());
            }
        } catch (SQLException | IOException | ParseException ex) {
            Logger.getLogger(JActivationDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(JActivationDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        setVisible(false);
        dispose();
        return siteguid;
    }
}
