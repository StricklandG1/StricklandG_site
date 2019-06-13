import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "CompleteServletAPI", urlPatterns = "/api/complete")
public class CompleteServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
    	response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
    	
		// Access the current session and retrieve the shopping cart
		HttpSession session = request.getSession();
		User username = (User) session.getAttribute("user");
    	Cart myCart = (Cart) session.getAttribute("cart");
    	ArrayList<Item> tempCart = myCart.getCart();
    	
    	JsonArray jsonArray = new JsonArray();
    	
    	/**
    	 * Memo from Jason: Don't forget to include the confirmation number as a jsonObject into the
    	 * jsonArray at the end before it writes out.
    	 * 
    	 * I included a basic set up to use the PreparedStatement below, so it's a matter of filling out
    	 * the details in between after the query is executed. If you need help with the query params, 
    	 * either look at my example in SearchServletAPI or ask me
    	 * */
    	// Grab the confirmation number of the most recent transaction by the customer to print onto confirmation page
    	try
    	{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ CompleteServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ CompleteServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ CompleteServlet.");
    		
    		String query = "SELECT id FROM customers WHERE email = ?";
    		PreparedStatement stmt = dbcon.prepareStatement(query);
    		stmt.setString(1, username.getUsername());
    		ResultSet rs = stmt.executeQuery();
    		int userId = 0;
    		if(rs.next())
    		{
    			userId = Integer.parseInt(rs.getString("id"));
    		}
    		
    		query = "SELECT MAX(id) FROM sales\n"
    				+ "WHERE movieId = (SELECT id FROM movies WHERE title = ?)\n"
    				+ "AND customerId = ?";
    		stmt = dbcon.prepareStatement(query);
    		stmt.setInt(2, userId);
	    	// Retrieve cart details to show at the complete screen.
	    	for (Item item : myCart.getCart())
	    	{
	    		stmt.setString(1, item.getMovieTitle());
	    		rs = stmt.executeQuery();
	    		rs.next();
	    		JsonObject jsonObject = new JsonObject();
	    		jsonObject.addProperty("movie_title", item.getMovieTitle());
	    		jsonObject.addProperty("movie_quantity", item.getQuantity());
	    		jsonObject.addProperty("movie_cost", item.getCost());
	    		jsonObject.addProperty("sales_id", Integer.parseInt(rs.getString("MAX(id)")));
	    		jsonArray.add(jsonObject);
	    	}
	
	    	// Send out JSON data to client-side
	    	rs.close();
	    	stmt.close();
	    	dbcon.close();
	    	out.write(jsonArray.toString());
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
    	try
    	{
	    	// Purge the cart and put it back into session as empty cart.
	    	myCart.deleteCart();
	    	session.setAttribute("cart", myCart);
    	}
    	catch (Exception e)
    	{
    		System.out.println(e);
    	}
	}
}
