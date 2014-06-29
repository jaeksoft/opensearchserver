## URL browser API

_**This API is deprecated, have a look at the [new RESTFul API](../api_v2/README.html)**_

    http://{server_name}:9090/urlbrowser

The following commands can be performed with URL browser API:
- **urls**: The URL's in the URL browser can be exported as text file.
- **sitemap**: The URL's in the URL browser can be exported as sitemap (http://www.sitemaps.org/) .
- **deleteAll**: The URL's in the URL browser will be deleted / truncated.


**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**cmd**_: The command to perform: export urls,export sitemap and deleteAll URL's.

### Example

**HTTP request**

Export URL's from URL database;

    http://localhost:9090/urlbrowser?use=indexname&cmd=urls
 
Export URL's as sitemap from URL database:

    http://localhost:9090/urlbrowser?use=indexname&cmd=sitemap
 
Truncate URL's from URL database:

    http://localhost:9090/urlbrowser?use=indexname&cmd=deleteAll

**HTTP response**

It indicates that the URL database has been truncated:

```xml
<response>
  <entry key="Status">OK</entry>
  <entry key="Info">URL database truncated</entry>
</response>
```
