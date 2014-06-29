## Search API

_**This API is deprecated, have a look at the [new RESTFul API](../api_v2/README.html)**_

    http://{server_name}:9090/select

The Search/Select API is used to query and search the OpenSearchServer index.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**q or query**_ (optional): The query or keyword need to be searched.
- _**qt (query template)**_ (required): The query template name which is used to load a pre-loaded settings for search.
- _**start**_ (optional): The start parameter specifies that from which document the result need to be displayed.
  - If the start value is 0 - The search result starts from 0.
  - If the start value is 10 - The search result starts from 10.
- _**rows**_ (optional): The rows parameter specifies the number of documents need to be returned.
- _**lang**_ (optional):The language specified search. OpenSearchServer searches for particular document. For default is searches with all languages. The language can be "en"or "fr" which is an ISO 639-1 format.For more refer http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes.
- _**collapse.mode**_ (collapse.mode): Collapsing the search result document.
  - off : No collapsing .
  - optimized: The consecutive documents will be collapsed with with specified field.
  - full: All the document with same fields collapsed.
- _**collapse.field**_ (optional): The field for collapsing the returned search documents.
- _**collapse.max**_ (optional): The collapse.max parameter specifies the number of consecutive documents needed for collapsing.
- _**rf or fl (returned field)**_ (optional): The rf fields sets the fields that need to be returned.
- _**fq (filter query)**_ (optional): Adds a filter to the current call. The parameters can be used several times in the same call for successive filters. Example: This filter will keep only the documents which fit this query: &fq=color:green.
- _**fqn (negative filter query)**_ (optional): Adds a negative filter. The parameters can be used several times. Example: This filter will remove any documents which fit this query: &fqn=color:red.
- _**sort**_ (optional): Controls the order of the results. Use the abbreviation + ou - to sort in ascending or descending order.
- _**facet**_ (optional): Enables faceting for the field passed as a parameter. You can add a number in parentheses to specify the minimum count.
- _**facet.multi**_ (optional): Same as with parameter facet, for use with fields containing multiple values (multi-valued fields).


### Example

**HTTP Request:**

To search a keyword "a word" in french language:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&lang=fr
 
Search with collapse mode:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&collapse.mode=optimized&collapse.field=hostname&collapse.max=3
 
Search documents with return fields:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&rf=date&rf=color
 
Search documents with filters:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&fq=date:20101201&fq=color:red
 
Search documents with facets:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&facet=color&facet.multi=date(1)

**Using PHP:**

To search a keyword "a word" in french language:

```php
$search = new OssSearch('http://localhost:9090', 'index1');$result = $search->template('template1')
                 ->query('a word')
                 ->lang('fr')
                 ->execute();
```
 
Search with collapse mode:

```php
$search = new OssSearch('http://localhost:9090', 'index1');
$result = $search->template('template1')
                 ->collapseMode('optimized')
                 ->collapseField('hostname')
                 ->collapseMax(3)
                 ->query('a word')
                 ->execute();
```

Search documents with filter:

```php
$search = new OssSearch('http://localhost:9090', 'index1');$result = $search->template('template1')
                 ->query('a word')
         ->filter('color:red');
                 ->execute();
```

Search documents with facets:

```php
$search = new OssSearch('http://localhost:9090', 'index1');
$result = $search->template('template1')
                 ->facet('color', 5, true)
                 ->facet('date', 1, true)
                 ->query('a word')
                 ->execute();
```