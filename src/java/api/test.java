/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.util.LinkedHashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/test")
public class test {
    @Context
    ServletContext sc;
    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;
    @Context
    HttpServletResponse response;

    public test() {
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
}
