/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString)
{
	resultDataJson = resultDataString;
	
	console.log("Handling login response:");
	console.log(resultDataJson);
	console.log("resultDataJson['status']: " + resultDataJson["status"]);
	
	// If login succeeds, re-route to index.html (main page)
	if (resultDataJson["status"] === "success")
	{
		if (resultDataJson["type"] === "customer")
			window.location.replace("index.html");
		else if (resultDataJson["type"] === "employee")
			window.location.replace("_dashboard.html");
	}
	else
	{
		// If login fails, display login error message on the web page
		console.log("Login failed; showing error message:");
		console.log("resultDataJson['message']: " + resultDataJson["message"]);
		$("#login_error_message").text(resultDataJson["message"]);
		grecaptcha.reset();
	}
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent)
{
	console.log("Submitting login form data.");
	
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    
    /*
     * Handling the Post event from the login page with the data under 
     * */
    $.post(
    	"api/login",
    	
    	// Serialize the login form to the data in the login_form form object on web page,
    	// and post it to the back-end
    	$("#login_form").serialize(),
    	
    	(resultDataString) => handleLoginResult(resultDataString)
    );
}

/*
 * Bind the login_form's form data to the Login submit button to a handler function.
 * Basically, this means when the login_form's Login button is clicked, submitLoginForm
 * function is bound to handle the post for logging in.
 * */
$("#login_form").submit((event) => submitLoginForm(event));