package com.openbravo.pos.sales.restaurant.dao;

import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.sales.Notifications;
import com.openbravo.pos.sales.TrayNotification;
import com.openbravo.pos.util.AltEncrypter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;
import com.openbravo.pos.sales.mobileorders.MobileAppOrders;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author premsarojanand
 */
@Component
@EnableScheduling
public class PosSynchronizationPoll {
//    @Autowired
//    private final ConnectionValidator connectionValidator;

    private final String db_url, db_user;
    private String db_password;
    private Connection localConnection = null;
    ResultSet localrs = null;
    public MobileAppOrders mobileAppOrders;

    private Boolean SFLAG;
    private String SQL;
    private String host = "apis.ethosmiracle.com";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private String ordercheck, SITEGUID, tablename, tablepk, process, tablepk_name, table_sc_name, table_sc_id;
    private int count;
  //  String url = "http://apis.ethosmiracle.com/api/dataSync/create?autoReconnect=true&useSSL=false";
     String url = "http://13.235.167.89/apis/miracleFamily.php";

    public PosSynchronizationPoll() {
        AppConfig appConfig = new AppConfig(new String[]{});
        appConfig.load();
        this.db_url = appConfig.getProperty("db.URL");
        this.db_user = appConfig.getProperty("db.user");
        this.db_password = appConfig.getProperty("db.password");
        this.mobileAppOrders = mobileAppOrders;

    }

