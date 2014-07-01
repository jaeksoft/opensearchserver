## Renderer API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/renderer

This API renders a HTML search box, which you can then readily embed in a website.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**name**_ (required): The name parameter denotes the name of the renderer. This name will be available after creating a renderer item.
- _**query**_ (required): The query parameter denotes the query that needs to be searched in index similar to a parameter.
- _**page**_ (required): The page parameter denotes which page needs to be displayed.

### Example

HTTP request:

    http://localhost:9090/renderer?use=index1&name=default&query=opensearchserver
