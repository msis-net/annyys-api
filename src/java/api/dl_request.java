/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/request")
public class dl_request {
    @Context
    ServletContext sc;
    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;
    @Context
    HttpServletResponse response;

    public dl_request() {
    }
    /**
    @QueryParam("p1") String p1,@QueryParam("p2") String p2
    デフォルト値を設定する場合
    @DefaultValue("world") @QueryParam("q") String name
     */
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String test1() {
       return getClass().getSimpleName();//==test 
    }
    
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String test2(String data) {
        
        return data;
    }
    
    @GET
    @Path("/table")
    @Produces(MediaType.TEXT_PLAIN)
    public String table(
            @QueryParam("p1") String p1,
            @QueryParam("p2") String p2
    ) throws SQLException {
        
        InitialContext ctx = null;
        DataSource ds = null;
        Connection con = null;
        String rtnXML="apiDatabase";
        try {
            ctx = new InitialContext();
            ds = (DataSource)ctx.lookup("java:comp/env/jdbc/DBM");
            con = ds.getConnection();
            //rtnXML=con.toString();
            
            Statement stmt = con.createStatement();
            DatabaseMetaData dmd = con.getMetaData();
           
            String types[] = { "TABLE" };
            ResultSet rs1 = dmd.getTables(null, null,"%", types);
            while(rs1.next()){
                
                String tblname=rs1.getString("TABLE_NAME");
                rtnXML +=tblname+"\n";
                
            }
            rs1.close();
            stmt.close();
            
        } catch (NamingException e) {
            e.printStackTrace();
        } finally{
            if(con != null){
                con.close();
            }
        }
        
        return rtnXML;
    }
    
    
    @POST
    @Path("/download")
    @Produces(MediaType.TEXT_PLAIN)
    public String create(
            @FormParam("pref") String pref,
            @FormParam("facilities") String facilities,
            @FormParam("name") String name,
            @FormParam("mail") String mail,
            @FormParam("itemname") String itemname,
            @FormParam("commnet") String commnet
    ) throws SQLException {
        String DLName=itemname.substring(0, itemname.lastIndexOf("_"));
        //*データベース登録*//
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Date nowdate = cal.getTime();
        
        InitialContext ctx = null;
        DataSource ds = null;
        Connection con = null;
        String rtnXML="NG:";
        try {
            ctx = new InitialContext();
            ds = (DataSource)ctx.lookup("java:comp/env/jdbc/DBM");
            con = ds.getConnection();
            Statement stmt = con.createStatement();
            
            String SQL1="INSERT INTO download_log SET(pref,facilities,name,itemname,commnet)";
            SQL1 += " VALUES("+pref+","+facilities+","+name+","+itemname+","+commnet+")";
            
            String SQL="INSERT INTO download_log (date,time,name,mail,pref,facilities,itemname,comment)";
            SQL +=" VALUES(\'"+formatter1.format(nowdate)+"\',\'"+formatter2.format(nowdate)+"\',\'"+name+"\',\'"+mail+"\',\'"+pref+"\',\'"+facilities+"\',\'"+itemname+"\',\'"+commnet+"\')";
            //rtnXML=con.toString();
            try{
                stmt.execute(SQL);
                stmt.close();
                rtnXML=SendMail(pref, facilities, name, mail,itemname);
                if(rtnXML.indexOf("OK") != -1){
                    rtnXML +="["+ mail+"]へメールを送信しました。\n受信メールを確認してURLリンクから「"+DLName+"」をダウンロードしてください。";
                }
            }catch(Exception e){
                rtnXML="NG:"+e.toString();
            }finally{
                if(stmt != null)
                    stmt.close();
            }
            
        } catch (NamingException e) {
            e.printStackTrace();
            rtnXML="NG:"+e.toString();
        } finally{
            if(con != null){
                con.close();
            }
        }
        
        return rtnXML;
    }
    
