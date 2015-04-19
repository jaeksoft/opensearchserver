## Parse a document by automatically detecting its MIME type.

When this API is used to send a file (or to parse a file on the server), OpenSearchServer attempts to detect the file's MIME type to apply the correct parser.

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
- **_type_** (optionnal): MIME type of the file, if known. This will help OpenSearchServer detect the correct parser.

**If parsing a file located on the server:** 

- **_path_**: File path to the file on the server.

**If sending a file to parse:**

- **_name_** (optional): The name of the file to send. This will help OpenSearchServer detect the correct parser.

**Binary data (PUT):**

If the file is sent to the server for parsing, the body of the request is the file to parse.

### Success response

OpenSearchServer will try to detect which parser to use on the file. The clues used to do so are:

1. the name of the file, if given as a parameter, and in particular its extension.
2. the type of the file, if given as a parameter.
3. the file's header.

We strongly suggest sending at least the name of the file to increase the odds of a correct detection.

If the process suceeds, the appropriate parser gets applied to the designated file. Every extracted field then gets returned.

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

The parsing failed. The reason why is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        http://localhost:8080/services/rest/parser?path=/home/book.pdf&p.pdfCrackCommandLine=/usr/bin/pdfcrack
    
