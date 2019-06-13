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


@WebServlet(name = "SingleGenreServletAPI", urlPatterns = "/api/single_genre")
public class SingleGenreServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 2L;
	
	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		response.setContentType("application/json");
	
		// Retrieve parameter id from url request.
		String genreId = request.getParameter("id");
		String genreName = request.getParameter("genre");

		PrintWriter out = response.getWriter();
		
		try
		{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ SingleGenreServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ SingleGenreServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ SingleGenreServlet.");
			
			// Create the SQL query
			String query = "SELECT genres.id, genres.name, movies.id, movies.title \n" + 
					"FROM genres, genres_in_movies, movies \n" + 
					"WHERE genres.id = ?\n" +
					"AND genres.id = genres_in_movies.genreId \n" + 
					"AND movies.id = genres_in_movies.movieId;";
				// 1 genreId
			
			PreparedStatement statement = dbcon.prepareStatement(query);
			
			statement.setString(1, genreId);
			
			ResultSet rs = statement.executeQuery();
			
			JsonArray jsonArray = new JsonArray();
			
			while (rs.next()) 
			{
				String tempGenreId = rs.getString("genres.id");
				String tempGenreName = rs.getString("genres.name");
				String movieId = rs.getString("movies.id");
				String movieTitle = rs.getString("movies.title");
				
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("genre_id", tempGenreId);
				jsonObject.addProperty("genre_name", tempGenreName);
				jsonObject.addProperty("movie_id", movieId);
				jsonObject.addProperty("movie_title", movieTitle);
				
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
}
