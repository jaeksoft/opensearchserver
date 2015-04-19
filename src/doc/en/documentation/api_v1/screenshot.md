## Screenshot API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/screenshot

The screenshot API can capture screenshots, get screenshots, or check whether a screenshot exists.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**url**_ (required): The URL parameter denotes the URL to be captured.
- _**action**_ (required): The action parameter is an ENUM field with the following options:
  - capture: Captures a screenshot of the given URL.
  - image: Gets an already-existing image of the indicated URL.
  - check: Checks whether the URL was already captured.

### Examples

HTTP Request:

    http://localhost:9090/screenshot?use=index1&action=capture&url=http://www.open-search-server.com
    http://localhost:9090/screenshot?use=index1&action=check&url=http://www.open-search-server.com
    
HTTP response:

```xml
<response>
    <entry key="Check">bfbe8b2187bdfcc8a1136486c68247e4b2.png</entry>
</response>
```
