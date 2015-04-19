## Action API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/action

The action API performs actions such as:

- optimize: optimizes the index.
- reload: reloads the index.
- deleteAll: truncates the index.
- online: sets the index online.
- offline: sets the index offline.
- readonly: sets the index to read only mode.
- readwrite: sets the index to read/write mode.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**action**_ (required): The action to perform: optimize, reload, deleteAll, online, offline, readonly, readwrite.


### Examples

Optimize an index.

    http://localhost:9090/action?use=indexname&action=optimize
 
Truncate an index. This example call includes authentication parameters.

    http://localhost:9090/action?use=indexname&action=deleteAll&login=admin&key=6020b80a823e276727a0d6a23167d52b

PHP:

```php
$ossapi = new OssApi('http://localhost:9090', 'index1');
$ossapi->credential('admin', '584207379c568c724de883717be8');
$ossapi->optimize();
```

### HTTP response

The following indicates that the index was optimized.

```xml
<response>
    <entry key="Status">OK</entry>
</response>
```
