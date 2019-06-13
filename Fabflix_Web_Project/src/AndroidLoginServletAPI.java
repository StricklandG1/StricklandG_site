import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.*;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "AndroidLoginServletAPI", urlPatterns = "/api/android_login")
public class AndroidLoginServletAPI extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
    	response.setContentType("application/json");
    	
    	try
    	{
    		Connection dbcon = dataSource.getConnection();
    		
    		String email = request.getParameter("email_input");
	    	String password = request.getParameter("password_input");
	    	
	    	System.out.println("Android email: " + email);
	    	System.out.println("Android password: " + password);
	    	
        	String query = "SELECT password\n" 
        			+ "FROM customers\n"
        			+ "WHERE email = ?\n";
        	
        	PreparedStatement stmt = dbcon.prepareStatement(query);
        	stmt.setString(1, email);
        	
        	ResultSet rs = stmt.executeQuery();
        	
        	if (rs.next()) 
        	{
        		boolean success = new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"));
        		
        		if (success) // Correct password after decryption
        		{
        			request.getSession().setAttribute("user", new User(email, "customer"));
	                request.getSession().setAttribute("cart", new Cart());
	                
	                JsonObject responseJsonObject = new JsonObject();
	                responseJsonObject.addProperty("status", "success");
	                responseJsonObject.addProperty("type", "customer");
	        		responseJsonObject.addProperty("message", "success");
	
	                response.getWriter().write(responseJsonObject.toString());        		
	            }
        		else
        		{
        			JsonObject responseJsonObject = new JsonObject();
        			responseJsonObject.addProperty("status", "fail");
        			responseJsonObject.addProperty("message", "Email or password is incorrect.");
        			
        			response.getWriter().write(responseJsonObject.toString());
        		}
        	}
        	
        	rs.close();
        	stmt.close();
        	dbcon.close();
    	}
    	catch (Exception e)
    	{
			JsonObject responseJsonObject = new JsonObject();
			responseJsonObject.addProperty("status", "fail");
			responseJsonObject.addProperty("message", e.toString());
			response.getWriter().write(responseJsonObject.toString());
    	}
    }
}
