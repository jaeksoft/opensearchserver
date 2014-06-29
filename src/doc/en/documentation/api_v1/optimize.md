## Optimize API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/optimize

It optimizes the index.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.

### Example

HTTP Request:

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


