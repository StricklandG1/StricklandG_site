import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonObject;

@WebServlet(name="EmployeeCheckAPI", urlPatterns="/api/_checkEmployee")
public class EmployeeCheckAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		User user = (User)request.getSession().getAttribute("user");
		JsonObject responseObject = new JsonObject();
		responseObject.addProperty("type", user.getUserType());
		
		out.write(responseObject.toString());
	}
}