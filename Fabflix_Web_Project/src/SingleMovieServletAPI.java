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
import java.sql.PreparedStatement;


@WebServlet(name = "SingleMovieServletAPI", urlPatterns = "/api/single_movie")
public class SingleMovieServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 2L;
	
	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		response.setContentType("application/json"); // Response mime type
		String userAgent = request.getHeader("User-Agent");
		boolean isAndroid = userAgent.contains("Android");
		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try 
		{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ SingleMovieServlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ SingleMovieServlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ SingleMovieServlet.");

			// Construct a query with parameter represented by "?"
			String query = "SELECT movies.id, title, year, director,\n"
					+ "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name SEPARATOR',') as 'genreList',\n"
					+ "Group_CONCAT(DISTINCT genres.id ORDER BY genres.name SEPARATOR',') as 'genreIdList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name SEPARATOR',') as 'starList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name SEPARATOR',') as 'starIdList'\n"
					+ "FROM movies, genres, genres_in_movies, stars, stars_in_movies\n"
					+ "WHERE movies.id = ?\n"
					+ "AND movies.id = genres_in_movies.movieId "
					+ "AND genres.id = genreId "
					+ "AND movies.id = stars_in_movies.movieId "
					+ "AND starId = stars.id";
				// 1 id
			
			// Declare our statement
			PreparedStatement statement = dbcon.prepareStatement(query);
			//Statement statement = dbcon.createStatement();

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);

			// Perform the query
			//ResultSet rs = statement.executeQuery();
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();
			
			query = "SELECT rating FROM ratings WHERE ratings.movieId=?";
			statement = dbcon.prepareStatement(query);

			// Iterate through each row of rs
			while (rs.next()) 
			{
				String movieId = rs.getString("movies.id");
				String movieTitle = rs.getString("title");
				String movieYear = rs.getString("year");
				String movieDirector = rs.getString("director");
				String[] genreList = rs.getString("genreList").split(",");
				String[] genreIdList = rs.getString("genreIdList").split(",");
				String movieGenres = Top20ServletAPI.makeGenreLink(genreIdList, genreList);
				String[] starList = rs.getString("starList").split(",");
				String[] starIds = rs.getString("starIdList").split(",");
				String movieStars = combineStrings(starList, starIds);
				statement.setString(1, movieId);
				ResultSet ratingSet = statement.executeQuery();
				String movieRating = "";
				if (ratingSet.next())
				{
					movieRating = ratingSet.getString("rating");
				}
				else movieRating = "N/A";
				//String movieRating = rs.getString("rating");

				// Create a JsonObject based on the data we retrieve from rs
				HttpSession session = request.getSession();
				
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonObject.addProperty("movie_stars", movieStars);
                if (movieRating != null)
                	jsonObject.addProperty("movie_rating", movieRating);
                else
                	jsonObject.addProperty("movie_rating", "N/A");
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
