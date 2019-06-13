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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;


/**
 * Servlet implementation class SearchServletAPI
 */
@WebServlet(name="/SearchServletAPI", urlPatterns="/api/search")
public class SearchServletAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	@Resource(name = "jdbc/moviedb")
	private DataSource datasource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long servletStartTime = System.nanoTime();
		long servletTotalTime  = 0;
		long jdbcStartTime;
		long jdbcCurrentTime = 0;
		response.setContentType("application/json");
		
		String userAgent = request.getHeader("User-Agent");
		System.out.println("search\n" + userAgent);
		boolean isAndroid = userAgent.contains("Android");
		
		PrintWriter out = response.getWriter();
		try
		{
			jdbcStartTime = System.nanoTime();
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
            if (envCtx == null)
                out.println("envCtx is NULL @ SearchServlet");

            // Look up our data source
            jdbcStartTime = System.nanoTime();
            DataSource ds = (DataSource) envCtx.lookup("jdbc/PooledDB");
            jdbcCurrentTime = System.nanoTime() - jdbcStartTime;
            
            if (ds == null)
                out.println("ds is null @ SearchServlet.");
            jdbcStartTime = System.nanoTime();
        	Connection dbcon = ds.getConnection();
        	jdbcCurrentTime = System.nanoTime() - jdbcStartTime;
        	if (dbcon == null)
                out.println("dbcon is null @ SearchServlet.");
        	
			String search = request.getParameter("search");
			String id = request.getParameter("id");
			String sort = request.getParameter("sort");
			String order = request.getParameter("order");
			String page = request.getParameter("page");
			String results = request.getParameter("results");
			String query = "";
			String fieldsQuery = "";
			String fieldsQueryTitle = "";
			String fieldsQueryYear = "";
			String fieldsQueryDirector = "";
			String fieldsQueryStar = "";
			String test = "";
			
			// Fields for parameters specific to type of search to be populated
			// and then added in as PreparedStatements before the query is run
			
			HttpSession session = request.getSession();
			session.setAttribute("search", search);
			session.setAttribute("id", id);
			session.setAttribute("sort", sort);
			session.setAttribute("order", order);
			session.setAttribute("page", page);
			session.setAttribute("results", results);
			System.out.println("Search="+ (String)session.getAttribute("search"));
					
			if (search.equals("genre"))
			{
				// Query for genre
				query = "SELECT movies.id, movies.title, movies.year, movies.director,\n"
					+ "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name SEPARATOR',') as 'genreList',\n"
					+ "GROUP_CONCAT(DISTINCT genres.id ORDER BY genres.name SEPARATOR',') as 'genreIdList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name SEPARATOR',') as 'starList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name SEPARATOR',') as 'starIdList', rating\n"
					+ "FROM movies, ratings, genres, genres_in_movies, stars, stars_in_movies\n"
					+ "WHERE movies.id = ratings.movieId AND movies.id = genres_in_movies.movieId AND genres.id = genreId AND movies.id = stars_in_movies.movieId AND starId = stars.id\n"
					+ "GROUP BY movies.id\n"
					+ "HAVING FIND_IN_SET(?, genreIdList)\n";
				if (sort.equalsIgnoreCase("title"))
					query += "ORDER BY title ";
				else
					query += "ORDER BY rating ";
				if (order.equalsIgnoreCase("ASC"))
					query += "ASC\n";
				else
					query += "DESC\n";
				query += "LIMIT ? OFFSET ?;\n";

				// 1 id, 2 sort, 3 order, 4 results, 5 (Integer.parseInt(page) * Integer.parseInt(results))
			}
			else if (search.equals("title"))
			{
				// Query by title
				query = "SELECT movies.id, movies.title, movies.year, movies.director,\n"
					+ "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name SEPARATOR',') as 'genreList',\n"
					+ "GROUP_CONCAT(DISTINCT genres.id ORDER BY genres.name SEPARATOR',') as 'genreIdList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name SEPARATOR',') as 'starList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name SEPARATOR',') as 'starIdList', rating\n"
					+ "FROM movies, ratings, genres, genres_in_movies, stars, stars_in_movies\n"
					+ "WHERE movies.id = ratings.movieId AND movies.id = genres_in_movies.movieId AND genres.id = genreId AND movies.id = stars_in_movies.movieId AND starId = stars.id AND title LIKE ? \n"
					+ "GROUP BY movies.id\n";
				if (sort.equalsIgnoreCase("title"))
					 query += "ORDER BY title ";
				else
					query += "ORDER BY rating ";
				if (order.equalsIgnoreCase("ASC"))
					query += "ASC\n";
				else
					query += "DESC\n";
				query += "LIMIT ? OFFSET ?;\n";

				// 1 id, 2 sort, 3 order, 4 results, 5 (Integer.parseInt(page) * Integer.parseInt(results))
			}
			else if (search.equals("fts"))
			{
				id = "+" + id;
				if (id.contains(" "))
				{
					if (id.charAt(id.length() - 1) == ' ')
						id = id.substring(0, id.length() - 1) + "*";
					else
						id = id + "*";
					id = id.replaceAll(" ", "*+");
				}
				else
					id = id + "*";
				query = "SELECT movies.id, movies.title, movies.year, movies.director,\n"
						+ "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name SEPARATOR',') as 'genreList',\n"
						+ "GROUP_CONCAT(DISTINCT genres.id ORDER BY genres.name SEPARATOR',') as 'genreIdList',\n"
						+ "GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name SEPARATOR',') as 'starList',\n"
						+ "GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name SEPARATOR',') as 'starIdList'\n"
						+ "FROM movies, ratings, genres, genres_in_movies, stars, stars_in_movies\n"
						+ "WHERE (MATCH(title) AGAINST(? IN BOOLEAN MODE) ) AND movies.id = genres_in_movies.movieId AND genres.id = genreId AND movies.id = stars_in_movies.movieId AND starId = stars.id \n"
						+ "GROUP BY movies.id\n"
						+ "LIMIT ? OFFSET ?;\n";
			}
			else
			{
				System.out.println(id);
				// Query by search fields: parse first
				String[] fields = id.split("-");

				// Populate the search fields into variables to be used for setString() in PreparedStatements
				try 
				{
					if (fields[0] != null)
					{
						fieldsQueryTitle = fields[0];
					}
					if (fields[1] != null)
					{
						fieldsQueryYear =fields[1];
					}
					if (fields[2] != null)
					{
						fieldsQueryDirector = fields[2];
					}
					if (fields[3] != null)
					{
						//fieldsQuery += " AND stars.name LIKE '%" + fields[3] + "%'";
						fieldsQueryStar = fields[3];
					}
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
			
				// Query 
				query = "SELECT movies.id, movies.title, movies.year, movies.director,\n"
					+ "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name SEPARATOR',') as 'genreList',\n"
					+ "GROUP_CONCAT(DISTINCT genres.id ORDER BY genres.name SEPARATOR',') as 'genreIdList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name SEPARATOR',') as 'starList',\n"
					+ "GROUP_CONCAT(DISTINCT stars.id ORDER BY stars.name SEPARATOR',') as 'starIdList', rating\n"
					+ "FROM movies, ratings, genres, genres_in_movies, stars, stars_in_movies\n"
					+ "WHERE movies.id = ratings.movieId AND movies.id = genres_in_movies.movieId AND genres.id = genreId AND movies.id = stars_in_movies.movieId AND starId = stars.id \n"
					+ "AND movies.title LIKE ? \n"  				// 1 fieldsQueryTitle
					+ "AND CAST(movies.year AS CHAR(4)) LIKE ? \n"	// 2 fieldsQueryYear
					+ "AND movies.director LIKE ? \n"				// 3 fieldsQueryDirector
					+ "GROUP BY movies.id\n"
					+ "HAVING starList LIKE ? \n";                  // 4 fieldsQueryStar
				if (sort.equalsIgnoreCase("title"))
					query += "ORDER BY title ";
				else
					query += "ORDER BY rating ";
				if (order.equalsIgnoreCase("ASC"))
					query += "ASC\n";
				else
					query += "DESC\n";
				query += "LIMIT ? OFFSET ?;\n";						// 7 results, 8 (Integer.parseInt(page) * Integer.parseInt(results)

			}
			
			// Prepare the statement before populating the ?'s based on search type
			jdbcStartTime = System.nanoTime();
			PreparedStatement stmt = dbcon.prepareStatement(query);
			jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
			// Fill in the ? parameters based on search type
			if (search.equals("genre"))
			{
				jdbcStartTime = System.nanoTime();
				// 1 id, 2 sort, 3 order, 4 results, 5 (Integer.parseInt(page) * Integer.parseInt(results))
				stmt.setString(1, id);
				stmt.setInt(2, Integer.parseInt(results));
				stmt.setInt(3, Integer.parseInt(page) * Integer.parseInt(results));
				jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
			}
			else if (search.equals("title"))
			{
				jdbcStartTime = System.nanoTime();
				// 1 id, 2 sort, 3 order, 4 results, 5 (Integer.parseInt(page) * Integer.parseInt(results))
				stmt.setString(1, id + "%");
				stmt.setInt(2, Integer.parseInt(results));
				stmt.setInt(3, Integer.parseInt(page) * Integer.parseInt(results));
				jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
			}
			else if (search.equals("fts"))
			{
				System.out.println("title=" + id);
				jdbcStartTime = System.nanoTime();
				stmt.setString(1, id);
				stmt.setInt(2,  Integer.parseInt(results));
				stmt.setInt(3,  Integer.parseInt(page) * Integer.parseInt(results));
				jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
			}
			else
			{
				// 1 fieldsQuery, 2 test, 3 sort, 4 order, 5 results, 6 (Integer.parseInt(page) * Integer.parseInt(results))
				jdbcStartTime = System.nanoTime();
				stmt.setString(1, "%" + fieldsQueryTitle + "%");
				stmt.setString(2, "%" + fieldsQueryYear + "%");
				stmt.setString(3, "%" + fieldsQueryDirector + "%");
				stmt.setString(4, "%" + fieldsQueryStar + "%");
				stmt.setInt(5, Integer.parseInt(results));
				stmt.setInt(6, Integer.parseInt(page) * Integer.parseInt(results));
				jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
			}
			jdbcStartTime = System.nanoTime();
			ResultSet rs = stmt.executeQuery();
			jdbcCurrentTime += System.nanoTime() - jdbcStartTime;

			JsonArray jsonArray = new JsonArray();
			boolean first = true;
			
			query = "SELECT rating FROM ratings WHERE ratings.movieId=?";
			jdbcStartTime = System.nanoTime();
			stmt = dbcon.prepareStatement(query);
			jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
			if (!isAndroid)
			{
				while (rs.next()) 
			     {
						String movieId = rs.getString("movies.id");
						String tempMovieTitle = rs.getString("title");
						String movieTitle = Top20ServletAPI.makeMovieLink(movieId, tempMovieTitle);
						String movieYear = rs.getString("year");
						String movieDirector = rs.getString("director");
						String[] genreList = rs.getString("genreList").split(",");
						String[] genreIdList = rs.getString("genreIdList").split(",");
						String movieGenres = Top20ServletAPI.makeGenreLink(genreIdList, genreList);
						String[] starList = rs.getString("starList").split(",");
						String[] starIds = rs.getString("starIdList").split(",");
						String movieStars = Top20ServletAPI.combineStrings(starList, starIds);
						String movieRating = rs.getString("rating");
							/*
							jdbcStartTime = System.nanoTime();
							stmt.setString(1, movieId);
							ResultSet ratingSet = stmt.executeQuery();
							jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
							if (ratingSet.next())
							{
								movieRating = ratingSet.getString("rating");
							}
							*/
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
				while (rs.next()) 
			     {
						String movieId = rs.getString("movies.id");
						String movieTitle = rs.getString("title");
						//String movieTitle = Top20ServletAPI.makeMovieLink(movieId, tempMovieTitle);
						String movieYear = rs.getString("year");
						String movieDirector = rs.getString("director");
						//String[] genreList = rs.getString("genreList").split(",");
						//String[] genreIdList = rs.getString("genreIdList").split(",");
						String movieGenres = rs.getString("genreList");
						//String[] starList = rs.getString("starList").split(",");
						//String[] starIds = rs.getString("starIdList").split(",");
						String movieStars = rs.getString("starList");
						//String movieRating = "N/A";
	
         // Create a JsonObject based on the data we retrieve from rs
						
			            JsonObject jsonObject = new JsonObject();
			            jsonObject.addProperty("movie_id", movieId);
			            jsonObject.addProperty("movie_title", movieTitle);
			            jsonObject.addProperty("movie_year", movieYear);
			            jsonObject.addProperty("movie_director", movieDirector);
			            jsonObject.addProperty("movie_genres", movieGenres);
			            jsonObject.addProperty("movie_stars", movieStars);
			            jsonArray.add(jsonObject);
			      }
			}
			
			out.write(jsonArray.toString());
			
			response.setStatus(200);
			jdbcStartTime = System.nanoTime();
			rs.close();
			stmt.close();
			dbcon.close();
			jdbcCurrentTime += System.nanoTime() - jdbcStartTime;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		long servletEndTime = System.nanoTime();
		servletTotalTime = servletEndTime - servletStartTime;
		System.out.print("JDBC time: " + jdbcCurrentTime + " Servlet time: " + servletTotalTime);
		
		String output = Long.toString(servletTotalTime);
		output += " " + Long.toString(jdbcCurrentTime);
		try
		{
			//FileWriter writer = new FileWriter("output.txt", true);
			FileWriter writer = new FileWriter(getServletContext().getRealPath("/WEB-INF") + "/output.txt", true);
			writer.write(output);
			writer.write("\r\n");
			writer.write("JDBCTIMERTEST");
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	

}


