## Screenshot API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/screenshot

The screenshot API performs actions like getting the screenshot,capturing the screenshot or checking the screenshot if present.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**url**_ (required): The url Parameter denotes the URL that need to be captured.
- _**action**_ (required): The action parameter is an ENUM field having the following options:
  - capture: It captures the screenshot of the given URL.
  - image: It get the image of the given URL.If the URL was already captured.
  - check: It checks the URL is already captured or not.

### Example

HTTP Request:

    http://localhost:9090/screenshot?use=index1&action=capture&url=http://www.open-search-server.com
    http://localhost:9090/screenshot?use=index1&action=check&url=http://www.open-search-server.com
    
HTTP response:

```xml
<response>
    <entry key="Check">bfbe8b2187bdfcc8a1136486c68247e4b2.png</entry>
</response>
```
