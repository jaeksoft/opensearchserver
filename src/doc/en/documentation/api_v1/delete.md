## Delete API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/delete

This API delete's documents from the index by specifying a query.

The deleted documents cannot be recovered, so make sure you have created a backup before performing the delete operation.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**q**_: The query used to identify which documents will be deleted
- _**uniq**_: This parameter is used for deleting the documents by primary key

### Example

Delete all the documents from OpenSearchServer index:

    http://localhost:9090/delete?use=index1&q=*:* 
 
Deleting document matching the given primary key:

    http://localhost:9090/delete?use=index1&uniq=2
 
Delete documents associated to specific host from index:

    http://localhost:9090/delete?use=index1&q=host:www.open-search-server.com
 
Delete a specific url from index:

    http://localhost:9090/delete?use=index1&q=url:"http://www.open-search-server.com/services"

PHP:
 
```php
$delete = new OssDelete('http://localhost:9090', 'index1');
$result = $delete->delete('host:www.open-search-server.com');
```

### HTTP response

This indicates that 14 documents has been deleted from the index:

```xml
<response>
    <entry key="Status">OK</entry>
    <entry key="Deleted">14</entry>
</response>
```

