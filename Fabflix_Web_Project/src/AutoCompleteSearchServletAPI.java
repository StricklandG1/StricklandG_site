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
import java.sql.PreparedStatement;

@WebServlet(name="/AutoCompleteSearchServletAPI", urlPatterns="/api/auto_complete_search")
public class AutoCompleteSearchServletAPI extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	 
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String searchInput = "+" + request.getParameter("search_value");
		if (searchInput.contains(" "))
		{
			if (searchInput.charAt(searchInput.length() - 1) == ' ')
				searchInput = searchInput.substring(0, searchInput.length() - 1) + "*";
			else
				searchInput = searchInput + "*";
			searchInput = searchInput.replaceAll(" ", "*+");
		}
		else
			searchInput = searchInput + "*";
		
		System.out.println(searchInput);
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		try
		{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ AutoCompleteSearchServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            
            if (ds == null)
                out.println("ds is null @ AutoCompleteSearchServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ AutoCompleteSearchServlet.");
			
			String query = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10;";
			
    		PreparedStatement statement = dbcon.prepareStatement(query);

    		statement.setString(1, searchInput);
        	
        	ResultSet rs = statement.executeQuery();
        	
        	JsonArray jsonArray = new JsonArray();
        	
        	while (rs.next())
        	{
        		String movieId = rs.getString("id");
        		String movieTitle = rs.getString("title");
        		
        		jsonArray.add(generateJsonObject(movieId, movieTitle));
        	}
        	
        	out.write(jsonArray.toString());

			response.setStatus(200);
			out.close();
			dbcon.close();
			rs.close();
			statement.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
		}
	}
	
	private static JsonObject generateJsonObject(String movieId, String movieTitle)
	{
		JsonObject movie = new JsonObject();
		movie.addProperty("value", movieTitle);
		
		JsonObject id = new JsonObject();
		id.addProperty("id", movieId);
		
		movie.add("data", id);
		return movie;
		
	}
}
