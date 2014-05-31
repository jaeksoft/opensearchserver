The autocompletion function uses a combination of Javascript / AJAX and PHP.

A Javascript script must call a PHP script every time there is a change in the search input. One way to do so is to use jQuery's UI autocomplete feature, with the `remote datasource` option activated. More information about this option can be found at http://jqueryui.com/autocomplete/#remote.

The role of said PHP script is to call OpenSearchServer to get search suggestions, and return them to the Javascript script. In a typical use case these would be encoded in JSON.

Here is an example of such a PHP script:

```php
require_once(dirname(__FILE__).'/oss_api.class.php');
require_once(dirname(__FILE__).'/oss_autocompletion.class.php');

$oss_url = 'http://localhost:9090';
$oss_index = 'my_index';
$oss_login = 'my_login';
$oss_key = '98hg72de4f27cefbcb7a771335b98735e'

$keywords = (isset($_GET['q'])) ? $_GET['q'] : '';
if(!empty($keywords)) {
  $search = new OssAutocompletion($oss_url, $oss_index, $oss_login, $oss_key);
  $result = $search->autocomplete($keywords, 20);    //ask for 20 suggestions

  $result = str_replace("\r\n", "\n", $result);      
  $result_array = explode("\n", $result);            //this API returns data in plain text, one suggestion per line
  $count = count($result_array)-1;
  for($i=0;$i<$count;$i++) {
    $completions[] = array('value' => $result_array[$i]);
  }
  
  return json_encode($completions);                   //encodes the array in JSON to facilitate its use in Javascript
}
return '';
```
