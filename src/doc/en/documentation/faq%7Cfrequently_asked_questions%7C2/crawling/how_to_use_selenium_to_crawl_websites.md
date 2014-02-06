**OpenSearchServer 1.5** introduces a new powerful scripting feature. It is now possible to drive a web browser (Firefox, PhantomJS) using a written script.

The new scripting feature can:

* Open a web browser,
* Execute Javascript,
* Extract data using an XPATH query, a CSS selector, or the ID of the web element,
* Insert documents in an index.

A set of REST APIs has been created to manage the scripts. Each script can be executed/stored/updated/deleted by calling it by its name.

This new feature is particularly useful when crawling websites protected by a login/password form, websites making heavy use of Javascript to display content -- or when the values that need to be indexed can only be defined by precise CSS or XPATH selectors.

 

## JSON script

Each command is a JSON structure with a command value and an array of parameters.


    {
      "command": "WEBDRIVER_OPEN",
      "parameters": [ "FIREFOX"]
    }

A script is an array of JSON command structures. Here is an example:


    [
     { "command": "WEBDRIVER_OPEN", "parameters": [ "PHANTOMJS"] },
     { "command": "WEBDRIVER_SET_TIMEOUTS", "parameters": [ 60, 60 ] },
     { "command": "WEBDRIVER_RESIZE", "parameters": [ 1024, 768 ] },
     { "command": "WEBDRIVER_GET", "parameters": [ "{url}" ] },
     { "command": "SLEEP", "parameters": [ 1 ] },
     { "command": "INDEX_DOCUMENT_NEW", "parameters": [ "english" ] },
     { "command": "INDEX_DOCUMENT_ADD_VALUE", "parameters": [ "url", "{url}" ] },
     { "command": "INDEX_DOCUMENT_ADD_VALUE", "parameters": [ "title", "Test" ] },
     { "command": "CSS_SELECTOR_INDEX_FIELD", "parameters": [ "content", "div ul li a" ] },
     { "command": "INDEX_DOCUMENT_UPDATE" }
    ]

This script executes the following actions:

* open a PHANTOMJS window,
* set a timeout duration of one minute,
* the width and the height of the window are set to 1024×768,
* a WEB page is loaded, based on the `{url}` parameter,
* the script waits for one second,
* an index document is created,
* the fields `url` and `title` are set,
* a CSS selector locates a text element in the web page. These elements are stored in the `content` field.
* the document is inserted within the index.

## RESTful API

A set of RESTful API is available.

### Saving the script

To store the script, use the following call:

* URL: http://localhost:9090/services/rest/index/script/script/{script_name}
* HTTP Method: PUT
* HTTP Header: Content-Type: application/json
* Payload: The JSON script

### Running the script

To run the script use the following call:

* URL: http://localhost:9090/services/rest/index/script/script/{script_name}/run
* HTTP Method: POST
* HTTP Header: Content-Type: application/json
* Payload: The JSON structure with the variable
* The JSON structure for the variables:

`{"url": "http://www.open-search-server.com", "name": "John Doe" }`

###Subscript###

A script can call a subscript for each web element found by a selector.

  
    [
     { "command": "WEBDRIVER_OPEN", "parameters": [ "PHANTOMJS" ] },
     { "command": "WEBDRIVER_SET_TIMEOUTS", "parameters": [ 60, 60 ] },
     { "command": "WEBDRIVER_RESIZE", "parameters": [ 1024, 768 ] },
     { "command": "WEBDRIVER_GET", "parameters": [ "http://www.dmoz.org/" ] },
     { "command": "SLEEP", "parameters": [ 3 ] },
     { "command": "CSS_SELECTOR_SUBSCRIPT", "parameters": [ "dmoz_sub", "div#catalogs span a" ] },
     { "command": "WEBDRIVER_CLOSE" }
    ]

This script will extract all the root categories of the homepage of Dmoz.org. Then, for each category found, the subscript `dmoz_sub` will be called.

## Full list of commands

