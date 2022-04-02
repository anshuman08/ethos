/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.payment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.util.AltEncrypter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author premsarojanand
 */
public class PaymentGatewayEzeeTab implements PaymentGateway {

    private static final String SALE = "SALE";
    private static final String REFUND = "CREDIT";

    private static String HOST;

    private String m_userName; //Path of the .12 file

    private String m_userPassword; //Password used creating .12 file

    private String m_appKey; //StoreName

    private String m_orgCode;//Organization code

    private String m_devicesno;

    private boolean m_bTestMode;
    private static String APPROVED = "APPROVED";

    public PaymentGatewayEzeeTab(AppProperties props) {
        this.m_bTestMode = Boolean.parseBoolean(props.getProperty("payment.testmode"));
        m_userName = props.getProperty("payment.commerceid");
        m_userPassword = props.getProperty("payment.commercepassword");
        m_appKey = props.getProperty("payment.appkey");
        m_orgCode = props.getProperty("payment.orgcode");
        m_devicesno = props.getProperty("payment.deviceid");
    }

    public PaymentGatewayEzeeTab() {

    }

    @Override
    public void execute(PaymentInfoMagcard payinfo) {
        URL url;
        URLConnection connection;
        DataOutputStream out;
        System.out.println("I amin Ezee Tab Payment Gateway");
        StringBuilder sb = new StringBuilder();

        try {
            NumberFormat formatter = new DecimalFormat("0000.00");
            String amount = formatter.format(Math.abs(payinfo.getTotal()));
            System.out.println("device No" + m_devicesno);
            sb.append("{\"username\" : \"" + m_userName + "\", \"appKey\":\"" + m_appKey + "\",\"orgCode\":\"" + m_orgCode + "\",\"amount\": \"" + amount + "\",\"customerMobile\":\"\",\"externalRefNumber\":\"" + payinfo.getTransactionID() + "\",\"externalRefNumber2\":\"TKTBLRTOCHNI\",\"externalRefNumber3\":\"CNDTRNAME\",\"emailId\":\"\",\"pushTo\":{\"deviceId\":\"" + m_devicesno + "\"}}");

            //sb.append("&cvv="); //card security code
            if (payinfo.getTrack1(true) == null) {
                sb.append("&ccnumber="); //test=4111111111111111 (visa)
                sb.append(URLEncoder.encode(payinfo.getCardNumber(), "UTF-8"));

                sb.append("&ccexp="); //expiration date  (MM/YY)
                sb.append(payinfo.getExpirationDate());

                String[] cc_name = payinfo.getHolderName().split(" ");
                sb.append("&firstname=");
                if (cc_name.length > 0) {
                    sb.append(URLEncoder.encode(cc_name[0], "UTF-8"));
                }
                sb.append("&lastname=");
                if (cc_name.length > 1) {
                    sb.append(URLEncoder.encode(cc_name[1], "UTF-8"));
                }
            } else {
                //String track_1 = "%B4111111111111111^PADILLA VISDOMINE/LUIS ^0509120000000000000000999000000?";
                //String track_2 = ";4111111111111111=05091200333300000000?";
                //String track_3 = ";4111111111111111=7247241000000000000303009046040400005090=111111234564568798543654==1=0000000000000000?";
                sb.append("&track_1=").append(URLEncoder.encode(payinfo.getTrack1(true), "UTF-8"));
                sb.append("&track_2=").append(URLEncoder.encode(payinfo.getTrack2(true), "UTF-8"));
            }

            if (payinfo.getTotal() > 0.0) { //SALE
                sb.append("&type=");
                sb.append("Sale");
                sb.append("&transactionid="); //transaction ID
                sb.append(payinfo.getTransactionID());
            } else { // REFUND
                sb.append("&type=");
                sb.append("Refund");
                sb.append("&transactionid="); //transaction ID
                sb.append(payinfo.getTransactionID());
                //payinfo.paymentError(AppLocal.getIntString("message.paymentrefundsnotsupported"));
            }
            // open secure connection
            url = new URL("http://d.eze.cc/api/2.0/p2p/start");
            connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            // not necessarily required but fixes a bug with some servers
            connection.setRequestProperty("Content-Type", "application/json");
            // POST the data in the string buffer
            out = new DataOutputStream(connection.getOutputStream());
            out.write(sb.toString().getBytes());
            out.flush();
            out.close();
            // process and read the gateway response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! response " + response);
            //RESPONSE
            //response=1&responsetext=SUCCESS&authcode=123456&transactionid=849066017&avsresponse=&cvvresponse=M&orderid=&type=sale&response_code=100
            payinfo.setReturnMessage(response);
            in.close(); // fin
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response);
            if (response == null) {
                payinfo.paymentError(AppLocal.getIntString("message.paymenterror"), "Response empty.");
            } else {
                if (obj.get("success").toString().equals("true")) {
                    int timer = 0;
                    int timeout = 120;
                    Thread.sleep(10000);
                    timer += 1;
                    if (timer > timeout);
                    int result = JOptionPane.showConfirmDialog(null, "Press F2 and Please Swap the Card", "Press F2 and Please Swap the Card",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        sb.append("{\"username\" : \"" + m_userName + "\", \"password\":\"" + m_userPassword + "\",\"externalRefNumber\":\"" + payinfo.getTransactionID() + "\"}");
                        url = new URL("http://d.eze.cc/api/2.0/p2p/status");
                        connection = url.openConnection();
                        connection.setDoOutput(true);
                        connection.setUseCaches(false);
                        connection.setAllowUserInteraction(false);

                        // not necessarily required but fixes a bug with some servers
                        connection.setRequestProperty("Content-Type", "application/json");
                        // POST the data in the string buffer
                        out = new DataOutputStream(connection.getOutputStream());
                        out.write(sb.toString().getBytes());
                        out.flush();
                        out.close();
                        BufferedReader in1 = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String response1 = in1.readLine();
                        JSONParser parser1 = new JSONParser();
                        JSONObject obj1 = (JSONObject) parser.parse(response1);
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! second response " + response1);
                        if (obj1.get("success").toString().equals("true")) {
                            payinfo.paymentOK("OK", payinfo.getTransactionID(), response);
                        } else {
                            payinfo.paymentError(AppLocal.getIntString("message.paymenterror"), obj.get("message").toString());
                        }
                    } else {
                        System.out.println("Cancelled");
                    }

                } else {
                    payinfo.paymentError(AppLocal.getIntString("message.paymenterror"), obj.get("message").toString());
                }

            }
        } catch (UnsupportedEncodingException | MalformedURLException eUE) {
            //no pasa nunca
            payinfo.paymentError(AppLocal.getIntString("message.paymentexceptionservice"), eUE.getMessage());
        } // no pasa nunca
        catch (IOException e) {
            payinfo.paymentError(AppLocal.getIntString("message.paymenterror"), e.getMessage());
        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(PaymentGatewayEzeeTab.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
