# XML file

One file containing 3 documents. We want to create one document in our index for each document of the file.

![XML file used in this example](xpath_1_xmlfile.png)

# Schema

Our schema has 3 fields :
  
![Details of 3 fields](xpath_2_schema.png)

![Unique and default fields](xpath_3_schema_2.png)

# Parser

Delete any existing parser handling XML files. For example here the default « XML » parser is deleted:
 
![Delete parser XML](xpath_4_parser_delete.png)

Create a new « XML (XPATH) » parser :

![Create new parser](xpath_5_parser_new.png)
 
Give this new parser a name and write « /documents/document » in field « XPATH request for documents » as it is the XPATH request to access each document:

![Create new parser](xpath_6_parser_1.png)
 
In tab « Supported extension » add « xml » :

![Create new parser](xpath_7_parser_2.png)
 
In tab « Supported MIME type » add handling for theses types :
* application/xhtml+xml
* application/xml
* text/xml
In tab « Field mapping » configure this mapping :
 
![Create new parser](xpath_8_parser_3.png)

This tell the parser to fetch :
* /documents/document/date into field « date »
* /documents/document/id into field « id »
* /documents/document/title into field « title »

Click « Create » :

![Create new parser](xpath_9_parser_4.png)
 
# Crawl file

For example place your file in a folder and configure a new file crawler :

![Configure file crawler](xpath_10_crawler.png)
 
Launch crawler :

![Launch file crawler](xpath_11_crawler_2.png)
 
File is now indexed. Check index’s info on main page:
 
![Check index info](xpath_12_indexinfo.png)

Index now has 3 documents.

# Query documents

A query template can be quickly created to see documents:

![Query documents](xpath_13_query.png)
