# XML file

In this example, we have one file containing three documents. Our goal is to create one document in the index for each document in the file.

![XML file used in this example](xpath_1_xmlfile.png)

# Schema

Our schema has 3 fields :
  
![Details of 3 fields](xpath_2_schema.png)

![Unique and default fields](xpath_3_schema2.png)

# Parser

Delete any existing parser handling XML files. In this example, the default « XML » parser gets deleted:

![Delete parser XML](xpath_4_parser_delete.png)

Create a new « XML (XPATH) » parser :

![Create new parser](xpath_5_parser_new.png)
 
Name this new parser and write « /documents/document » in the field « XPATH request for documents » - since this is the XPATH request to access each document:

![Create new parser](xpath_6_parser_1.png)
 
In the tab « Supported extension », add « xml » :

![Create new parser](xpath_7_parser_2.png)
 
In the tab « Supported MIME type », add handling for the following types :
* application/xhtml+xml
* application/xml
* text/xml

In the tab « Field mapping », configure this mapping :
 
![Create new parser](xpath_8_parser_3.png)

This tells the parser to fetch :
* /documents/document/date into the « date » field
* /documents/document/id into the « id » field
* /documents/document/title into the « title » field

Click « Create » :

![Create new parser](xpath_9_parser_4.png)
 
# Crawl the file

In this example, we place the file in a folder then configure a new file crawler :

![Configure file crawler](xpath_10_crawler.png)
 
We launch the crawler :

![Launch file crawler](xpath_11_crawler_2.png)
 
The file is now indexed. Check the information about the index on the main page:
 
![Check index info](xpath_12_indexinfo.png)

The index now includes three documents.

# Query documents

A query template can be quickly created to see documents:

![Query documents](xpath_13_query.png)
