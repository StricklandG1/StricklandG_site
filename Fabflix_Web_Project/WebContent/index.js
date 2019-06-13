
function handleTop20Result(resultData) 
{
    console.log("handleTop20Result: populating top 20 table from resultData");

    // Populate the star table
    // Find the empty table body by id "top_20_table_body"
    let top20TableBodyElement = jQuery("#top_20_table_body");

    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_stars"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        // Need to use onclick function call within index.js that handles adding a new item to the cart
        rowHTML += "<th><a onclick=\"addItemToCart('"+ resultData[i]["movie_id"]  +"')\" id='addItemButton' value='" + resultData[i]["movie_id"] + "' class='btn btn-primary btn-block' style=\"color:white\"> Add To Cart</a></th>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        top20TableBodyElement.append(rowHTML);
    }
}
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/top_20",
    success: (resultData) => handleTop20Result(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

function addItemToCart(movieId, submitEvent)
{
	console.log("Adding item into a cart...");

	let buttonValue = movieId;
	let status = "add";
	console.log("buttonValue: " + buttonValue);
	console.log("status: " + status);
    //submitEvent.preventDefault();

    // THE CORRECT STRUCTURE FOR AJAX POST: $.post( [servlet url] , [data] , [function] )
    $.post(
        "api/cart", 
        {
        	"status" : "add",
        	"movie_id" : buttonValue
        }, 
        function(){ 
        	console.log(buttonValue);
        }
    );
    window.alert("You've added a movie to your cart!");
}
$("#addItemButton").submit((event) => addItemToCart(event));

/**
 * Helper functions
 * */
//Placeholder for function handling adding items into a cart
function submitSearchForm() {
	console.log("Retrieving search form's input fields:");
	
	let searchTitle = (document.getElementById('search_title').value).replace(/ /g, "_");
	let searchYear = document.getElementById('search_year').value;
	let searchDirector = (document.getElementById('search_director').value).replace(/ /g, "_");
	let searchStar = (document.getElementById('search_star').value).replace(/ /g, "_");
	
	console.log(searchTitle);
	console.log(searchYear);
	console.log(searchDirector);
	console.log(searchStar);
	
	let URL = "search.html?search=form&id=" + searchTitle + "-" + searchYear + "-" + searchDirector + "-" + searchStar + "&sort=title&order=asc&page=0&results=10";
	
	console.log("URL:" + URL);
	
	window.location.href = URL;
}

/////////////////////////////////////////////// START Auto-complete logic ////////////////////////////////////////////////////
var movieMap = new Map();


function handleLookup(query, doneCallback){
	console.log("autocomplete initiated")
	
	// TODO: check past queries
	if (movieMap.has(query))
	{
		handleLookupAjaxSuccess(movieMap.get(query), query, doneCallback)
		console.log("grabbing cached data")
	}
	else
	{
		console.log("fetching data from database")
		jQuery.ajax({
			"method": "GET",
			url: "api/auto_complete_search?search_value=" + escape(query),
			"success": function(data){
				handleLookupAjaxSuccess(data, query, doneCallback)
			},
			"error": function(errorData){
				console.log("lookup ajax error")
				console.log(errorData)
			}
		})
	}
}

function handleLookupAjaxSuccess(data, query, doneCallback){
	console.log("lookup ajax successful")
	console.log(data)
	// cache results using map
	movieMap.set(query, data);
	doneCallback({suggestions: data});
}

function handleSelectSuggestion(suggestion){
	// jump to page based on selection SPECIFIC MOVIE PAGE
	console.log(suggestion["data"]["id"])
	window.location.replace("single_movie.html?id=" + suggestion["data"]["id"])
}

function handleOnClickSuggestion(suggestion){
	window.location.replace("single_movie.html?id=" + suggestion["data"]["id"])
}

function handleNormalSearch(query){
	console.log("doing normal search with query: " + query);
	// redirect based on the normal search
	window.location.replace("search.html?search=fts&id="+query+"&sort=title&order=ASC&page=0&results=10");

}

$('#autocomplete').autocomplete({
	lookup: function(query, doneCallback){
		handleLookup(query, doneCallback)
	},
	onSelect: function(suggestion){
		handleSelectSuggestion(suggestion)
	},
	select: function(suggestion){
		handleOnClickSuggestion(suggestion)
	},
	deferRequestBy: 300,
	minChars: 3
})

$('#autocomplete').keypress(function(event) {
	if (event.keyCode == 13) {
		handleNormalSearch($('#autocomplete').val())
	}
})

// Handle submit button onclick
function handleBtn(submitEvent)
{
	//submitEvent.preventDefault();
	console.log("using button");
	let value = document.getElementById("autocomplete").value;
	
	handleNormalSearch(value);
}
$('#autcomplete_btn').submit((event) => handleBtn(event));

/////////////////////////////////////////////// END Auto-complete logic ////////////////////////////////////////////////////

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