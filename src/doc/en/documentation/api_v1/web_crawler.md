## WEB Crawler API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/webcrawler

The web crawler API can start or stop the web crawler.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**action**_: The action to perform: start, stop, and know status of the index.
- _**runOnce**_: The runOnce parameter is to specify whether the crawler should run only once, or forever. This is a Boolean parameter.

### Example

**HTTP request**

    http://localhost:9090/webcrawler?use=index1&action=start
    http://localhost:9090/webcrawler?use=index1&action=stop 

**HTTP response**

This indicates that 14 documents has been deleted from the index:

```xml
<response>
    <entry key="info">STARTED</entry>
    <entry key="status">OK</entry>
</response>
```
