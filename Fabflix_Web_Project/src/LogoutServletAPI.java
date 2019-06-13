import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
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

@WebServlet(name="LogoutServletAPI", urlPatterns="/api/logout")
public class LogoutServletAPI extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	 
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
    	
		HttpSession session = request.getSession();
		
		JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", "logout");
        response.getWriter().write(responseJsonObject.toString());

		session.invalidate();
	}
}
