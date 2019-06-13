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

    console.log("handleResult: populating movie info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let singleMovieTitle = jQuery("#single_movie_header");

    // Add the title of the movie at the top of the single movie view page
    singleMovieTitle.append("<p>" + resultData[0]['movie_title'] + "</p>");

    console.log("handleResult: populating single movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#single_movie_table_body");

    let rowHTML = "";
    // Concatenate the html tags with resultData jsonObject to create table rows
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData[0]["movie_year"] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_director"] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_genres"] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_stars"] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_rating"] + "</th>";
    rowHTML += "</tr>";
    
    if (resultData[0]["search"] != null)
    	rowHTML += "<tr><td><a href=\"search.html?search="+resultData[0]["search"]+"&id="+resultData[0]["id"]+"&sort="+resultData[0]["sort"]+"&order="+resultData[0]["order"]+"&page="+resultData[0]["page"]+"&results="+resultData[0]["results"]+"\">Back</a></td></tr>";
    else
    	rowHTML += "<tr><td><a href=index.html>Back</a></td></tr>";
    
    let cartButton = jQuery("#add_cart_button");
    let buttonHTML = "";
    buttonHTML += "<div><a onclick=\"addItemToCart('" + resultData[0]['movie_id'] + "')\" id='addItemButton' class='btn btn-primary btn-block' style=\"color:white\"> Add To Cart </a></div>";

    movieTableBodyElement.append(rowHTML);
    cartButton.append(buttonHTML);
    console.log("Search=" + resultData[0]["search"]);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single_movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


function addItemToCart(movieId, submitEvent)
{
	console.log("Adding item into a cart...");
	
	let buttonValue = movieId;
	console.log("buttonValue: " + buttonValue);
    //submitEvent.preventDefault();

    // THE CORRECT STRUCTURE FOR AJAX POST: $.post( [servlet url] , [data] , [function] )
    $.post(
        "api/cart", 
        {
        	"status" : "add",
        	"movie_id" : buttonValue
        }, 
        function(){ console.log(buttonValue);}
    );
    window.alert("You've added a movie to your cart!");
}
$("#addItemButton").submit((event) => addItemToCart(event));


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