* `WEBDRIVER_OPEN`: open a web browser
	* **parameter 1**: name of driver. Possible values: `PHANTOMJS`, `FIREFOX`.
* `WEBDRIVER_CLOSE`: close the browser
* `WEBDRIVER_NEW_WINDOW`: open a new window and keeps the session running
* `WEBDRIVER_CLOSE_WINDOW`: close the current window
* `WEBDRIVER_SET_TIMEOUTS`: define a timeout delay after which script execution will be stopped.
	* **parameter 1**: delay, in seconds
* `WEBDRIVER_RESIZE`: resize window
	* **parameter 1**: width
	* **parameter 2**: height
* `WEBDRIVER_GET`: access an URL
	* **parameter 1**: URL. Variables can be used within this parameter.
* `SLEEP` : pause script execution
	* **parameter 1**: time, in seconds
* `CSS_SELECTOR_SUBSCRIPT`, `XPATH_SELECTOR_SUBSCRIPT`: select an element and run a subscript. The selected element must be an `<a` or an `<img`. The `href` or `src` attribute will be passed as the `{url}` variable of the subscript.
	* **parameter 1**: name of the script  
	* **parameter 2**: selector, CSS or XPATH depending on the command.
* `WEBDRIVER_JAVASCRIPT`: execute some Javascript.
	* **parameter 1**: Javascript commands to execute. Variables can be used within this parameter.
* `SCRIPT`: call another script
	* **parameter 1**: name of the script
* `CSS_SELECTOR_CLICK_AND_SCRIPT`, `XPATH_SELECTOR_CLICK_AND_SCRIPT`: select an element, click on it, wait some time and run a script
	* **parameter 1**: selector, CSS or XPATH depending on the command.
	* **parameter 2**: name of the script
	* **parameter 3**: time to wait between the click and the script execution
* `INDEX_DOCUMENT_NEW`: create a new document to be indexed
	* **parameter 1**: name of the new document
* `INDEX_DOCUMENT_ADD_VALUE`: add a value in the current new document
	* **parameter 1**: field
	* **parameter 2**: value. Variables can be used in this parameter.
* `INDEX_DOCUMENT_UPDATE`: commit the current new document to the index
* `VAR_NEW_REGEX` : create a new variable by using a regexp on a variable. The new variable can then be used in the script and subscripts.
	* **parameter 1**: variable on which to apply the regexp
	* **parameter 2**: regexp. The capture group will give the new variable its value.
	* **parameter 3**: new variable name
* `CSS_SELECTOR_DOWNLOAD`, `XPATH_SELECTOR_DOWNLOAD` : select an element and download the URL within the `href` attribute. Selected element must be an `<a`.
	* **parameter 1**: directory into which the file will be download. It will be fully (recursively) created if it does not exist. Variables can be used within this parameter.
	* **parameter 2**: selector, CSS or XPATH depending on the command.
* `WEBDRIVER_DOWNLOAD`: download the current file indicated by the `{url}` variable.
	* **parameter 1**: directory into which the file will be downloaded. It will be fully (recursively) created if it does not exist. Variables can be used within this parameter.
* `PARSER_MERGE`: merge every PDF document from a directory to a new PDF document.
	* **parameter 1**: name of the PDF parser. Value can only be "PDF parser" at the moment.
	* **parameter 2**: directory that contains PDF files to merge.
	* **parameter 3**: full path (including filename) to the PDF to be created
* `SEARCH_TEMPLATE_JSON`: execute a query on the index and run an action depending on the result.
	* **parameter 1**: name of the query template to use for the search
	* **parameter 2**: keywords to use for the search query
	* **parameter 3**: JSON path to match a specific part of the result
	* **parameter 4**: action to take. Values can be:
		* `EXIT_IF_NOT_FOUND`: exit current script if the JSON path does not match
		* `IF_FOUND`: if the JSON path matches:
			* **parameter 5**: must be `NEXT_COMMAND`
			* **parameter 6**: next command to run. Will often be `WEBDRIVER_CLOSE_WINDOW`