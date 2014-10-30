## Parse a document by uploading it

Use this API to send a file to OpenSearchServer and use a parser on it.

**Requirement:** OpenSearchServer v1.5.9

### Call parameters

**URL:** ```/services/rest/parser/{parser_name}?lang={optional_lang}&p.{any_parser_property}={property_value}```

**Method:** ```PUT```

**Header**:

- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**

- **_parser\_name_** (required): The name of the parser to use.
- **_lang_** (optionnal): The language of the document.
- **_p.{any_parser_property}_** (optionnal): Any property of the parser.


**Binary data (PUT):**

Body of the request is the file to parse.

### Success response
The parser has been applied to the given file. Every extracted fields are returned.

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
        -d @rulesofcompetitions.pdf \
        http://localhost:8080/services/rest/parser/pdf?p.pdfCrackCommandLine=/usr/bin/pdfcrack
    
