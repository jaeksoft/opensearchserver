## Optimize API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/optimize

This API optimizes the index.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.

### Examples

HTTP request:

    http://localhost:9090/optimize?use=index1 

HTTP response:

```xml
<response>
    <entry key="Status">OK</entry>
</response>
```

Using PHP:

```php
$index = new OssApi('http://localhost:9090', 'index1');
$result = $index -> optimize(‘index1’);
```


