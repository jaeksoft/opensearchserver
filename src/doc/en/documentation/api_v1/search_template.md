## Search Template API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/searchtemplate

The Search Template API is used to create, delete, setreturnfields, setsnippetfield,Search Template is for querying the index.

**Global parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**cmd**_ (optional): The action to perform: create, delete, setreturnfields, setsnippetfield.

**Parameters for template creation:**
- _**qt.name**_ (required): The name of the query Template or Search Template.
- _**qt.query**_ (required): The Pattern Query of the search Template.
- _**qt.operator**_ (required): The default Operator for search it can be AND or OR search.
- _**qt.rows**_ (required): The number of rows to be return.
- _**qt.slop**_ (required): The tolerance for words between the terms being searched.
- _**qt.lang**_ (required): The language to be searched.

**Parameters for template deletion:**
- _**qt.name**_ (required): The name of the query Template or Search Template to be deleted.

**Parameters for setting return fields:**
- _**qt.name**_ (required): The name of the query Template or Search Template.
- _**qt.returnfield**_ (required): The name of the Field to be returned.

**Parameters for setting snippet fields:**
- _**qt.name**_ (required): The name of the query Template or Search Template.
- _**qt.maxSnippetSize**_ (required): The maximum size of the snippet.
- _**qt.tag**_ (required): The HTML tag to be highlighted on search keyword.
- _**qt.Snippetfield**_ (required): The name of the field to be set as Snippet.
- _**qt.fragmenter**_ (required): Fragmenter is one in which how the snippet is spitted into chunks. It is an enum value listed below:
  - NoFragmenter: Indiscriminately quotes the field from its beginning.
  - SizeFragmenter: It centres the snippet on the target terms, then extends the snippet rightward and leftward until it hits the maximum allowed size.
  - SentenceFragmenter: It centres the snippet on the target terms, then extends the snippet rightward and leftward in a way that strives to preserve the sentences in the text.

### Example

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
