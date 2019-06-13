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

@WebServlet(name = "Top20ServletAPI", urlPatterns = "/api/top_20")
public class Top20ServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	// New DataSource object to connect to the one registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("application/json");
		String userAgent = request.getHeader("User-Agent");
		System.out.println("Top20\n" + userAgent);
		boolean isAndroid = userAgent.contains("Android");
		
		PrintWriter out = response.getWriter();
		
		try
		{
			HttpSession session = request.getSession();
			session.removeAttribute("search");
			session.removeAttribute("id");
			session.removeAttribute("sort");
			session.removeAttribute("order");
			session.removeAttribute("page");
			session.removeAttribute("results");
			// Get a connection from dataSource

        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ Top20Servlet");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            
            if (ds == null)
                out.println("ds is null @ Top20Servlet.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ Top20Servlet.");

            // Declare our statement
            Statement statement = dbcon.createStatement();

			String query = "SELECT movies.id, title, year, director,\n"
					+ "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name SEPARATOR',') as 'genreList',\n"
					+ "GROUP_CONCAT(DISTINCT genres.id ORDER BY genres.name SEPARATOR',') as 'genreIdList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name SEPARATOR',') as 'starList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name SEPARATOR',') as 'starIdList', rating\n"
					+ "FROM movies, ratings, genres, genres_in_movies, stars, stars_in_movies\n"
					+ "WHERE movies.id = ratings.movieId AND movies.id = genres_in_movies.movieId AND genres.id = genreId AND movies.id = stars_in_movies.movieId AND starId = stars.id\n"
					+ "GROUP BY movies.id\n"
					+ "ORDER BY rating DESC\n"
					+ "LIMIT 20;\n";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();
            if (!isAndroid)
            {
	            while (rs.next()) 
	            {
					String movieId = rs.getString("movies.id");
					String tempMovieTitle = rs.getString("title");
					String movieTitle = makeMovieLink(movieId, tempMovieTitle);
					String movieYear = rs.getString("year");
					String movieDirector = rs.getString("director");
					String[] genreList = rs.getString("genreList").split(",");
					String[] genreIdList = rs.getString("genreIdList").split(",");
					String movieGenres = makeGenreLink(genreIdList, genreList);
					String[] starList = rs.getString("starList").split(",");
					String[] starIds = rs.getString("starIdList").split(",");
					String movieStars = combineStrings(starList, starIds);
					String movieRating = rs.getString("rating");
	
	                // Create a JsonObject based on the data we retrieve from rs
	                JsonObject jsonObject = new JsonObject();
	                jsonObject.addProperty("movie_id", movieId);
	                jsonObject.addProperty("movie_title", movieTitle);
	                jsonObject.addProperty("movie_year", movieYear);
	                jsonObject.addProperty("movie_director", movieDirector);
	                jsonObject.addProperty("movie_genres", movieGenres);
	                jsonObject.addProperty("movie_stars", movieStars);
	                jsonObject.addProperty("movie_rating", movieRating);
	
	                jsonArray.add(jsonObject);
	            }
            }
            else
            {
            	while(rs.next())
            	{
            		String movieId = rs.getString("movies.id");
					String tempMovieTitle = rs.getString("title");
					//String movieTitle = makeMovieLink(movieId, tempMovieTitle);
					String movieYear = rs.getString("year");
					String movieDirector = rs.getString("director");
					String genreList = rs.getString("genreList");
					//String[] genreIdList = rs.getString("genreIdList").split(",");
					//String movieGenres = makeGenreLink(genreIdList, genreList);
					String starList = rs.getString("starList");
					//String[] starIds = rs.getString("starIdList").split(",");
					//String movieStars = combineStrings(starList, starIds);
					//String movieRating = rs.getString("rating");
	
	                // Create a JsonObject based on the data we retrieve from rs
	                JsonObject jsonObject = new JsonObject();
	                jsonObject.addProperty("movie_id", movieId);
	                jsonObject.addProperty("movie_title", tempMovieTitle);
	                jsonObject.addProperty("movie_year", movieYear);
	                jsonObject.addProperty("movie_director", movieDirector);
	                jsonObject.addProperty("movie_genres", genreList);
	                jsonObject.addProperty("movie_stars", starList);
	               // jsonObject.addProperty("movie_rating", movieRating);
	
	                jsonArray.add(jsonObject);
            	}
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
	static String makeMovieLink(String movieId, String movieTitle)
	{
		String result = "<a href=single_movie.html?id=" + movieId + ">" + movieTitle + "</a>";
		return result;
	}
	
	static String combineStrings(String[] starNames, String[] starIds)
	{
		String result = "";
		int len = starNames.length;
		for(int i = 0; i < len; i++)
		{
			result += "<a href=single_star.html?id=" + starIds[i] + ">" + starNames[i] + "</a><br>";
		}
		return result;
	}
	
	static String makeGenreLink(String genreId[], String genreName[])
	{
		int len = genreId.length;
		String result = "";
		for(int i = 0; i < len; ++i)
		{
		// http://localhost:8080/CS_122B_Fablix_Project_API_Version/search.html?search=title&id=D&sort=title&order=asc&page=0&results=10
			result += "<a href=search.html?"
					+ "search=genre"
					+ "&id=" + genreId[i]
					+ "&sort=title"
					+ "&order=asc"
					+ "&page=0"
					+ "&results=10"
					+ ">" + genreName[i]
					+ "</a><br>";
		}
		return result;
	}
}
