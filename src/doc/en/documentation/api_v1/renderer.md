## Renderer API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/renderer

This API renders an HTML search box that can be embedded easily in a website easily.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**name**_ (required): The name parameter denotes the name of the renderer . This name will be available after creating an render-er item
- _**query**_ (required): This query parameter denotes the query that need to be searched in index similar to q parameter
- _**page**_ (required): This page parameter denotes that which page needs to be displayed

### Example

HTTP Request:

    http://localhost:9090/renderer?use=index1&name=default&query=opensearchserver