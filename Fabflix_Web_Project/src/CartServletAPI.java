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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "CartServletAPI", urlPatterns = "/api/cart")
public class CartServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	// doGet to handle cart item quantity updates
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
        String id = request.getParameter("movie_id");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        System.out.println(id);
        System.out.println(quantity);

    }
    
    // doPost to handle all the Add To Cart or Update Quantity buttons clicked from client-side
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
    	response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
    	
		HttpSession session = request.getSession();
    	
		User username = (User) session.getAttribute("user");
    	Cart myCart = (Cart) session.getAttribute("cart");
    	if (myCart == null)
    	{
    		myCart = new Cart();
    	}
        //String movieId = request.getParameter("movie_id");
        
        // Should be "add" or "update" depending on which button posted to the Cart Servlet
        String status = request.getParameter("status");
        
        String movieId = "";
        if (status.equals("add"))
        {
        	movieId = request.getParameter("movie_id");
        }
        else if (status.equals("update"))
        {
        	movieId = request.getParameter("update_movie_id");
        }
        System.out.println("movieID: " + movieId);
        System.out.println("username: " + username.getUsername());
        System.out.println("myCart size: " + myCart.getCartSize());
        
        System.out.println("***** STATUS: " + status + "********");

        try
        {
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ CartServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            
            if (ds == null)
                out.println("ds is null @ CartServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ CartServlet.");
		
        	String query = "SELECT movies.id, movies.title FROM movies WHERE movies.id = ?";
    	
        	PreparedStatement statement = dbcon.prepareStatement(query);

        	statement.setString(1, movieId);
        	
        	ResultSet rs = statement.executeQuery();

        	if (rs.next()) 
        	{
        		if (status.equals("add"))
        		{
	        		Item newItem = new Item(movieId, rs.getString("movies.title"));
	        		if (myCart.alreadyContains(newItem))
	        		{
	        			System.out.println("New item exists in the cart...");
	        			myCart.updateItem(newItem);
	        		}
	        		else
	        		{
	        			synchronized(myCart)
	        			{
	        				myCart.addItem(newItem);
	        			}
	        		}
        		}
        		
        		else if (status.equals("update"))
        		{
        			int updateQuantity = Integer.parseInt(request.getParameter("update_quantity"));
        			String updateMovieId = request.getParameter("update_movie_id");
        			
        			synchronized(myCart)
        			{
        				myCart.updateQuantity(updateMovieId, updateQuantity);
        			}
        		}
        	}
        	// Put cart back into session storage
        	session.setAttribute("cart", myCart);
        	
        	// cart after adding
        	System.out.println(myCart.getCartSize());
        	System.out.println("TOTAL: " + myCart.getTotal());
        	System.out.println("===================");

        	myCart.printCart();
        	
        	rs.close();
            statement.close();
            dbcon.close();
            
            response.setStatus(200);
        }
        catch (Exception e)
        {
			// write error message JSON object to output
			e.printStackTrace();
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
        }
    }
}
