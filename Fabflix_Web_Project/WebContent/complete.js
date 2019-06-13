function handleResult(resultData)
{
    console.log("handleResult: populating final confirmation table from resultData");
    console.log(resultData);
    
    let completeTableElement = jQuery("#complete_table_body");
    let total = 0.0;
    
    if (resultData.length)
    {
    	console.log("Printing cart contents.");

    	for (let i = 0; i < resultData.length; i++) {
	    	let rowHTML = "";
	    	rowHTML += "<tr>";
	        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
	        rowHTML += "<th>" + resultData[i]["movie_quantity"] + "</th>";
	        
	        let USDcost = resultData[i]["movie_cost"];
	        
	        total += USDcost;
	     
	        USDcost = USDcost.toFixed(2);
	        rowHTML += "<th>" + "$" + USDcost + "</th>";
	        rowHTML += "<th>" + resultData[i]["sales_id"] + "</th>";
	        rowHTML += "</tr>";
	        
	        // Append the row created to the table body, which will refresh the page
	        completeTableElement.append(rowHTML);
	    }
    	
    	let totalElement = jQuery("#total_calc");
    	let rowHTML = "<h5 style='text-align:center'>" + "Total price: $" + (total.toFixed(2)).toString() + "</h5>";
    	totalElement.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/complete",
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});



///////////// Handling the logout to remove the session cookie ////////////
function handleLogoutResult(resultDataString)
{
	resultDataJson = resultDataString;
	
	console.log("Handling logout response:");
	console.log(resultDataJson);
	console.log(resultDataJson["status"]);
	
	if (resultDataJson["status"] === "logout")
	{
		window.location.replace("login.html");
	}
}

function submitLogout()
{
	$.post(
		"api/logout",
		{
			"logout" : true
		},
		(resultDataString) => handleLogoutResult(resultDataString)
	);
	
}
$("#logout_button").submit((event) => submitLogout(event));