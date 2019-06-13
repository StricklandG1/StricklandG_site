import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.CallableStatement;
import com.mysql.jdbc.DatabaseMetaData;

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

@WebServlet(name="DashboardServlet2API", urlPatterns="/api/_dashboard2")
public class DashboardServlet2API extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.setContentType("application/json");
		String movieTitle = request.getParameter("add_movie_title");
		String movieYear = request.getParameter("add_movie_year");
		String movieDirector = request.getParameter("add_movie_director");
		String movieStar = request.getParameter("add_movie_star");
		String movieGenre = request.getParameter("add_movie_genre");
		
		System.out.println("Verifying employee input for adding movie:");
		System.out.println("Movie title: " + movieTitle);
		System.out.println("Movie year: " + movieYear);
		System.out.println("Movie director: " + movieDirector);
		System.out.println("Movie star: " + movieStar);
		System.out.println("Movie genre: " + movieGenre);
		
		try
		{
			PrintWriter out = response.getWriter();
			if (verifyYear(movieYear))
			{
	        	Context initCtx = new InitialContext();

	            Context envCtx = (Context) initCtx.lookup("java:comp/env");
	            if (envCtx == null)
	                out.println("envCtx is NULL @ DashboardServlet2");

	            // Look up our data source
	            DataSource ds = (DataSource) envCtx.lookup("jdbc/MasterWrite");
	            
	            if (ds == null)
	                out.println("ds is null @ DashboardServlet2.");
	            
	        	Connection dbcon = ds.getConnection();
	        	if (dbcon == null)
	                out.println("dbcon is null @ DashboardServlet2.");
				
				// Insert stored procedure code here
				String result = "";
				String query = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
				java.sql.CallableStatement stmt =  dbcon.prepareCall(query);
				
				stmt.setString(1, movieTitle);
				stmt.setInt(2, Integer.parseInt(movieYear));
				stmt.setString(3, movieDirector);
				stmt.setString(4, movieGenre);
				stmt.setString(5, movieStar);
				stmt.registerOutParameter(6, java.sql.Types.VARCHAR);
				stmt.executeQuery();
				result = stmt.getString(6);
				System.out.println("result: " + result);
				
				response.setStatus(200);
				stmt.close();
				dbcon.close();
				
				JsonObject employeeSuccessObject = new JsonObject();

				employeeSuccessObject.addProperty("status", "success");
				employeeSuccessObject.addProperty("message", result);
				
				response.getWriter().write(employeeSuccessObject.toString());
			}
			else
			{
				throw new Exception("Invalid movie year entered.");
			}
		}
		catch (Exception e)
		{
			JsonObject employeeFailObject = new JsonObject();
			
			employeeFailObject.addProperty("status", "fail");
			employeeFailObject.addProperty("message", e.getMessage());
			
			response.getWriter().write(employeeFailObject.toString());
		}
	}
	
	public static boolean verifyYear(String year)
	{
		int strLen = year.length();
		if (strLen == 4)
		{
			boolean notDone = true;
			int i = 0;
			while (notDone && i < 4)
			{
				notDone = Character.isDigit(year.charAt(i));
				++i;
			}
			
			return notDone;
		}
		
		return false;
	}
	
	public static String generateMovieId(String id)
	{
		String result = "";
		int num = 1 + Integer.parseInt(id.substring(2));
		result = "tt" + Integer.toString(num);
		return result;
	}
}
