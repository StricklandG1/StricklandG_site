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

function handleResult(resultData) {

    console.log("handleResult: populating search from resultData");
    console.log(resultData);
    
    if (resultData.length)
    {
	    let search_header = jQuery("#search_header");
	    let id = "";
	    if (search_type == "genre") 
	    {
	    	
	    }
	    else if (search_type == "title")
	    {
	    	id = " - " + id_param;
	    }
	    else
	    	id = " - " + id_param.replace(/_/g, " ");
	    search_header.append("Browse by: " + search_type + id);
	
	    let search_tr = jQuery("#search_tr");
	    search_tr.append("<th>Movie Title<a href=\"?search=" + search_type +"&id=" + id_param + "&sort=title&order=asc&page=" + page_num + "&results=" + results_num +"\">^</a><a href=\"?search=" + search_type +"&id=" + id_param + "&sort=title&order=desc&page=" + page_num + "&results=" + results_num +"\">v</a></th>");
	    search_tr.append("<th>Year</th>");
	    search_tr.append("<th>Director</th>");
	    search_tr.append("<th>Genre(s)</th>");
	    search_tr.append("<th>Stars</th>");
	    search_tr.append("<th>Rating<a href=\"?search=" + search_type +"&id=" + id_param + "&sort=rating&order=asc&page=" + page_num + "&results=" + results_num +"\">^</a><a href=\"?search=" + search_type +"&id=" + id_param + "&sort=rating&order=desc&page=" + page_num + "&results=" + results_num +"\">v</a></th>");
	    search_tr.append("<th>Action</th>");

	    // Populate the star table
	    // Find the empty table body by id "movie_table_body"
	    let searchTableBodyElement = jQuery("#search_table_body");
	
	    // Concatenate the html tags with resultData jsonObject to create table rows
	    for (let i = 0; i < resultData.length; i++) {
	    	let rowHTML = "";
	        rowHTML += "<tr>";
	        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]["movie_title"] + '</a></th>'
	        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
	        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
	        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
	        rowHTML += "<th>" + resultData[i]["movie_stars"] + "</th>";
	        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
	        rowHTML += "<th><a onclick=\"addItemToCart('" + resultData[i]['movie_id'] + "')\" id='addItemButton' class='btn btn-primary btn-block' style=\"color:white\"> Add To Cart </a></th>";

	        rowHTML += "</tr>";
	
	        // Append the row created to the table body, which will refresh the page
	        searchTableBodyElement.append(rowHTML);
	    }
    }
    else
	{
    	let searchErrorElement = jQuery("#error_message");
    	let resultError = "Search returned no results.";
    	searchErrorElement.append(resultError);
	}
}

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


let search_type = getParameterByName('search');
let id_param = getParameterByName('id');
let sort_by = getParameterByName('sort');
let order_by = getParameterByName('order');
let page_num = getParameterByName('page');
let results_num = getParameterByName('results');

function addToCart(movieId)
{
	console.log(movieId);
}

function nextPage()
{
	let temp = parseInt(page_num);
	++temp;
	page_num = temp.toString();
	window.location.href="?search="+search_type+"&id="+id_param+"&sort="+sort_by+"&order="+order_by+"&page="+page_num+"&results="+results_num;
}

function prevPage()
{
	let temp = parseInt(page_num);
	if (temp > 0)
		--temp;
	page_num = temp.toString();
	window.location.href="?search="+search_type+"&id="+id_param+"&sort="+sort_by+"&order="+order_by+"&page="+page_num+"&results="+results_num;
}

function results(x)
{
	window.location.href="?search="+search_type+"&id="+id_param+"&sort="+sort_by+"&order="+order_by+"&page="+page_num+"&results="+x;
}

jQuery.ajax({
	
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/search?search="+search_type+"&id="+id_param+"&sort="+sort_by+"&order="+order_by+"&page="+page_num+"&results="+results_num,
    error: function (xhr, status ) { alert(status);},
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
