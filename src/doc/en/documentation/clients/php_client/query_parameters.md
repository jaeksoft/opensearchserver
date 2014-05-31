## Initialization
First, the boilerplate code to instanciate an OssApi object:

```php 
require_once(dirname(__FILE__).'/oss_api.class.php');
require_once(dirname(__FILE__).'/oss_results.class.php');

$oss_url = 'http://localhost:9090';
$oss_index = 'my_index';
$oss_login = 'my_login';
$oss_key = '98hg72de4f27cefbcb7a771335b98735e'
$oss_api = new OssApi($oss_url, $oss_index, $oss_login, $oss_key);
```

## Basic search query
Now we're going to search:
* for the keyword `open`
* using the pre-defined OpenSearchServer `search` query template
  * this query being pre-configured in OpenSearchServer, its parameters are already set -- search pattern or fields to search in, returned fields, snippets, etc. 
  * however, the PHP code allows for passing additional parameters, and for overwriting existing parameters
* among documents that have a `date`
* among documents with a value within the `visibilty` field that is not zero
* for 10 documents at most

We also specify that the keyword we're looking for is from the English language.

```php
$xmlResult = $oss_search->query('open')
                        ->lang('en')
                        ->template('search')
                        ->filter('date:[* TO *]') //range query are used here. See the Lucene syntax information at http://lucene.apache.org/core/2_9_4/queryparsersyntax.html
                        ->negativeFilter('visibility:0') 
                        ->rows(10)
                        ->execute(60);
```

> Please note that the language of the keyword isn't necessarily the language of the documents! Here `->lang('en')` tells OpenSearchServer that the keywords are in English, so OSS can process them using language-specific transformations. However returned documents could be in various languages - for instance the word "open" also exists in Dutch.
> If our goal was to solely return documents in English, we would use `->filter('lang:en')` .


## Looping through results

`$xmlResult` now holds the OpenSearchServer results. We need to iterate through these, for example to build an array of titles and categories found among the results.

In this example one document can have several categories, since the `category` field can be **multivalued**.

First, let's have a look at the beginning of the returned XML so as to better understand the whole process:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <header>
    <status>0</status>
    <query>...</query>
  </header>
  <result name="response" numFound="45" collapsedDocCount="0" start="0" rows="10" maxScore="6,932" time="1">
    <doc score="0" pos="0" docId="51198">
      <field name="title">A new president</field>
      <field name="category">politics</field>
      <field name="category">usa</field>
      ...
```

The `<result` node provides important information:
* `numFound`: total number of documents matching our query
* `collapsedDocCount`: number of documents _collapsed_. Collapsing is not used in this example, hence zero.
* `start`: offset value of the first document. Since we did not request a specific offset value OpenSearchServer started at 0.
* `rows`: number of queried rows. It further indicates the maximum number of documents that can be returned in this results set.

Then come several `<doc` nodes (only one is shown here), which also provide important data:
* attribute `pos`: the position (offset) of the document in the global results set.
* fields with `name="category"`: these are identical nodes, though they carry different values.

Now let's use this information and some other PHP classes to build our array of results:

```php
$oss_result = new OssResults($xmlResult);         //wraps the XML output into a specific object
$doc_found_number = $oss_result->getResultRows(); //we'll assume for now that the number of results is the number of returned rows

$results = array();
for ($i = 0; $i < $doc_found_number; $i++) {      //for each `<doc` node in the results set:
  $pos = $oss_result->getResultRows() + $i;
  $title = $oss_result->getField($pos, 'title');  //each field must be accessed with its 
                                                  //name and the position of the document in the results set
  $categories = $oss_result->getField($pos, 'category', false, 
                                            false, null, true); //the last 'true' in this sequence 
                                                                //means that we're requesting every value within the multivalued 'category' field.
  $results[] = array('title' => $title, 'categories' => $categories);
}
```

> Getting the actual number of returned documents would be a bit more tricky. The correct line to do so is:
```php
$doc_found_number = min(
          $this->oss_result->getResultRows(), 
          $this->oss_result->getResultFound() - $this->oss_result->getResultCollapsedCount() - $startOffset
       );
```
> Here we get:
> * either the number of queried rows, but this will not be correct for the last "page" -- for instance for the documents with a position value from 40 to 44.
> * or the total number of results, minus the number of collapsed documents, minus the offset. In our example for "page" 4 this would be: 45 - 0 - (4 * 10) = 5.

### Displaying the results

Now that results have been stored in an array, we could display them in a list:

```php
print '<ul>';
foreach($results as $result) {
  $categories = is_array($result['categories'])) ? implode(', ', $result['categories']) : $result['categories'];
  print '<li>'.$result['title'].' - <em>'.$categories.'</em></li>';
}
print '</ul>';
```

> This example displays:
>
> * A new president - _politics_, _usa_

## Debugging

A useful tool for debugging is the `$oss_search->getLastQueryString()` method. It returns the URL used by the query. This URL can be copied/pasted in a browser to access the XML output from OpenSearchServer. 

It can also be used to access any error coming from OpenSearchServer.

```php

$xmlResult = $oss_search->query('open')
                         ...
                        ->execute(60);

echo '<a href="'.$oss_search->getLastQueryString().'" target="_blank">Click to view OSS output</a>';
```


## Logging data

OpenSearchServer can log every query run on an index. To activate this function, set `->setLog(true)` on our `$oss_search` object.

Furthermore, up to 10 customised log parameters can be sent with the query (occupying positions 0 to 9), using `->setCustomLog($pos, $data)`. Typical uses include sending a session identifier or an IP.

> Log entries can then be used in the OpenSearchServer `Report` tab.

Completed code for the query:

```php
$xmlResult = $oss_search->query('open')
                        ->lang('en')
                        ->template('search')
                        ->filter('date:[* TO *]') 
                        ->negativeFilter('visibility:0') 
                        ->setLog(true)                             //activates the logging of the query
                        ->setCustomLog(0, $_SERVER['REMOTE_ADDR']) //adds the IP of the client (basic IP retrieval 
                                                                   //that does not handle any 'x_forwarded_for' parameter)
                        ->setCustomLog(1, $username)               //also sends an imaginary username
                        ->rows(10)
                        ->execute(60);
```
