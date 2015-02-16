## Parse a document by automatically detecting its MIME type.

Use this API to send a file or to parse a file located on the server: OpenSearchServer will try to automatically detect its MIME type to apply the correct parser on it.

**Requirement:** OpenSearchServer v1.5.11

### Call parameters

**URL:** ```/services/rest/parser?name={optionnal_file_name}&type={optionnal_mime_type}&path={file_to_crawl_if_located_on_server}&p.{any_parser_property}={property_value}```

**Method:** ```PUT```

**Header**:

- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**

- **_lang_** (optionnal): The language of the document.
- **_p.{any_parser_property}_** (optionnal): Any property of the parser.
- **_type_** (optionnal): MIME type of the file, if known. This will help OpenSearchServer detecting the correct parser.

**If parsing a file located on the server:** 

- **_path_**: File path for file to parse if it is located on the server.

**If sending a file to parse:**

- **_name_** (optionnal): The name of the file that is sent. This will help OpenSearchServer detecting the correct parser.

**Binary data (PUT):**

If the file is sent to the server for parsing: body of the request is the file to parse.

### Success response

OpenSearchServer will try to detect which parser to use on the file. It will use:

1. name of the file, if given as a parameter, and especially its extension.
2. type of the file, if given as a parameter.
3. header of the file.

It is a good practice to send at least the name of the file to ensure correct detection of the parser to use.

The parser has been applied to file (sent or the one whose path has been given as parameter). Appropriate parser has been automatically detected and used. Every extracted fields are returned.

**HTTP code:**
200

**Content (application/json):**


    {  
    "items":[  
      [  
         {  
            "fieldName":"author",
            "values":[  
               "m.garden"
            ]
         },
         {  
            "fieldName":"content",
            "values":[  
               "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
			   "Vivamus facilisis enim in libero rhoncus, id pretium augue porta.",
			   "Cras nec ante risus. Aenean condimentum, velit non"
			   "blandit egestas, leo felis pharetra sapien, sed feugiat."
            ]
         },
         {  
            "fieldName":"creation_date",
            "values":[  
               "Tue Feb 10 11:43:28 CET 2013"
            ]
         },
         {  
            "fieldName":"lang",
            "values":[  
               "fr"
            ]
         },
         {  
            "fieldName":"lang_method",
            "values":[  
               "ngram recognition"
            ]
         },
         {  
            "fieldName":"modification_date",
            "values":[  
               "Tue Feb 12 11:43:28 CET 2013"
            ]
         },
         {  
            "fieldName":"number_of_pages",
            "values":[  
               "2"
            ]
         },
         {  
            "fieldName":"producer",
            "values":[  
               "GPL Ghostscript 8.64"
            ]
         },
         {  
            "fieldName":"title",
            "values":[  
               "Rules of competition"
            ]
         }
      ]
     ]
    }        

### Error response

The parsing failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        http://localhost:8080/services/rest/parser?name=book.pdf&p.pdfCrackCommandLine=/usr/bin/pdfcrack
    
