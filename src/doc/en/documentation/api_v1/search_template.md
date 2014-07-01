## Search Template API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/searchtemplate

The Search Template API is used to create and delete templates, set return fields, and set snippet fields.

**Global parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**cmd**_ (optional): The action to perform: create, delete, setreturnfields, setsnippetfield.

**Parameters for template creation:**
- _**qt.name**_ (required): The name of the query template or search template.
- _**qt.query**_ (required): The pattern query of the search template.
- _**qt.operator**_ (required): The default operator for search. It can be set to AND or OR.
- _**qt.rows**_ (required): The number of rows to be returned.
- _**qt.slop**_ (required): The tolerance for the number of words between the terms being searched.
- _**qt.lang**_ (required): The language of the documents to be searched in.

**Parameters for template deletion:**
- _**qt.name**_ (required): The name of the query template or search template to be deleted.

**Parameters for setting return fields:**
- _**qt.name**_ (required): The name of the query template or search template.
- _**qt.returnfield**_ (required): The name of the field to be returned.

**Parameters for setting snippet fields:**
- _**qt.name**_ (required): The name of the query template or search template.
- _**qt.maxSnippetSize**_ (required): The maximum size of the snippet.
- _**qt.tag**_ (required): The HTML tag to use to highlight search keywords.
- _**qt.Snippetfield**_ (required): The name of the field to be set as a snippet.
- _**qt.fragmenter**_ (required): Determines how the snippet is splitted into chunks. It is an ENUM value among the following options:
  - NoFragmenter: Indiscriminately quotes the field from its beginning.
  - SizeFragmenter: Centers the snippet on the target terms, then extends the snippet rightward and leftward until it hits the maximum allowed size.
  - SentenceFragmenter: Centers the snippet on the target terms, then extends the snippet rightward and leftward in a way that strives to preserve the sentences in the text.

### Examples

**HTTP Request:**

    http://localhost:9090/searchtemplate?use=index1&cmd=delete&qt.name=search
    http://localhost:9090/searchtemplate?use=index1&cmd=create&qt.name=search&qt.query=title:($$)^10 OR title:("$$")^10  OR content:($$)^10 OR content:("$$")^10&qt.operator=AND&qt.rows=10&qt.slop=2&qt.lang=ENGLISH 

**Using PHP:**

```php
$searchTemplate=new OssSearchTemplate("http://localhost:9090", "index1");
$searchTemplate->createSearchTemplate("search1", 'title:($$)^10 OR title:("$$")^10 OR content:($$)^10 OR content:("$$")^10 OR',
    "AND", "10", "2", "ENGLISH");
$searchTemplate->setSnippetField("search1", "title", 150, "b", "1", "NoFragmenter");
```
