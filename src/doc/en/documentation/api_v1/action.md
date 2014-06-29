## Action API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/action

The action API performs actions such as:

- optimize: optimize the index
- reload: reload the index
- deleteAll: truncate the index
- online: set the index online
- offline: set the index offline
- readonly: set the index to read only mode
- readwrite: set the index to read/write mode.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**action**_ (required): The action to perform: optimize, reload, deleteAll, online, offline, readonly, readwrite


### Example

Optimize an index

    http://localhost:9090/action?use=indexname&action=optimize
 
Truncate an index. This call includes authentication parameters

    http://localhost:9090/action?use=indexname&action=deleteAll&login=admin&key=6020b80a823e276727a0d6a23167d52b

PHP:

```php
$ossapi = new OssApi('http://localhost:9090', 'index1');
$ossapi->credential('admin', '584207379c568c724de883717be8');
$ossapi->optimize();
```

### HTTP response

It indicates that the index was optimized.

```xml
<response>
    <entry key="Status">OK</entry>
</response>
```