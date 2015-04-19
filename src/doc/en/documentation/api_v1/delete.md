## Delete API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/delete

This API deletes documents from the index by specifying a query.

The deleted documents cannot be recovered. Please make sure that you have a backup before performing the deletion.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**q**_: The query used to identify which documents will be deleted.
- _**uniq**_: This parameter is used for deleting the documents by primary key.

### Examples

Delete all the documents from an OpenSearchServer index:

    http://localhost:9090/delete?use=index1&q=*:* 
 
Delete documents matching a specific primary key:

    http://localhost:9090/delete?use=index1&uniq=2
 
Delete documents associated with a specific host from the index:

    http://localhost:9090/delete?use=index1&q=host:www.open-search-server.com
 
Delete a specific url from the index:

    http://localhost:9090/delete?use=index1&q=url:"http://www.open-search-server.com/services"

PHP:
 
```php
$delete = new OssDelete('http://localhost:9090', 'index1');
$result = $delete->delete('host:www.open-search-server.com');
```

### HTTP response

The following indicates that 14 documents have been deleted from the index:

```xml
<response>
    <entry key="Status">OK</entry>
    <entry key="Deleted">14</entry>
</response>
```

