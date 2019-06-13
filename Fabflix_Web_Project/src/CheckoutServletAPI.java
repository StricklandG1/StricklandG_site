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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;


@WebServlet(name="/CheckoutServletAPI", urlPatterns="/api/checkout")
public class CheckoutServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	 
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	// Handle data being posted from browser
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("user");
        String username = user.getUsername();
        
		String ccId = request.getParameter("cc_id");
    	String ccFname = request.getParameter("cc_fname");
    	String ccLname = request.getParameter("cc_lname");
    	String ccExp = request.getParameter("cc_exp");
    	
    	System.out.println(ccId);
    	System.out.println(ccFname);
    	System.out.println(ccLname);
    	System.out.println(ccExp);
    	
    	response.setContentType("application/json");
    	
    	try
    	{
    		PrintWriter out = response.getWriter();
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ CheckoutServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ CheckoutServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ CheckoutServlet.");

    		String query = "SELECT customers.id\n" + 
    				"FROM creditcards, customers\n" + 
    				"WHERE customers.email = ?\n" +							// 1
    				"AND creditcards.id = ?\n" + 							// 2
    				"AND customers.ccId = creditcards.id\n" + 
    				"AND creditcards.firstName = ?\n" + 					// 3
    				"AND creditcards.lastName = ?\n" + 						// 4
    				"AND creditcards.firstName = customers.firstName\n" + 
    				"AND creditcards.lastName = customers.lastName\n" + 
    				"AND expiration = ?;";									// 5
        	
    		PreparedStatement statement = dbcon.prepareStatement(query);

    		statement.setString(1, username);
    		statement.setString(2, ccId);
    		statement.setString(3, ccFname);
    		statement.setString(4, ccLname);
    		statement.setString(5, ccExp);
        	
        	ResultSet rs = statement.executeQuery();

        	if (rs.next())
    		{
        		// Grab the items from the cart to access the movie ID's to insert into sales transaction
                Cart myCart = (Cart) session.getAttribute("cart");
                ArrayList<Item> tempCart = myCart.getCart();
                
                // Retrieve customer ID from query results, and generate current date in sales date column format
                String customerId = rs.getString("customers.id");
                Date today = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(today);

                System.out.println("Recording sales items into the database.");
                
                // Loop through every item in the cart, and enter each movie sold as a sales entry for that customer
                for (Item item : tempCart)
                {
                	System.out.println(customerId + " " + item.getMovieId() + " " + item.getMovieTitle() + " " + date + " x" + item.getQuantity());
                	
                    String salesQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity)"
                    				  + "VALUES (?, ?, ?, ?)";
            
	                PreparedStatement salesStatement = dbcon.prepareStatement(salesQuery);
	                
	                salesStatement.setInt(1, Integer.parseInt(customerId));
	                salesStatement.setString(2, item.getMovieId());
	                salesStatement.setDate(3, java.sql.Date.valueOf(date));
	                salesStatement.setInt(4, item.getQuantity());
	                
	                salesStatement.executeUpdate();
	                
	                // Don't forget to close the statement to complete the query transaction!
	                salesStatement.close();
                }
                
                System.out.println("Recorded sales items into the database.");

                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                response.getWriter().write(responseJsonObject.toString());
    		}
    		else 
            {
                // If credit card validation fails, update the status to fail
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Credit card information was invalid. Please try again.");
                
                response.getWriter().write(responseJsonObject.toString());
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
	
	// Handle data to be retrieved by browser
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		HttpSession session = request.getSession();
		User username = (User) session.getAttribute("user");
    	Cart myCart = (Cart) session.getAttribute("cart");
        String movieId = request.getParameter("movie_id");
        
        if (!myCart.equals(null)) 
        {
	        System.out.println("movieID: " + movieId);
	        System.out.println("username: " + username.getUsername());
	        System.out.println("myCart size: " + myCart.getCartSize());
	        myCart.printCart();
	        
	        JsonArray jsonArray = new JsonArray();
	        
	        for (Item item : myCart.getCart())
	        {
	        	JsonObject jsonObject = new JsonObject();
	        	jsonObject.addProperty("movie_id", item.getMovieId());
	        	jsonObject.addProperty("movie_title", item.getMovieTitle());
	        	jsonObject.addProperty("movie_quantity", item.getQuantity());
	        	jsonObject.addProperty("movie_cost", item.getCost());
	        	
	        	jsonArray.add(jsonObject);
	        }
	        
	        out.write(jsonArray.toString());
	        response.setStatus(200);
        }
	}
}
