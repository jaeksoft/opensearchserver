## Parsers API

Use this API to work with parsers.

* [Getting the list of parsers](list.md)
* [Retrieving details for a parser](get.md)
* [Parsing a file by uploading it](parse_upload_file.md)
* [Parsing a file located on the server](parse_local_file.md)
* [Parsing a file and letting OpenSearchServer detect its type](parse_detect_mime.md)

Parsers allow for the extraction of information within documents. They can handle several types of documents: `.doc`, `.xsl`, `.xml`, `.html`, ...

The returned information depends on the parser and on the type of document. 

For example the PDF parser will return, for a PDF file:

* the title,
* the author,
* the full text content,
* the producer,
* the creation date,
* the modification date,
* the language,
* the number of pages,
* the content detected with OCR,
* and some other information.
