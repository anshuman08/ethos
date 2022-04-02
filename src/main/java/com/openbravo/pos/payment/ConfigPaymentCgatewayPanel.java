/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.payment;

import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.util.AltEncrypter;
import javax.swing.JPanel;

/**
 *
 * @author premsarojanand
 */
public class ConfigPaymentCgatewayPanel extends javax.swing.JPanel implements PaymentConfiguration {

    /**
     * Creates new form ConfigPaymentCgatewayPanel
     */
    public ConfigPaymentCgatewayPanel() {
        initComponents();
    }
    
    /**
     *
     * @return
     */
    @Override
    public JPanel getComponent() {  
        return this;
    }
    
    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {
        String sCommerceID = config.getProperty("payment.commerceid");
        String sCommercePass = config.getProperty("payment.commercepassword");
        
        if (sCommerceID != null && sCommercePass != null && sCommercePass.startsWith("crypt:")) {
            m_jUser.setText(config.getProperty("payment.commerceid"));
            m_jPassword.setText(config.getProperty("payment.commercepassword"));
            m_jOrgcode.setText(config.getProperty("payment.orgCode"));
            m_jAppkey.setText(config.getProperty("payment.appkey"));
            m_jDeviceNo.setText(config.getProperty("payment.deviceid"));
        }
    }
    
    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {
        config.setProperty("payment.commerceid", m_jUser.getText());      
        config.setProperty("payment.commercepassword", m_jPassword.getText());
        config.setProperty("payment.orgCode",m_jOrgcode.getText());
        config.setProperty("payment.appkey", m_jAppkey.getText());
        config.setProperty("payment.deviceid", m_jDeviceNo.getText());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        m_jUser = new javax.swing.JTextField();
        m_jAppkey = new javax.swing.JTextField();
        m_jOrgcode = new javax.swing.JTextField();
        Device = new javax.swing.JLabel();
        m_jDeviceNo = new javax.swing.JTextField();
        m_jPassword = new javax.swing.JTextField();

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText("UserName");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Password");

        jLabel3.setText("APPKEY");

        jLabel4.setText("ORGCODE");

        m_jUser.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        m_jAppkey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        m_jOrgcode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        Device.setText("Device No");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(64, 64, 64)
                        .addComponent(m_jUser, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(Device))
                        .addGap(69, 69, 69)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_jAppkey, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                            .addComponent(m_jOrgcode, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                            .addComponent(m_jDeviceNo)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(m_jPassword)
                                .addGap(1, 1, 1)))))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(m_jUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(m_jPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(m_jAppkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3))
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jOrgcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Device)
                    .addComponent(m_jDeviceNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(80, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Device;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField m_jAppkey;
    private javax.swing.JTextField m_jDeviceNo;
    private javax.swing.JTextField m_jOrgcode;
    private javax.swing.JTextField m_jPassword;
    private javax.swing.JTextField m_jUser;
    // End of variables declaration//GEN-END:variables
}
