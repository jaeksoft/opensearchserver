## Parsers API

Use this API to work with parsers.

* [Getting the list of parsers](list.md)
* [Retrieving details for a parser](get.md)
* [Parsing a file by uploading it](parse_upload_file.md)
* [Parsing a file located on the server](parse_local_file.md)

Parsers allow for extraction of information inside documents. Parsers can handle several types of documents: `.doc`, `.xsl`, `.xml`, `.html`, ...
Depending on the parser (and on the type of document), different information will be returned. 

For example the PDF parser will be able to return, for a PDF file:

* title,
* author,
* full text content,
* producer,
* creation date,
* modification date,
* language,
* number of pages,
* content detected with OCR,
* and some other information.

