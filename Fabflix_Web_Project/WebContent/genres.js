
function handleGenreResult(resultData) 
{
    console.log("handleSearchResult: populating search results table from resultData");
    console.log(resultData);
    
    let genreListTableBodyElement = jQuery("#genre_list_table_body");
    
    let rowHTML = "<div>";
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
    	if (i != resultData.length - 1)
    	{
	        rowHTML += "  ";
	        rowHTML += resultData[i]["genre_name"]; 
	        rowHTML += " | ";
    	}
    	else
    	{
    		rowHTML += "  ";
    		rowHTML += resultData[i]["genre_name"];
    		rowHTML += "  ";
    	}
    }
    rowHTML += "</div>";
    
    // Append the row created to the table body, which will refresh the page
    genreListTableBodyElement.append(rowHTML);
}


jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres",
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
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