    @Scheduled(fixedDelay = 600000)
    public void PollSyncdata() throws Exception {
        if (db_user != null && db_password != null && db_password.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + db_user);
            db_password = cypher.decrypt(db_password.substring(6));
        }
        try {
            localConnection = DriverManager.getConnection(db_url, db_user, db_password);
        } catch (SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }
        SITEGUID = null;
        SFLAG = false;
        PreparedStatement s = localConnection.prepareStatement("Select * from siteguid");
        ResultSet rs_local = s.executeQuery();
        while (rs_local.next()) {
            SITEGUID = rs_local.getString("siteguid");
            SFLAG = rs_local.getBoolean("sflag");
            ordercheck = rs_local.getString("ordercheck");
        }
        try {
            if (!SITEGUID.equals('0')) {
                if (SFLAG) {
                    String cmd;
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        cmd = "ping -n 1 " + host;
                    } else {
                        cmd = "ping -c 1 " + host;
                    }
                    Process myProcess = Runtime.getRuntime().exec(cmd);
                    myProcess.waitFor();
                    if (myProcess.exitValue() == 0) {
                       //serverProcess();
                        System.out.println(" Internet is True ");
                    }
                }
            }
            localConnection.close();
        } catch (SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void serverProcess() {
        String order = "http://apis.ethosmiracle.com/api/orders/getposorder?autoReconnect=true&useSSL=false";
    //    String order = "http://elegenze.in/apis/miracleFamily.php?autoReconnect=true&useSSL=false";
    
//Order Checking process
    //checkOrders(order, ordercheck);
        try {
            String sql = null;
            tablename = null;
            tablepk = null;
            process = null;
            tablepk_name = null;
            table_sc_id = null;
            table_sc_name = null;
            localrs = null;
            sql = "select * from databasechangelog";
            PreparedStatement database = localConnection.prepareStatement(sql);
            localrs = database.executeQuery();
            
            while (localrs.next()) {
                tablename = localrs.getString("TABLENAME");
                tablepk = localrs.getString("TABLE_PK_ID");
                process = localrs.getString("PROCESS");
                tablepk_name = localrs.getString("tablepk_name");
                table_sc_id = localrs.getString("table_scnd_id");
                table_sc_name = localrs.getString("table_scnd_name");
                count = localrs.getInt("count");
                System.out.println("local rss " + localrs + " serverProcess " + tablename + process + tablepk + tablepk_name + table_sc_id + table_sc_name);
             //   System.out.println("local rss " + localrs + " serverProcess " + process);
                switch (process) {
                    case "I":
                        serverInsert(tablename, process, tablepk, tablepk_name, table_sc_id, table_sc_name);
                        break;
                    case "U":
                        serverUpdate(tablename, process, tablepk, tablepk_name, table_sc_id, table_sc_name);
                        break;
                    case "D":
                        serverDelete();
                        break;
                }

            }

            localrs.close();
            database.closeOnCompletion();

        } catch (SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void serverInsert(String tablename, String process, String tablepk, String tablepk_name, String table_sc_id, String table_sc_name) {
        ResultSetMetaData rsmd;
        String si = null;
        try {
            String upqu = "update " + tablename + " set SFLAG = 1 where " + tablepk_name + " = " + "'" + tablepk + "' and " + table_sc_name + " = " + "'" + table_sc_id + "'";
            PreparedStatement updateQuery = localConnection.prepareStatement(upqu);
            updateQuery.executeUpdate();
            switch (tablename) {
                case "categories":
                    si = "select id,name,parentid,imgurl,description,texttip,catshowname,catorder,siteguid,sflag,lflag from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
                    break;
                case "products":
                    si = "select id,reference,code,codetype,name,pricebuy,pricesell,category,taxcat,attributeset_id,stockcost,stockvolume,iscom,isscale,isconstant,printkb,sendstatus,isservice,attributes,isvprice,isverpatrib,texttip,warranty,stockunits,printto,supplier,uom,memodate,imgurl,description,siteguid,sflag,lflag from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
                    break;
//                case "ticketlines":
//                    si = "select id,reference,code,codetype,name,pricebuy,pricesell,category,taxcat,attributeset_id,stockcost,stockvolume,iscom,isscale,isconstant,printkb,sendstatus,isservice,attributes,display,isvprice,isverpatrib,texttip,warranty,stockunits,printto,supplier,uom,memodate,siteguid,sflag,lflag from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
//                    break;
                default:
                    si = "select * from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
            }
            PreparedStatement sv = localConnection.prepareStatement(si);
            try (ResultSet rs_local = sv.executeQuery()) {
                while (rs_local.next()) {
                    localConnection.setAutoCommit(false);
                    rsmd = rs_local.getMetaData();
                    final int columnCount = rsmd.getColumnCount();
                    List<List<String>> rowList = new LinkedList<>();
                    List<String> columnNames = null;
                    String insertColumns = "";
                    String insertValues = "";
                    columnNames = new ArrayList<>();
                    List<String> columnList = new LinkedList<>();
                    rowList.add(columnList);
                    for (int j = 1; j <= columnCount; j++) {
                        columnNames.add(rsmd.getColumnLabel(j));
                    }
                    if (columnNames != null && columnNames.size() > 0) {
                        insertColumns += columnNames.get(0);
                        insertValues += "?";
                    }
                    for (int j = 1; j < columnNames.size(); j++) {
                        insertColumns += ", " + columnNames.get(j);
                        insertValues += ", " + "?";
                    }
                    SQL = "INSERT INTO " + tablename + " (" + insertColumns + ") values(" + insertValues + ")";
                    PreparedStatement ps = localConnection.prepareStatement(SQL);
                    for (int column = 1; column <= columnCount; column++) {
                        Object value = rs_local.getObject(column);
                        ps.setObject(column, value);
                    }
                    String psa = ps.toString();
                    String query = psa.substring(psa.indexOf(": ") + 2);
                    System.out.println(" psa " + query);
                    Sapi(url, query);

                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void serverUpdate(String tablename, String process, String tablepk, String tablepk_name, String table_sc_id, String table_sc_name) {
        ResultSetMetaData rsmd;
        String sv1 = null;
        try {
            switch (tablename) {
                case "categories":
                    sv1 = "select id,name,parentid,imgurl,description,texttip,catshowname,catorder,siteguid,sflag,lflag from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
                    break;
                case "products":
                    sv1 = "select id,reference,code,codetype,name,pricebuy,pricesell,category,taxcat,attributeset_id,stockcost,stockvolume,iscom,isscale,isconstant,printkb,sendstatus,isservice,attributes,display,isvprice,isverpatrib,texttip,warranty,stockunits,printto,supplier,uom,memodate,imgurl,description,siteguid,sflag,lflag from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
                    break;
                default:
                    sv1 = "select * from " + tablename + " where " + tablepk_name + " = '" + tablepk + "'" + " and " + table_sc_name + " =  '" + table_sc_id + "'";
            }
            PreparedStatement sv = localConnection.prepareStatement(sv1);
            try (ResultSet rs_update = sv.executeQuery()) {
                while (rs_update.next()) {
                    localConnection.setAutoCommit(false);
                    rsmd = rs_update.getMetaData();
                    final int columnCount = rsmd.getColumnCount();
                    List<List<String>> rowList = new LinkedList<>();
                    List<String> columnNames = null;
                    String insertColumns = "";
                    String insertValues = "";
                    columnNames = new ArrayList<>();
                    List<String> columnList = new LinkedList<>();
                    rowList.add(columnList);
                    for (int j = 1; j <= columnCount; j++) {
                        columnNames.add(rsmd.getColumnLabel(j));
                    }
                    if (columnNames != null && columnNames.size() > 0) {
                        insertColumns += columnNames.get(0);
                        insertValues += "?";
                    }
                    for (int j = 1; j < columnNames.size(); j++) {
                        insertColumns += " = ?, " + columnNames.get(j);
                        insertValues += ", " + "?";
                    }
                    switch (tablename) {
                        case "stockcurrent":
                            SQL = "UPDATE " + tablename + " SET " + insertColumns + " = ? where " + tablepk_name + " = '" + tablepk + "'" + table_sc_name + " = '" + table_sc_id + "'";
                        default:
                            SQL = "UPDATE " + tablename + " SET " + insertColumns + " = ? where " + tablepk_name + " = '" + tablepk + "'" + " and siteguid = '" + SITEGUID + "'";
                    }

                    PreparedStatement ps = localConnection.prepareStatement(SQL);
                    for (int column = 1; column <= columnCount; column++) {
                        Object value = rs_update.getObject(column);
//                                columnList.add(String.valueOf(value));
                        ps.setObject(column, value);
                    }
                    String psa = ps.toString();
                    String query = psa.substring(psa.indexOf(": ") + 2);
                    System.out.println(" Update  " + query);

                    Sapi(url, query);
                }
            }
            sv.closeOnCompletion();
        } catch (SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void serverDelete() {
        switch (tablename) {
            case "products_cat":
                String serverdelete = "delete from products_cat where product = '" + tablepk + "' and siteguid = '" + SITEGUID + "'";
                Sapi(url, serverdelete);
                break;
        }
    }

    public void Sapi(String url, String dataString) {
        HashMap<String, String> content = new HashMap<>();
        try {
//            System.out.println(" SapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapiSapi " + process);
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjU3YjJiZTQwYjU0ZjZiYmFlZjc5ZTE4Y2E5YTFmMDJkMzNjODJkZjEzZjNmNGRkZWQ5ZmVlYTgyM2FmZmUxMmIzNzkxZDIxOTNjZjdkYjY4In0.eyJhdWQiOiIxIiwianRpIjoiNTdiMmJlNDBiNTRmNmJiYWVmNzllMThjYTlhMWYwMmQzM2M4MmRmMTNmM2Y0ZGRlZDlmZWVhODIzYWZmZTEyYjM3OTFkMjE5M2NmN2RiNjgiLCJpYXQiOjE1NjA5NjMzNTAsIm5iZiI6MTU2MDk2MzM1MCwiZXhwIjoxNTkyNTg1NzUwLCJzdWIiOiIxIiwic2NvcGVzIjpbIioiXX0.aVPXyzY2ZQ8wONps6BAMxnKgII1CWMfET4NEcwfPU7Z37MnvM-aq1U-o1nvAffG9AE9jgg05h1SUnq5sRga3Vyab8B0p2DEk5blzTSUjnzuK-sWsIKM5EsqtHsAPkBBT5vyBAMXl64-ecxSfUEu03XB6qgny8gzsJI29P1-oDEOt8ztw6xy_uHHSDX1gkNiHOSVTbx8nKw3_CpHACZLdHe30K23CJjMgc8mvP0G7lnypXk9Hjh7Z4SU2qUwIllpcuJzoN2tIj3P-Tb-T6BaLiqFbn3EDBXaTybPfcyjgYp8jgOjn2y4DbczNFjoCVNlGkRsDK3V4ycrepaOfn3_48oxHW8e4KoeMqYUvhz2lNKHHRdwXlYRnZE2Xi1ih9Q_T1efAJH6Ycu0AICY5ccBU3K9OHi5K9CeyE79QD_T_9Ojxq3L4v28u4c0o2Ipy8BixTdU4Wc9UnW4c30JqA-Wp9y1I9N739Ru5u9-GRmA0R6GTOnuawH_8emK_A3gR2b4qTGLKueExRn7mmGx75ORM3AhIe-7fv85gaHer0fj1JBwrjnMVT2kIc3Cg_0TGlxbI5HxuQ6GJ-7OjQCSwlRt3-TQRhwnP0khZ7kyouSuC1AOa-as6PU3YJG732xhwfKdQZDDRVcd5A66WKlpn-Nqg-1PhHs_Apmvikvil9MpqxCY";
            URL url1 = new URL(url);
            // 2. Open connection
            HttpURLConnection conn;
            conn = (HttpURLConnection) url1.openConnection();
            // 3. Specify POST method
            conn.setRequestMethod("POST");
            // 4. Set the headers
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            // 5. Add JSON data into POST request body
            //`5.1 Use Jackson object mapper to convert Contnet object into JSON
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("data", dataString);
            json.put("tablename", tablename);
            json.put("process", process);
            json.put("pk_id", tablepk);
            json.put("tablepk_name", tablepk_name);
            json.put("table_sc_name", table_sc_name);
            json.put("table_sc_id", table_sc_id);
            json.put("siteguid", SITEGUID);
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
            
            System.out.println("POST data *********************" + postData.toString());
            
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                sb.append((char) c);
            }
            
            System.out.println("*****************************************" + sb.toString());
            
            
            JSONObject obj = new JSONObject(sb.toString());
//            System.out.println("object " + obj.get("status"));
            if (obj.get("status").toString().equals("1")) {
//                System.out.println("Executing Delete");
                String s = "delete from databasechangelog where tablename = " + '"' + obj.get("tablename").toString() + '"' + " and table_pk_id = " + "'" + obj.get("pk_id").toString() + "'" + " and table_scnd_id = " + "'" + obj.get("table_sc_id").toString() + "'";
                PreparedStatement sv = localConnection.prepareStatement(s);
                int execute = sv.executeUpdate();

                localConnection.commit();
//                System.out.println("Commited delete" + execute);
            } else if (count < 3) {
                String s = "update databasechangelog set count = " + (count + 1) + " where tablename = " + '"' + obj.get("tablename").toString() + '"' + " and table_pk_id = " + "'" + obj.get("pk_id").toString() + "'" + " and table_scnd_id = " + "'" + obj.get("table_sc_id").toString() + "'";
                PreparedStatement sv = localConnection.prepareStatement(s);
                int execute = sv.executeUpdate();
                localConnection.commit();
            } else {
                String val1 = obj.get("query").toString();
                String val2 = obj.get("tablename").toString();
                String inserttrash = "insert into trash (query,tablename) values ( " + '"' + obj.get("query") + '"' + " , " + "'" + val2 + "')";
                PreparedStatement sv1 = localConnection.prepareStatement(inserttrash);
                sv1.execute();

                String s = "delete from databasechangelog where tablename = " + '"' + obj.get("tablename").toString() + '"' + " and table_pk_id = " + "'" + obj.get("pk_id").toString() + "'" + " and table_scnd_id = " + "'" + obj.get("table_sc_id").toString() + "'";
                PreparedStatement sv = localConnection.prepareStatement(s);
                int execute = sv.executeUpdate();

                localConnection.commit();
            }

            conn.disconnect();

        } catch (MalformedURLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void checkOrders(String url2, String ordercheck) {

        HashMap<String, String> content = new HashMap<>();
        try {
            localConnection.setAutoCommit(true);
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImY4NTYxMmFkOTVmYTYxZDk4YTI2OTg5OTNiMzE1MTZkMDUxM2JhZGE0NDU2ZTIxOTVhYjgyNDVhYzk5N2Q1MmE4NDgxMDhmYjQxNGM1YzcyIn0.eyJhdWQiOiIxIiwianRpIjoiZjg1NjEyYWQ5NWZhNjFkOThhMjY5ODk5M2IzMTUxNmQwNTEzYmFkYTQ0NTZlMjE5NWFiODI0NWFjOTk3ZDUyYTg0ODEwOGZiNDE0YzVjNzIiLCJpYXQiOjE1NDY3MDY3MjIsIm5iZiI6MTU0NjcwNjcyMiwiZXhwIjoxNTc4MjQyNzIxLCJzdWIiOiIxIiwic2NvcGVzIjpbIioiXX0.GrLHc7PEodeFOrKOqc1ZVa8lfXpB9hVwrXiVcqXwk20RwGZEh3ZjDRwENCkr0asWI6ss-22iWRCX8xBBGjV0f5Kuw6pbI1u2fCi1bFge0OJVoGH9E4fmUSo1B8FOm86_7xyCu4VkFvajTeWySDNUFJIKxlMc0Bev1nloqqhutzdAMgCFFwrTZNMyrq17khcSHe4aoqTz5jjPFmLSC9jbKiRorlORkJKRFLMWoeg9yQwRQfH7f_Bzp_G1d93uBHMdA1PYnFbCTaSutynzMgSHukpbENb0Yx8hU5KOqhfzaHhPIkzXbLEf1Mnt9jWwGMco5v7QFBwUJz3f1isqdVJ4xx9_9b2Q80c-LWHMamQy_wRQ9o8DkfIqilcQJVB7GOJCavcVHu57CMuWfLKsEEAQ5NIJP_PjwdRIx9iTVgWS63JKIKv5vCE1w9ljm-1bQHg10NFfDHutQs7WL0Ud2Ox1Ls-0XBjWJz-umK7eeRwWHul9urQpdMYEBQ4d5PUn4SIjw89Tyu7XjePPmyrubz2ZAOj80q-sS446ictN90NLyZGvy2bo_lzrcXOMflcZfs9wO-xVzHIFPCUsq64CJBQ7jUEYfoduCpYu8KwrCH61Xi1JxKJUhF0zX0gJJxR_mQ_q2e92DxZ30bxApibb4WlkVfVzRYgAWM4i6mqhuTEdS6M";
            URL url1 = new URL(url2);
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
            json.put("siteguid", SITEGUID);
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
                if (!obj.get("orderstatus").toString().equals(ordercheck)) {

                    insertOrders(obj);
                }
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void showNotification(String message, Notifications notification) {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            JFXPanel jfxPanel = new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {

        }

        Platform.runLater(() -> {
            TrayNotification trayNotification = new TrayNotification("Ethos Miracle", message, notification);
            trayNotification.showAndWait();
        });

    }

    public void insertOrders(JSONObject data) {
        try {
            localConnection.setAutoCommit(true);
            JSONObject orderMasters = new JSONObject(data.get("orders").toString());
            JSONArray ordermasterdatas = orderMasters.getJSONArray("order");
            for (int i = 0; i < ordermasterdatas.length(); i++) {
                JSONObject orderMasterdata = ordermasterdatas.getJSONObject(i);
                String orders = "insert into app_orders (`order_no`,"
                        + "`order_date`,`customer_name`,`customer_phone`,"
                        + "`address`,`total_value`,`total_tax`,`total_items`,"
                        + "`total_discount`,`gross_total`,`order_type`,`promotion_id`,`credits_used`,"
                        + "`credits_earned`,`order_status`,`payment_method`,`transactionid`,`payment_status`,"
                        + "`payment_party`,`coupon_discount`,`table` ,`siteguid`,`sflag`,`lflag`) "
                        + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement orderstatement = localConnection.prepareStatement(orders);
                orderstatement.setInt(1, orderMasterdata.optInt("orderno"));
                orderstatement.setString(2, orderMasterdata.optString("orderdate"));//1
                orderstatement.setString(3, orderMasterdata.optString("name"));
                orderstatement.setInt(4, orderMasterdata.optInt("phone", 0));//2
                orderstatement.setString(5, orderMasterdata.optString("address"));
                orderstatement.setDouble(6, orderMasterdata.optDouble("total_value"));
                orderstatement.setDouble(7, orderMasterdata.optDouble("total_tax"));
                orderstatement.setInt(8, orderMasterdata.optInt("total_items"));
                orderstatement.setDouble(9, orderMasterdata.optDouble("total_discount", 0.0));
                orderstatement.setDouble(10, orderMasterdata.optDouble("gross_total"));//4
                orderstatement.setString(11, orderMasterdata.optString("order_type"));
                orderstatement.setString(12, orderMasterdata.optString("promotion_id", null));//5
                orderstatement.setDouble(13, orderMasterdata.optDouble("credits_used", 0.0));
                orderstatement.setDouble(14, orderMasterdata.optDouble("credits_earned"));//6
                orderstatement.setString(15, orderMasterdata.optString("order_status"));
                orderstatement.setString(16, orderMasterdata.optString("payment_method"));//7
                orderstatement.setString(17, orderMasterdata.optString("transactionid", null));
                orderstatement.setString(18, orderMasterdata.optString("payment_status"));//8
                orderstatement.setString(19, orderMasterdata.optString("payment_party", null));
                orderstatement.setString(20, orderMasterdata.optString("coupon_discount"));//9
                orderstatement.setString(21, orderMasterdata.optString("table"));//10
                orderstatement.setString(22, SITEGUID);
                orderstatement.setInt(23, 1);//12
                orderstatement.setInt(24, 1);//13
//                System.out.println(" !!!!!!!!!!!!!!!!!! orders1 !!!!!!!!!!!!!!!!! " + orderstatement);
                boolean execute = orderstatement.execute();
//                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + execute);

            }
            JSONArray orderdetaildatas = orderMasters.getJSONArray("orderdetails");
            for (int i = 0; i < orderdetaildatas.length(); i++) {
                JSONObject orderDetaildata = orderdetaildatas.getJSONObject(i);
                String orderDetails = "insert into app_order_details (`order_id`,`product`,`price`,`pricewithtax`,`tax`,`quantity`,"
                        + "`attributename`) values (?,?,?,?,?,?,?)";
                PreparedStatement orderDetailStatement = localConnection.prepareStatement(orderDetails);
                orderDetailStatement.setInt(1, orderDetaildata.optInt("order_id"));
                orderDetailStatement.setString(2, orderDetaildata.optString("menu_item_id"));
                orderDetailStatement.setDouble(3, orderDetaildata.optDouble("menu_item_rate"));
                orderDetailStatement.setDouble(4, orderDetaildata.optDouble("menu_item_ratetax"));
                orderDetailStatement.setDouble(5, orderDetaildata.optDouble("menu_item_tax"));
                orderDetailStatement.setInt(6, orderDetaildata.optInt("menu_item_quantity"));
                orderDetailStatement.setString(7, orderDetaildata.optString("menu_item_attributename"));
                boolean execute = orderDetailStatement.execute();
//                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + orderDetailStatement);
            }
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Now coomit"  );
            String s = "update siteguid set ordercheck = '" + data.get("orderstatus").toString() + "' ";
            PreparedStatement sv = localConnection.prepareStatement(s);
            int execute = sv.executeUpdate();
            if (execute == 1) {
                showNotification("New Mobile Order Reciveied", Notifications.INFORMATION);

            }
        } catch (SQLException ex) {
            Logger.getLogger(PosSynchronizationPoll.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
