function populateAtoZ() {
	console.log("Populating A ~ Z in titles page");
	
	let titleTableBodyELement = jQuery("#title_list_table_body");
	
	alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
	
	console.log(alphabet);
	
    for (let i = 0; i < alphabet.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>" +
            	alphabet[i] +
            "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        titleTableBodyELement.append(rowHTML);
    }
}


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