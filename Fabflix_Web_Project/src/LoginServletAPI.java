import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
//import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.PreparedStatement;

import java.io.IOException;
import java.io.PrintWriter;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	 /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
    	String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

    	response.setContentType("application/json");

        boolean passedReCaptcha = false;
        String userAgent = request.getHeader("User-Agent");
        boolean isAndroid = userAgent.contains("Android");
        System.out.println("Login\n" + userAgent);
        
        // Verify reCAPTCHA
        if (userAgent != null && !isAndroid)
        {
	        try 
	        {
	            RecaptchaVerifyUtilities.verify(gRecaptchaResponse);
	            passedReCaptcha = true;
	        } 
	        catch (Exception e) 
	        {
				System.out.println("reCAPTCHA is not validated.");
				
	            // If reCAPTCHA fails, update the login status to fail
	            JsonObject recaptchaFailJsonObject = new JsonObject();
	            
	            recaptchaFailJsonObject.addProperty("status", "fail");
	            recaptchaFailJsonObject.addProperty("message", "Please check the reCAPTCHA box and try again.");
	            
	            response.getWriter().write(recaptchaFailJsonObject.toString());
	            
	        }
        }
        
        // Only validate login in if the reCAPTCHA is completed
        if (passedReCaptcha || isAndroid)
        {
	    	// These values are grabbed from the login form fields. 
	    	String username = request.getParameter("username");
	    	String password = request.getParameter("password");
	    		    	
	    	try
	    	{
	    		PrintWriter out = response.getWriter();
	        	Context initCtx = new InitialContext();

	            Context envCtx = (Context) initCtx.lookup("java:comp/env");
	            if (envCtx == null)
	                out.println("envCtx is NULL @ LoginServlet");

	            // Look up our data source
	            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
	            
	            if (ds == null)
	                out.println("ds is null @ LoginServlet.");
	            
	        	Connection dbcon = ds.getConnection();
	        	if (dbcon == null)
	                out.println("dbcon is null @ LoginServlet.");
	    		
	        	String query = "SELECT password\n" 
	        			+ "FROM customers\n"
	        			+ "WHERE email= ?\n";
	    		
	    		//Statement statement = dbcon.createStatement();
	    		PreparedStatement statement = dbcon.prepareStatement(query);
	        	statement.setString(1, username);
	        	
	        	ResultSet rs = statement.executeQuery();
	        	
	    		if (rs.next()) // Correct email - check for password
	            {
	    			
	    			boolean success = new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"));
	    			if (success) // correct password after decryption
	    			{
		                request.getSession().setAttribute("user", new User(username, "customer"));
		                request.getSession().setAttribute("cart", new Cart());
		                
		                JsonObject responseJsonObject = new JsonObject();
		                responseJsonObject.addProperty("status", "success");
		                responseJsonObject.addProperty("type", "customer");
		        		responseJsonObject.addProperty("message", "Login Successful!");
		
		                response.getWriter().write(responseJsonObject.toString());
	    			}
	    			else
	    			{
	    				JsonObject responseJsonObject = new JsonObject();
	    				responseJsonObject.addProperty("status", "fail");
	    				responseJsonObject.addProperty("message", "Username or password is invalid.");
		                response.getWriter().write(responseJsonObject.toString());
	    			}
	            } 
	            else 
	            {
	                // Check for potential employee login
	            	query = "SELECT password\n"
	            			+ "FROM employees\n"
	            			+ "WHERE email=?\n";
	            	statement = dbcon.prepareStatement(query);
	            	statement.setString(1, username);
	            	
	            	rs = statement.executeQuery();
	            	
	            	if (rs.next())
	            	{
	            		boolean success = new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"));
	            		if (success)
	            		{
			                request.getSession().setAttribute("user", new User(username, "employee"));
			                request.getSession().setAttribute("cart", new Cart());
			                
			                JsonObject responseJsonObject = new JsonObject();
			                responseJsonObject.addProperty("status", "success");
			                responseJsonObject.addProperty("type", "employee");
			        		responseJsonObject.addProperty("message", "success");
			
			                response.getWriter().write(responseJsonObject.toString());
	            		}
	            	}
	            	else
	            	{
		                JsonObject responseJsonObject = new JsonObject();
		                responseJsonObject.addProperty("status", "fail");
		
		                responseJsonObject.addProperty("message", "Username or password is incorrect. Please try again.");
		                
		                response.getWriter().write(responseJsonObject.toString());
	            	}
	            }
	    
	                	
	        	response.setStatus(200);
	        	
	        	rs.close();
	            statement.close();
	            dbcon.close();
	            
	    	}
	    	catch (Exception e)
	    	{
				// write error message JSON object to output
				e.printStackTrace();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("errorMessage", e.getMessage());
	
				// set response status to 500 (Internal Server Error)
				response.setStatus(500);
	    	}
        }
    }
}