    public String SendMail(
            String pref,
            String facilities,
            String name,
            String mail,
            String itemname
    
    ) throws MessagingException, UnsupportedEncodingException{
        String rtnStr="";
        String SendTo = mail;
        String SendFrom = "official@annyys.net";
        String RESULT="NG:";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.host", "smtp.gmail.com");
        // SMTPポートを設定
        prop.put("mail.smtp.port", "587");
        // 認証を有効に設定
        prop.put("mail.smtp.auth", "true" );
        // STARTTLSを有効に設定
        prop.put("mail.smtp.starttls.enable", "true");
        // 認証用クラスを第2引数に指定して、セッションを生成
        prop.put("mail.smtp.debug", "true");
        
        Session MailSession = Session.getInstance(prop, new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication("admin@annyys.net","tso62725f");
                }
            });
        
        
        String Subject = "ダウンロードURLのご案内（ANNYYS_D事務局）";
        String BodyMsg = name+" 様\r\n";
        
        String Param="download/"+mail+"/"+itemname;
        
        String DLName=itemname.substring(0, itemname.lastIndexOf("_"));
        
        BodyMsg += "「"+DLName+"」ダウンロードのお申込みありがとうございます。\r\n";
        BodyMsg += "\r\n";
        BodyMsg += "\r\n";
        BodyMsg += "「"+DLName+"」は以下のURLからダウンロード可能です。（記載されたURLは24時間有効です。それ以後は、再度ダウンロードリクエストの送信が必要です。）\r\n";
        //BodyMsg += "\r\n【FileMakerV11対応】\r\n";ダウンロードダウンロード
        //BodyMsg += "http://www.annyys.net/JSP/Downloads/"+RandomName+".jsp\r\n";
        BodyMsg += "\r\n【FileMakerはV12以後対応】※可能な限り最新版をＤＬしてください。\r\n";
        BodyMsg += "http://www1.annyys.net:18080/annyys_api/api/"+Param+"\r\n";
        BodyMsg += "\r\n注意:このURLにはフォームに登録されたメールアドレス情報が含まれていますので転用・転載はお控えいただきますようお願い申し上げます。\r\n";
        BodyMsg += "\r\n";
        BodyMsg += "\r\n";
        BodyMsg += "メーリングリストに登録すると情報交換が行えます。\r\n";
        BodyMsg += "メーリングリストは誰でも登録・参加することができます。登録はこちらから\r\n";
        BodyMsg += "http://dev.annyys.net/home/content_community\r\n";
        BodyMsg += "なを、本メールは自動配信の為返信はできませんのであらかじめご了承ください。\r\n";
        BodyMsg += "\r\n";
        BodyMsg += "====================================\r\n";
        BodyMsg += "ANNYYS_D事務局\r\n";
        BodyMsg += "事務局管理：Msis,Inc.(株式会社エムシス)\r\n";
        BodyMsg += "〒158-0083\r\n";
        BodyMsg += "東京都世田谷区奥沢6-27-2-5F\r\n";
        BodyMsg += "official@msis-net.com\r\n";
        BodyMsg += "====================================\r\n";

        MimeMessage msgA = new MimeMessage(MailSession);
        msgA.setRecipients(Message.RecipientType.TO, SendTo);
        msgA.setFrom(new InternetAddress(SendFrom, SendFrom, "ISO-2022-JP"));
        msgA.setSubject(Subject, "ISO-2022-JP");
        msgA.setText(BodyMsg, "ISO-2022-JP");
        try{
            Transport.send(msgA);
            rtnStr ="OK:";
        }catch(MessagingException mex){
            rtnStr="NG:"+mex.toString();
            /*for (StackTraceElement ste : mex.getStackTrace()){
            rtnStr +=ste.toString()+"\n";   
           }
           */
        }
        
        Subject = DLName+"ダウンロードリクエストがありました。";
        BodyMsg = "Name: "+name+"\r\n";
        BodyMsg += "Pref: "+pref+"\r\n";
        BodyMsg += "Facilities: "+facilities+"\r\n";
        BodyMsg += "Email: "+mail+"\r\n";
        BodyMsg += "itemname: "+itemname+"\r\n";
        //BodyMsg += "MLIST:"+ml+"\r\n";
        MimeMessage msgB = new MimeMessage(MailSession);
        msgB.setRecipients(Message.RecipientType.TO, "official@annyys.net");
        msgB.setFrom(new InternetAddress(SendFrom, SendFrom, "ISO-2022-JP"));
        msgB.setSubject(Subject, "ISO-2022-JP");
        msgB.setText(BodyMsg, "ISO-2022-JP");
        
        try{
            Transport.send(msgB);
            rtnStr ="OK:";
        }catch(MessagingException mex){
            rtnStr="NG:"+mex.toString();
        }
        
        return rtnStr;
    }
    
    public String CreateDLFile(String itemname){
        String URL="";
        
        return URL;
    }
}