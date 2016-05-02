/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.DirStateFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author msis
 */
@Path("/download")
public class dl_execute {
    @Context
    ServletContext sc;
    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;
    @Context
    HttpServletResponse response;

    public dl_execute() {
    }
    
    @GET
    @Path("/{mail}/{itemname}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(
            @PathParam("mail") String mail,
            @PathParam("itemname") String itemname
    ) throws SQLException {
        String recordset=recoedset(mail, itemname);
        
        if(recordset.equals("setup")){
            //itemnameの末尾の_で分解しbxxxxのビルド番号を省いた名称をファイル名としている。
            String DLName=itemname.substring(0, itemname.lastIndexOf("_"))+".zip";
                 
            File file=new File("/home/annyys/binary/ANNYYS_D/"+itemname+".zip");//パスURL
            //File file=new File("D:\\Downloads\\/BGsssInfo.zip");//パスURL
            //http://www.annyys.net/archive/ANNYYS_D/ANNYYS_Dv14_b160415.zip
            if(file.exists()){
                ResponseBuilder response = Response.ok((Object)file); 
                String headerVal = "attachment; filename="+DLName;
                response.header("Content-Disposition",headerVal);
                return response.build();
            }else{
                String html = "<html><body><br><br>" +
                "【エラー：ご迷惑をおかけして申し訳ございません】<br><br>" +
                "ダウンロード対象のアーカイブが見当たりません。<br><hr size=1><br>" +
                "お手数ですが、下記リンクのお問い合わフォームよりご連絡ください。<br><br>" +
                "<a href=\"http://www1.annyys.net/?page_id=25\" target=\"_brank\">ANNYYS_D事務局お問い合わせフォーム</a><br><br><br>" +
                "ANNYYS_D事務局(<a href=\"mailto:official@annyys.net\">official@annyys.net</a>)<br>" +
                "</body></html>";
                ResponseBuilder response = Response.ok().entity(html).header("Content-Type","text/html; charset=UTF-8");
                return response.build();
            }
        }else{
            String html = "<html><body><br><br>" +
                "ダウンロード対象URLの有効期間が過ぎています。（期限はリクエストより２４時間です。）<br><hr size=1><br>" +
                "お手数をおかけいたしますが、もう一度フォームよりダウンロードリクエストを送信してください。<br><br>" +
                "<a href=\"http://www1.annyys.net/?page_id=47\">ダウンロードリクエスト</a><br><br><br>" +
                "ANNYYS_D事務局(<a href=\"mailto:official@annyys.net\">official@annyys.net</a>)<br>" +
                "</body></html>";
            ResponseBuilder response = Response.ok().entity(html).header("Content-Type","text/html; charset=UTF-8");
            return response.build();
        }
    }
    
    @GET
    @Path("/download2/{mail}/{itemname}")
    @Produces(MediaType.TEXT_PLAIN)
    public String recoedset(
            @PathParam("mail") String mail,
            @PathParam("itemname") String itemname
    ) throws SQLException {
        String rtnStr="";
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        Date nowdate = cal.getTime();
        
        InitialContext ctx = null;
        DataSource ds = null;
        Connection con = null;
        
        try {
            ctx = new InitialContext();
            ds = (DataSource)ctx.lookup("java:comp/env/jdbc/DBM");
            con = ds.getConnection();
            Statement stmt = con.createStatement();
                        
            String SQL="SELECT * FROM download_log";
            SQL += " WHERE mail=\'"+mail+"\'";
            SQL += " AND itemname=\'"+itemname+"\'";
            SQL += " ORDER BY _key DESC";
            
            ResultSet rs=stmt.executeQuery(SQL);
            boolean record=rs.next();
            if(record){
                int key = rs.getInt("_key");
                //dowlloadにタイムスタンプの登録
                Calendar addtime = Calendar.getInstance();
                Calendar endtime = Calendar.getInstance();
                try{
                    String[] date=rs.getString("date").split("-",-1);
                    String[] time=rs.getString("time").split(":",-1);
                    /*使わない
                    addtime.set(
                            Integer.parseInt(date[0]), 
                            Integer.parseInt(date[1])-1, 
                            Integer.parseInt(date[2]), 
                            Integer.parseInt(time[0]), 
                            Integer.parseInt(time[1]),
                            Integer.parseInt(time[2])
                            );
                    */
                    endtime.set(
                            Integer.parseInt(date[0]), 
                            Integer.parseInt(date[1])-1, 
                            Integer.parseInt(date[2]), 
                            Integer.parseInt(time[0]), 
                            Integer.parseInt(time[1]+24),//期限切れの時刻
                            Integer.parseInt(time[2])
                            );
                    int diff = endtime.compareTo(cal);
                    //endtimeが現在時刻より先の場合＝期限内の場合
                    if(diff==1){
                        //レコードdounloadtime　のアップデート
                        String UPDATE="UPDATE download_log SET download=\'"+formatter1.format(cal.getTime())+"\'";
                        UPDATE += " WHERE _key="+key;
                        boolean SET=stmt.execute(UPDATE);
                        rtnStr="setup";
                    }else{
                        rtnStr="timeout";
                    }
                    /*int year = addtime.get(Calendar.YEAR);
                    int month = addtime.get(Calendar.MONTH);
                    int day = addtime.get(Calendar.DATE);
                    rtnStr=Integer.toString(year)+"-"+Integer.toString(month)+"-"+Integer.toString(day);
                    rtnStr=formatter1.format(addtime.getTime())+"/"+formatter1.format(endtime.getTime())+"/"+formatter1.format(cal.getTime());
                    rtnStr += "/ diff="+Integer.toString(diff);
                    */
                }catch(Exception e){
                    rtnStr = e.toString();
                }
                
                //return addtime.toString();
            }else{
                rtnStr= "NG";
            }
            rs.close();
            stmt.close();
            
        } catch (NamingException e) {
            e.printStackTrace();
            
        } finally{
            if(con != null){
                con.close();
            }
        }
        return rtnStr;
    }
    
    @GET
    @Path("test/{JNDI}")
    //@Produces(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_XML)
    //@Produces(MediaType.APPLICATION_JSON)
    public String  database(@PathParam("JNDI") String jndi ) throws SQLException {
        return jndi;
    }
    
}
