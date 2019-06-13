function handleDashboardResult(resultData)
{
    console.log("handleResult: populating dashboard table from resultData");
    console.log(resultData);
    
    let metadataBodyElement = jQuery("#metadata_body");
    
    if (resultData.length)
	{
    	console.log("Printing metadata contents onto the HTML table.");
    	
    	let oldColumn = "";
    	for (let i = 0; i < resultData.length; i++)
    	{
        	let rowHTML = "";
        	
        	let newColumn = resultData[i]["table_name"];
        	
        	if (newColumn !== oldColumn)
        	{
	    		rowHTML += "<span style='padding-left:1em'><strong>" + resultData[i]["table_name"] + "</strong></span>";
	    		rowHTML += "<div class='card mb-3'>";
		        	rowHTML += "<div class='card-header'>";
		        		rowHTML += "<div id='database_table' class='col-md-10' style='padding-left:4em' width='50px'>";
			        		rowHTML += "<strong>Column name:</strong> " + resultData[i]["col_name"] + "<br>";
			        		rowHTML += "<strong>Datatype:</strong> " + resultData[i]["col_datatype"] + "<br>";
			        		rowHTML += "<strong>Nullable?:</strong> " + resultData[i]["col_nullable"] + "<br>";
			        		rowHTML += "<strong>Auto-increment?: </strong> " + resultData[i]["col_auto_inc"] + "<br>";
		                rowHTML += "</div>";
		        	rowHTML += "</div>";
		        rowHTML += "</div>";
		        oldColumn = resultData[i]["table_name"];
		    	metadataBodyElement.append(rowHTML);
        	}
        	else
        	{
	    		rowHTML += "<div class='card mb-3'>";
	        	rowHTML += "<div class='card-header'>";
	        		rowHTML += "<div class='col-md-10' style='padding-left:4em'>";
		        		rowHTML += "<strong>Column name:</strong> " + resultData[i]["col_name"] + "<br>";
		        		rowHTML += "<strong>Datatype:</strong> " + resultData[i]["col_datatype"] + "<br>";
		        		rowHTML += "<strong>Nullable?:</strong> " + resultData[i]["col_nullable"] + "<br>";
		        		rowHTML += "<strong>Auto-increment?: </strong> " + resultData[i]["col_auto_inc"] + "<br>";
	                rowHTML += "</div>";
	        	rowHTML += "</div>";
	        rowHTML += "</div>";
	    	metadataBodyElement.append(rowHTML);
        	}
    	}
	}
}
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/_dashboard",
    error: function (xhr, status ) { alert(status);},
    success: (resultData) => handleDashboardResult(resultData) // Setting callback function to handle data returned successfully by the CheckoutServlet
});



// Check to see if current user logged in is an employee or not
function checkUser(resultData)
{
	resultDataJson = resultData;
	console.log("checking user type for dashboard");
	
	if (resultDataJson["type"] != "employee")
		window.location.replace("index.html")
}
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/_checkEmployee",
    error: function (xhr, status ) { alert(status);},
    success: (resultData) => checkUser(resultData) // Setting callback function to handle data returned successfully by the CheckoutServlet
});


//////////////////////////// Adding New Star ////////////////////////////////

// Handles the result of employee attempting to add a movie into the database
// and displays messages on the web browser accordingly
function handleAddStarResult(resultDataString)
{
	resultDataJson = JSON.parse(resultDataString);
	
	console.log("Handling employee's star to be added");
	console.log(resultDataJson);
	console.log(resultDataJson["status"]);
	console.log(resultDataJson["message"]);
	
	if (resultDataJson["status"] === "success")
	{
		alert("Star entry successfully added!");
		window.location.reload(3);
	}
	else
	{
		console.log("Entry attempt failed; showing error message:");
		console.log(resultDataJson["message"]);
		$("#add_star_error_message").text(resultDataJson["message"]);
	}
}

// Takes event object of employee submitting new star to add, and calls the
// DashboardServletAPI to use the form data and pass the results to 
// handleAddStarResult function above
function submitAddStarForm(formSubmitEvent)
{
	console.log("Submitting employee's star to be added");
	
	formSubmitEvent.preventDefault();
	
	$.post(
		"api/_dashboard",
		
		$("#add_star_form").serialize(),
		
		(resultDataString) => handleAddStarResult(resultDataString)
	);
}

// Catch the submit event from adding a movie by employee, and send the object
// to submitAddMovieForm function above
$("#add_star_form").submit((event) => submitAddStarForm(event));
////////////////////////////Adding New Star ////////////////////////////////

////////////////////////////Adding New Movie ///////////////////////////////

function handleAddMovieResult(resultData)
{
	resultDataJson = JSON.parse(resultData);
	
	console.log("Handling employee's movie to be added");
	console.log(resultDataJson);
	console.log(resultDataJson["status"]);
	console.log(resultDataJson["message"]);
	
	if (resultDataJson["status"] === "success")
	{
		alert(resultDataJson["message"]);
		window.location.reload(3);
	}
	else
	{
		console.log("Entry attempt failed; showing error message:");
		console.log(resultDataJson["message"]);
		$("#add_movie_error_message").text(resultDataJson["message"]);
	}
}

// Take event object of employee submitting new movie to add, and calls the
// DashboardServletAPI to use the form data and pass results to
// handleAddMovieResult function above
function submitAddMovieForm(formSubmitEvent)
{
	console.log("Submitting employee's movie to be added");

	formSubmitEvent.preventDefault();
	
	$.post(
		"api/_dashboard2",
		
		$("#add_movie_form").serialize(),
		
		(resultDataString) => handleAddMovieResult(resultDataString)
	);
}
$("#add_movie_form").submit((event) => submitAddMovieForm(event));
