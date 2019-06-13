//////////////////////////// Display the shopping cart results to checkout page///////////////////////////////

function handleResult(resultData) {

    console.log("handleResult: populating checkout table from resultData");
    console.log(resultData);

    let cartTableBodyElement = jQuery("#cart_table_body");
    let cartHeaderElement = jQuery("#cart_table_header");

    // Concatenate the html tags with resultData jsonObject to create table rows
    if (resultData.length)
    {
    	console.log("Printing cart contents.");
    	
    	let headerHTML = "";
    	headerHTML = "<th>Movie Title</th>" +
    			"<th>Quantity</th>" +
    			"<th>Cost</th>" +
    			"<th>Update Qty</th>" +
    			"<th>Action</th>";
    	cartHeaderElement.append(headerHTML);

    	let total = 0;
    	
	    for (let i = 0; i < resultData.length; i++) 
	    {
	    	let rowHTML = "";
	    	rowHTML += "<tr>";
	        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
	        rowHTML += "<th>" + resultData[i]["movie_quantity"] + "</th>";
	        
	        let USDcost = resultData[i]["movie_cost"];
	        
	        total += USDcost;
	        
	        USDcost = USDcost.toFixed(2);
	        rowHTML += "<th>" + "$" + USDcost + "</th>";
	        rowHTML += "<th><input type='number' id='quantity" + resultData[i]["movie_id"] + "' oninput='javascript: if (this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength); 'id='quantity_update' min='0' class='form-control' maxlength='2' style='width:70px; text-align:center'></th>";
	        rowHTML += "<th><input type='submit' class='btn' onclick=\"updateQuantity('" + resultData[i]["movie_id"]  +"')\" value='Update'";
	        rowHTML += "</tr>";
	        
	        // Append the row created to the table body, which will refresh the page
	    	cartTableBodyElement.append(rowHTML);
	    }
	    
	    let totalElement = jQuery("#total");
	    let rowHTML = "";
        rowHTML += "<th></th>";
        rowHTML += "<th>TOTAL:</th>";
	    rowHTML +="<th align='right'>" + "$" + total.toFixed(2) + "</th>";
	    totalElement.append(rowHTML);
	    
	    let buttonElement = jQuery("#refresh_button");
    	let buttonHTML = '<input type="submit" style="position:absolute; right: 20px" class="btn-primary" onclick="window.location.reload()" value="Refresh Page">';
    	buttonElement.append(buttonHTML);
    }
    else
    {
    	console.log("Printing empty cart message.");
    	let errorMessageElement = jQuery("#error_message");
    	let rowHTML = "";
    	rowHTML += "<div>Your shopping cart is empty.</div>";
    	errorMessageElement.append(rowHTML);
    }
}

$.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/checkout",
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the CheckoutServlet
});

//////////////////////////// Display the shopping cart results to checkout page ///////////////////////////////


//////////////////////////// Handle the movie quantity update submission ///////////////////////////////////

function updateQuantity(movieId, formSubmitEvent)
{
	console.log("Submitting update quantity value.");
	let movieToUpdate = movieId;
	let elementId = "quantity" + movieId;
	let quantity = parseInt(document.getElementById(elementId).value);
	
	// Only handles the quantity update if an actual value is in the input
	if (quantity === 0 || quantity > 1 )
	{
		console.log("Movie ID: " + movieToUpdate);
		console.log("Quantity: " + quantity);
		
		// Send the update quantity data to the cart servlet
		$.post(
			"api/cart",
			{ 
				"status" : "update",
				"update_movie_id" : movieToUpdate,
				"update_quantity" : quantity
			}
		);
	}
}


//Catch the button click event for when the movie quantity update form is submitted
$("#updateQuantity").submit((event) => updateQuantity(event));

////////////////////////////Handle the movie quantity update submission ///////////////////////////////////


//////////////////////////// Handle the credit card checkout form submission ///////////////////////////////

// Handle the result of the database verification: if success, move to complete.html;
// Otherwise, notify user about the failure
function handleCheckoutResult(resultDataString)
{
	// Store the JSON result data from CheckoutServletAPI
	resultDataJson = resultDataString;
	
	console.log("Handling checkout response:");
	console.log(resultDataJson);
	console.log(resultDataJson["status"]);
	
	// If credit card verification succeeds, re-route to complete.html
	if (resultDataJson["status"] === "success")
	{
		window.location.replace("complete.html");
	}
	else
	{
		// If credit card verification fails, display error message on the page
		console.log("Credit card verification failed; showing error message:");
		console.log(resultDataJson["message"]);
		$("#cc_error_message").text(resultDataJson["message"]);
		
	}
}

// Get the data from the checkout form, serialize it into JSON, and send it to CheckoutServletAPI
// to verify data against database
function submitCheckoutForm(formSubmitEvent)
{
	console.log("Submitting checkout form data.");
	
	let ccId = (document.getElementById('cc_id').value);
	let ccFname = document.getElementById('cc_fname').value;
	let ccLname = (document.getElementById('cc_lname').value);
	let ccExp = (document.getElementById('cc_exp').value);

	$.post(
		"api/checkout",
		
		{ 
		  "cc_id" : ccId,
		  "cc_fname" : ccFname,
		  "cc_lname" : ccLname,
		  "cc_exp" : ccExp
		},
		
		(resultDataString) => handleCheckoutResult(resultDataString)
	);
}

// Catch the button click event for when the credit card checkout form is submitted
$("#submitCheckoutForm").submit((event) => submitCheckoutForm(event));

//////////////////////////// Handle the credit card checkout form submission ///////////////////////////////

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