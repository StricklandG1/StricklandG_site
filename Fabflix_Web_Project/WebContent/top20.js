/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleTop20Result(resultData) {
    console.log("handleTop20Result: populating top 20 table from resultData");

    // Populate the star table
    // Find the empty table body by id "top_20_table_body"
    let top20TableBodyElement = jQuery("#top_20_table_body");

    // Iterate through resultData, no more than 10 entries
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
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        top20TableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
//jQuery.ajax({
//    dataType: "json", // Setting return data type
//    method: "GET", // Setting request method
//    url: "api/top-20", // Setting request url, which is mapped by StarsServlet in Stars.java
//    success: (resultData) => handleTop20Result(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
//});

$("#top_20_table_body").DataTable({
	"ajax":
		{
			type: "GET",
			url: "api/top_20",
			dataType: "json",
			success: (resultData) => handleTop20Result(resultData)
		}
});