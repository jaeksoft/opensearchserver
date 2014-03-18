Autocompletion uses a combination of Javascript / AJAX and PHP.

A Javascript script must call a PHP script each time there is a change in the search input. This can for example be done by using jQuery UI autocomplete feature, with the `remote datasource` option: http://jqueryui.com/autocomplete/#remote.

PHP script is responsible for calling OpenSearchServer to get some search suggestion and return them to Javascript script, encoded in JSON for instance.

PHP script could look like this one:

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
  $result_array = explode("\n", $result);            //this API return data in plain text, one suggestion by line
  $count = count($result_array)-1;
  for($i=0;$i<$count;$i++) {
    $completions[] = array('value' => $result_array[$i]);
  }
  
  return json_encode($completions);                   //encode array in JSON for facilitating its use in Javascript
}
return '';
```