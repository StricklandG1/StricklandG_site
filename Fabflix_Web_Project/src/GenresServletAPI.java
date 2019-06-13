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


@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
	{
		response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		try
		{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ GenresServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ GenresServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ GenresServlet.");

			String query = "SELECT id, name FROM genres;";
			
            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
					
            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            
            while (rs.next()) 
            {
            	String genreId = rs.getString("id");
            	String genreName = makeGenreLink(genreId, rs.getString("name"));

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre_name", genreName);

                jsonArray.add(jsonObject);
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
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
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		
		out.close();
	}
	
	/**
	 * Create the relative URL to include the id and genre name as URL parameters for
	 * when a user clicks a specific genre to search movies by. 
	 * Each parameter is separated by the '&' character.
	 * */
	static String makeGenreLink(String genreId, String genreName)
	{
		// http://localhost:8080/CS_122B_Fablix_Project_API_Version/search.html?search=title&id=D&sort=title&order=asc&page=0&results=10
		String result = "<a href=search.html?"
				+ "search=genre"
				+ "&id=" + genreId
				+ "&sort=title"
				+ "&order=asc"
				+ "&page=0"
				+ "&results=10"
				+ ">" + genreName
				+ "</a>";
		return result;
	}
}