## Initialization
Start by common code to instanciate an OssApi object:

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
Let's search:
* for keyword `open`
* using our pre-defined `search` query template
  * this query has been pre-configured in OpenSearchServer: search pattern or fields to search in, returned fields, snippets, etc. 
  * In the PHP code some parameters can be added or overwritten.
* documents having a `date`
* documents having a value different from 0 in field `visibilty`
* for 10 documents at most

We also add an information about the language of the queried keywords, english here.

```php
$xmlResult = $oss_search->query('open')
                        ->lang('en')
                        ->template('search')
                        ->filter('date:[* TO *]') //range query are used here. See Lucene syntax: http://lucene.apache.org/core/2_9_4/queryparsersyntax.html
                        ->negativeFilter('visibility:0') 
                        ->rows(10)
                        ->execute(60);
```

> Language of the keywords is different from language of the documents! Here `->lang('en')` tells OpenSearchServer that keywords are in english (in order to apply some language specific transformation on them), but returned documents could be in various languages. 
> We would have use `->filter('lang:en')` to get back only english documents.


## Looping through results

`$xmlResult` now holds the result from OpenSearchServer. We need to iterate through these results, for example to build an array of results'titles and categories.

In this example one document can have several categories: field `category` can be **multivalued**.

Let's first have a look at the top part of the returned XML to better understand the whole process:
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

We can see some important information in the `<result` node:
* `numFound`: total number of documents matching our query
* `collapsedDocCount`: number of documents _collapsed_. This is not used here.
* `start`: offset of first document. Since we did not asked for a specific offset OpenSearchServer started at 0.
* `rows`: number of asked rows. It also tells the maximum number of documents that can be returned in this results set.

Then comes several `<doc` nodes (only one is shown here). Here again some important information is embedded:
* attribute `pos`: position (offset) of the document in the global results set.
* fields with `name="category"`: we can see here two identical nodes, except for their value.

Let's use this information and some other PHP classes to build our array of results:

```php
$oss_result = new OssResults($xmlResult);         //wrap XML result into a specific object
$doc_found_number = $oss_result->getResultRows(); //let's say for the moment than number of results equals number of returned rows

$results = array();
for ($i = 0; $i < $doc_found_number; $i++) {      //for each `<doc` node in the results set:
  $pos = $oss_result->getResultRows() + $i;
  $title = $oss_result->getField($pos, 'title');  //each field must be accessed with its 
                                                  //name and the position of the document in the results' set
  $categories = $oss_result->getField($pos, 'category', false, 
                                            false, null, true); //the last 'true' here 
                                                                //means we're asking for every values of the multivalued 'category' field.
  $results[] = array('title' => $title, 'categories' => $categories);
}
```

> Actually getting the real number of returned documents is a bit more tricky. The correct line would be:
```php
$doc_found_number = min(
          $this->oss_result->getResultRows(), 
          $this->oss_result->getResultFound() - $this->oss_result->getResultCollapsedCount() - $startOffset
       );
```
> Here we get:
> * either the number of asked rows, but this will not be OK for the last "page". For instance for documents from position 40 to 44.
> * or the total number of results minus the number of collapsed documents minus the offset. In our example for "page" 4 this would be: 45 - 0 - (4 * 10) = 5.

### Displaying results

Now that results are stored in an array they can for example be displayed in a list :

```php
print '<ul>';
foreach($results as $result) {
  $categories = is_array($result['categories'])) ? implode(', ', $result['categories']) : $result['categories'];
  print '<li>'.$result['title'].' - <em>'.$categories.'</em></li>';
}
print '</ul>';
```

> This would for example display:
>
> * A new president - _politics_, _usa_

## Debugging

A useful method that can be used to debug is the `$oss_search->getLastQueryString()` method. It returns the URL used by the query. This URL can be copied/pasted in a browser to access the XML output from OpenSearchServer. 

It can also be used to access any error coming from OpenSearchServer.

```php

$xmlResult = $oss_search->query('open')
                         ...
                        ->execute(60);

echo '<a href="'.$oss_search->getLastQueryString().'" target="_blank">Click to view OSS output</a>';
```


## Logging data

OpenSearchServer can log every query run on an index. We need to activate this logging by calling `->setLog(true)` on our `$oss_search` object.

Furthermore up to 10 custom log data can be sent with the query (from position 0 to 9) by using `->setCustomLog($pos, $data)`. This can be useful for instance to send a session identifier, an IP or any other parameter.

> Log entries can then be used in OpenSearchServer `Report` tab.

Completed code for the query:

```php
$xmlResult = $oss_search->query('open')
                        ->lang('en')
                        ->template('search')
                        ->filter('date:[* TO *]') 
                        ->negativeFilter('visibility:0') 
                        ->setLog(true)                             //activate logging of the query
                        ->setCustomLog(0, $_SERVER['REMOTE_ADDR']) //adding IP of the client (basic IP retrieval here, 
                                                                   //this does not handle any 'x_forwarded_for' parameter)
                        ->setCustomLog(1, $username)               //also send an imaginary username
                        ->rows(10)
                        ->execute(60);
```