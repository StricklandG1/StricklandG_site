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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "SingleStarServletAPI", urlPatterns = "/api/single_star")
public class SingleStarServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 2L;
	
	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try 
		{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ SingleStarServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ SingleStarServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ SingleStarServlet.");

			// Construct a query with parameter represented by "?"
			String query = "SELECT name, birthYear, movies.id, title, year, director\n"
					+ "FROM stars, movies, stars_in_movies\n"
					+ "WHERE stars.id = ? AND starId = stars.id AND movies.id = movieId";
				// 1 id
			// Declare our statement
			//PreparedStatement statement = dbcon.prepareStatement(query);
			PreparedStatement statement = dbcon.prepareStatement(query);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);

			// Perform the query
			//ResultSet rs = statement.executeQuery();
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) 
			{
				String starName = rs.getString("name");
				String starBirthYear = rs.getString("birthYear");
				String movieId = rs.getString("movies.id");
				String movieTitle = rs.getString("title");
				String movieYear = rs.getString("year");
				String movieDirector = rs.getString("director");

				// Create a JsonObject based on the data we retrieve from rs
				HttpSession session = request.getSession();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_birth_year", starBirthYear);
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("search", (String)session.getAttribute("search"));
                jsonObject.addProperty("id", (String)session.getAttribute("id"));
                jsonObject.addProperty("sort", (String)session.getAttribute("sort"));
                jsonObject.addProperty("order", (String)session.getAttribute("order"));
                jsonObject.addProperty("page", (String)session.getAttribute("page"));
                jsonObject.addProperty("results", (String)session.getAttribute("results"));

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
			e.printStackTrace();
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		
		out.close();
	}
	
	static String combineStrings(String[] starNames, String[] starIds)
	{
		String result = "";
		int len = starNames.length;
		for(int i = 0; i < len; i++)
		{
			//result += "<br><a href=/api/single-star.html?id=" + starIds[i] + ">" + starNames[i] + "</a>";
			result += "<a href=single_star.html?id=" + starIds[i] + ">" + starNames[i] + "</a><br>";
		}
		return result;
	}
}
