import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

/**
 * Servlet implementation class DashboardServletAPI
 */
@WebServlet(name="DashboardServletAPI", urlPatterns="/api/_dashboard")
public class DashboardServletAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		try
		{
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL @ DashboardServlet1");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/MasterWrite");
            
            if (ds == null)
                out.println("ds is null @ DashboardServlet1.");
            
        	Connection dbcon = ds.getConnection();
        	if (dbcon == null)
                out.println("dbcon is null @ DashboardServlet1.");
        	
			java.sql.DatabaseMetaData databaseMetaData = dbcon.getMetaData();
			
			ResultSet tableSet = databaseMetaData.getTables(null, "moviedb", null, new String[] {"TABLE"});
			JsonArray tableArray = new JsonArray();
			
			//System.out.println("Printing Tables: ");
			while (tableSet.next())
			{
				String tableName = tableSet.getString("TABLE_NAME");
				//System.out.println(tableName);
				ResultSet cols = databaseMetaData.getColumns(null,null,tableName,null);
				while (cols.next())
				{
					JsonObject colJson = new JsonObject();
					String colName = cols.getString("COLUMN_NAME");
					String datatype = cols.getString("DATA_TYPE");
					String colSize = cols.getString("COLUMN_SIZE");
					// String decimaldigits = cols.getString("DECIMAL_DIGITS");
					String isNullable = cols.getString("IS_NULLABLE");
					String is_autoIncrement = cols.getString("IS_AUTOINCREMENT");
					
					// brute force data type with if statements
					if (datatype.equals("12"))
					{
						datatype = "varchar(" + colSize + ")";
					}
					else if (datatype.equals("91"))
					{
						datatype = "date";
					}
					else if (datatype.equals("4"))
					{
						datatype = "int(" + colSize + ")";
					}
					
					colJson.addProperty("table_name", tableName);
					colJson.addProperty("col_name", colName);
					colJson.addProperty("col_datatype", datatype);
					colJson.addProperty("col_nullable", isNullable);
					colJson.addProperty("col_auto_inc", is_autoIncrement);
					//System.out.println(colName + " " + datatype + " " + isNullable + " " + is_autoIncrement);
					tableArray.add(colJson);
				}
			}
			
			out.write(tableArray.toString());
			response.setStatus(200);
			
			dbcon.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.setContentType("application/json");
		String starName = request.getParameter("add_star_name");
		String starYear = request.getParameter("add_star_year");
		
		System.out.println("star name: " + starName);
		System.out.println("star year: " + starYear);
		
		try
		{
			if (verifyYear(starYear))
			{	
				Connection dbcon = dataSource.getConnection();
				
				String query = "SELECT MAX(id) FROM stars;";
				Statement stmt = dbcon.createStatement();
				
				ResultSet rs = stmt.executeQuery(query);
				String id = "";
				if (rs.next())
				{
					id = rs.getString("MAX(id)");
					id = generateStarId(id);
				}
				
				query = "INSERT INTO stars VALUES(?, ?, ?);";
				
				PreparedStatement statement = dbcon.prepareStatement(query);
				
				statement.setString(1, id);
				statement.setString(2, starName);
				statement.setInt(3, Integer.parseInt(starYear));
				
				statement.executeUpdate();
				
				dbcon.close();
				
				JsonObject employeeSuccessObject = new JsonObject();

				employeeSuccessObject.addProperty("status", "success");
				employeeSuccessObject.addProperty("message", "Success!");
				
				response.getWriter().write(employeeSuccessObject.toString());
			}
			else
			{
				throw new Exception("Invalid birth year entered.");
			}
		}
		catch (Exception e)
		{
			JsonObject employeeFailObject = new JsonObject();
			
			employeeFailObject.addProperty("status", "fail");
			employeeFailObject.addProperty("message", "Invalid birth year entered. Please try again.");
			
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
	
	public static String generateStarId(String id)
	{
		String result = "";
		int num = 1 + Integer.parseInt(id.substring(2));
		result = "nm" + Integer.toString(num);
		return result;
	}
}
