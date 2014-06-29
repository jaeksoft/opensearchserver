## Database API

_**This API is deprecated, have a look at the [new RESTFul API](../api_v2/README.html)**_

    http://{server_name}:9090/database

This API performs execute a database crawl.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**name**_ (required): The name of the database crawl item which will be executed

### Example

Execute a database crawl

    http://localhost:9090/database?use=oss&name=dbname
 
The same with authentication parameters

    http://localhost:9090/database?use=oss&name=dbname&login=admin&key=6020b80a823e276727a0d6a23167d52b