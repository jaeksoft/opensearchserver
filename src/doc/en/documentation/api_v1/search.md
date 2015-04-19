## Search API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/select

The Search/Select API is used to query and search the OpenSearchServer index.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**q or query**_ (optional): The query or keyword need to be searched.
- _**qt (query template)**_ (required): The name of the query template used to load predefined settings for search.
- _**start**_ (optional): This specifies from which document the results will be displayed.
  - If the start value is 0 - The search results start from document #0.
  - If the start value is 10 - The search results start from document #10.
- _**rows**_ (optional): This specifies the number of documents to be returned.
- _**lang**_ (optional): A language restriction for this search. By default, OpenSearchServer searches among documents in all languages. The language can be given using the ISO 639-1 format, such as "en" or "fr". See http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes for more.
- _**collapse.mode**_ (optional): There are three ways of collapsing the search result documents:
  - off : No collapsing.
  - optimized: Consecutive documents will be collapsed if they share the specified field.
  - full: All documents sharing the specified field get collapsed.
- _**collapse.field**_ (optional): The field for collapsing the returned search documents.
- _**collapse.max**_ (optional): This specifies the number of consecutive documents that will trigger collapsing.
- _**rf or fl (returned field)**_ (optional): This sets the fields that need to be returned.
- _**fq (filter query)**_ (optional): Adds a filter to the current call. The parameters can be used several times in the same call for successive filters. Here is an example filter that only keeps the documents fitting the query: &fq=color:green.
- _**fqn (negative filter query)**_ (optional): Adds a negative filter. The parameters can be used several times. Here is an example filter that removes all documents fitting the query: &fqn=color:red.
- _**sort**_ (optional): Controls the order of the results. Use the abbreviation + or - to sort in ascending or descending order.
- _**facet**_ (optional): Enables faceting for the field passed as a parameter. You can add a number in parentheses to specify the minimum count.
- _**facet.multi**_ (optional): Same as with the facet parameter, but for fields containing multiple values (multi-valued fields).


### Examples

**HTTP Request:**

Searching for the keyword "a word" in French language documents:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&lang=fr
 
Searching with the collapse mode activated:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&collapse.mode=optimized&collapse.field=hostname&collapse.max=3
 
Searching documents using return fields:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&rf=date&rf=color
 
Searching documents using filters:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&fq=date:20101201&fq=color:red
 
Searching documents using facets:

    http://localhost:9090/select?use=index1&query=a+word&qt=template1&facet=color&facet.multi=date(1)

**Using PHP:**

Searching for the keyword "a word" in French language documents:
```php
$search = new OssSearch('http://localhost:9090', 'index1');$result = $search->template('template1')
                 ->query('a word')
                 ->lang('fr')
                 ->execute();
```
 
Searching with the collapse mode activated:

```php
$search = new OssSearch('http://localhost:9090', 'index1');
$result = $search->template('template1')
                 ->collapseMode('optimized')
                 ->collapseField('hostname')
                 ->collapseMax(3)
                 ->query('a word')
                 ->execute();
```

Searching documents using filters:

```php
$search = new OssSearch('http://localhost:9090', 'index1');$result = $search->template('template1')
                 ->query('a word')
         ->filter('color:red');
                 ->execute();
```

Searching documents using facets:
```php
$search = new OssSearch('http://localhost:9090', 'index1');
$result = $search->template('template1')
                 ->facet('color', 5, true)
                 ->facet('date', 1, true)
                 ->query('a word')
                 ->execute();
```
