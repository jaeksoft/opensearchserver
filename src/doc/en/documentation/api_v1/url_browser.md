## URL browser API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/urlbrowser

The following commands can be performed with the URL browser API:
- **urls**: The URLs in the URL browser can be exported as a text file.
- **sitemap**: The URLs in the URL browser can be exported as a sitemap (see http://www.sitemaps.org/ for more).
- **deleteAll**: The URLs in the URL browser will be deleted / truncated.


**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**cmd**_: The command to perform: export urls, export sitemap or deleteAll URLs.

### Examples

**HTTP request**

Export URLs from the URL database:

    http://localhost:9090/urlbrowser?use=indexname&cmd=urls
 
Export URLs as a sitemap from the URL database:

    http://localhost:9090/urlbrowser?use=indexname&cmd=sitemap
 
Truncate URLs from the URL database:

    http://localhost:9090/urlbrowser?use=indexname&cmd=deleteAll

**HTTP response**

The following indicates that the URL database has been truncated:

```xml
<response>
  <entry key="Status">OK</entry>
  <entry key="Info">URL database truncated</entry>
</response>
```
