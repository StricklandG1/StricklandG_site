/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating single star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "single_star_header"
    let singleStarNameElement = jQuery("#single_star_header");
    
    let birthYear = (resultData[0]["star_birth_year"] == null) ? "N/A" : resultData[0]["star_birth_year"];
    
    singleStarNameElement.append("<p>" + resultData[0]['star_name'] + " (" + birthYear + ")" + "</p>");

    console.log("handleResult: populating single star table from resultData");

    // Populate the star table
    // Find the empty table body by id "single_star_movie_table_body"
    let movieTableBodyElement = jQuery("#single_star_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th><a href=single_movie.html?id=" + resultData[i]["movie_id"] + ">" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    let homeLink = "";
    if (resultData[0]["search"] != null)
    	homeLink = "<div style='padding-left:0.8em'><a href=\"search.html?search="+resultData[0]["search"]+"&id="+resultData[0]["id"]+"&sort="+resultData[0]["sort"]+"&order="+resultData[0]["order"]+"&page="+resultData[0]["page"]+"&results="+resultData[0]["results"]+"\">Back</a></div>";
    else // Link to route back to the main page
    	homeLink + "<div style='padding-left:0.8em'><a href=index.html>Back</a></div>"; 
    console.log("Single Star Search = " + resultData[0]["search"]);
    movieTableBodyElement.append(homeLink)
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single_star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
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