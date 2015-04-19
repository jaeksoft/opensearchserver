## Retrieving a parser

This API returns the properties of a parser, as well as a list of fields it returns.

**Requirement:** OpenSearchServer v1.5.9

### Call parameters

**URL:** ```/services/rest/parser/{parser_name}```

**Method:** ```GET```

**URL parameters:**

- **_parser\_name_** (required): The name of the parser.

**Header** (optional returned type):

- Accept: ```application/json``` or ```application/xml```

### Success response

The data about the parser is returned either in JSON or in XML format. The supported file types and file extensions are shown.

**HTTP code:**
200

**Content (application/json):**


    {  
	   "name":"PDF (Pdfbox)",
	   "properties":{  
		  "ghostscriptBinaryPath":{  
			 "label":"Ghostscript binary path",
			 "description":"The path of the Ghostscript binary file."
		  },
		  "sizeLimit":{  
			 "label":"Size Limit",
			 "description":"The Size Limit of the file to be Parsed",
			 "defaultValue":"0"
		  },
		  "pdfCrackCommandLine":{  
			 "label":"PDFCrack command line",
			 "description":"The command line used to execute PDFCrack."
		  }
	   },
	   "fields":[  
		  "parser_name",
		  "title",
		  "author",
		  "subject",
		  "content",
		  "producer",
		  "keywords",
		  "creation_date",
		  "modification_date",
		  "language",
		  "number_of_pages",
		  "ocr_content",
		  "image_ocr_boxes",
		  "pdfcrack_password"
	   ],
	   "file_extensions":[
			"pdf"
	   ],
	   "mime_types":[
			"application/pdf"
	   ]
	}

### Error response

The parser was not returned. The reason is provided in the content.

**HTTP code:**
500, 404 (other than 200)

**Content (text/plain):**
    
    Parser not found: myParser
    

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/parser/pdf
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/parser/pdf
    }).done(function (data) {
       console.log(data);
    });
    
