The OssPaging class makes pagination easy.

In this example we will reuse the sample code from [our page explaining a basic query.](Run-a-query-with-different-parameters).

We are now going to improve this code by adding:
* a way to get the current results page from the URL;
* an offset value in the query to OpenSearchServer, so it can return the appropriate documents;
* a way to display the pagination links.

Here is the additional code:

```php 
require_once(dirname(__FILE__).'/oss_api.class.php');
require_once(dirname(__FILE__).'/oss_results.class.php');
require_once(dirname(__FILE__).'/oss_paging.class.php'); //requires the OssPaging class

$oss_url = 'http://localhost:9090';
$oss_index = 'my_index';
$oss_login = 'my_login';
$oss_key = '98hg72de4f27cefbcb7a771335b98735e'
$oss_api = new OssApi($oss_url, $oss_index, $oss_login, $oss_key);


$start_offset = (isset($_GET['p'])) ? $_GET['p'] : 0;    //retrieves the current page from URL, defaults to 0.
$number_by_page = 10;                                    //sets the display to 10 documents per page

$xmlResult = $oss_search->query('open')
                        ->lang('en')
                        ->template('search')
                        ->filter('date:[* TO *]')
                        ->negativeFilter('visibility:0') 
                        ->start($start_offset)           //we use 'start()' to tell OpenSearchServer what is the offset
                        ->rows($number_by_page)          //OpenSearchServer will return at most $number_by_page document by query
                        ->execute(60);

$oss_result = new OssResults($xmlResult);        
$doc_found_number = min(
          $this->oss_result->getResultRows(), 
          $this->oss_result->getResultFound() - $this->oss_result->getResultCollapsedCount() - $startOffset
       );

$results = array();
for ($i = 0; $i < $doc_found_number; $i++) {
  $pos = $oss_result->getResultRows() + $i;
  $title = $oss_result->getField($pos, 'title');
  $categories = $oss_result->getField($pos, 'category', false, false, null, true);
  $results[] = array('title' => $title, 'categories' => $categories);
}

print '<ul>';
foreach($results as $result) {
  $categories = is_array($result['categories'])) ? implode(', ', $result['categories']) : $result['categories'];
  print '<li>'.$result['title'].' - <em>'.$categories.'</em></li>';
}
print '</ul>';


$ossPaging = new OssPaging($xmlResult);                   //creates an OssPaging object
if ($ossPaging->getResultPrev() < $ossPaging->getResultCurrentPage()) {
  $url = $ossPaging->getPageBaseURI() . ($ossPaging->getResultPrev());
  print '<a href="'.$url.'">Previous page</a>';           //displays a link to the previous page
}
if ($ossPaging->getResultNext() > $ossPaging->getResultCurrentPage()) {
  $url = $ossPaging->getPageBaseURI() . ($ossPaging->getResultNext());
  print '<a href="'.$url.'">Next page</a>';               //displays a link to the next page
}
```

This is just a basic example of what can be done with the `OssPaging` class. We could improve on this by creating links to the first and to the last page of results, or by offering links to several results pages preceding or following the current one.

To do so, simply use other methods within `OssPaging`: `getResultTotal()`, `getResultLow()`, `getResultHigh()`, etc.
