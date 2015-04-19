## Create or update a Swift repository (location)

Use this API to create a new location to crawl files located on a Swift server.

**Requirement:** OpenSearchServer v1.5

**The URLs and HTTP methods used by this API are different from other OSS APIs, please give this document a careful read.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/inject/swift/{index_name}/{result_type}?path={path}&ignoreHiddenFile={ignoreHiddenFile}&includeSubDirectory={includeSubDirectory}&enabled={enabled}&delay={delay}`

**Method:** ```PUT```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): The type of returned result (`json` or `xml`).
- _**path**_: The path.
- _**ignoreHiddenFile**_: Whether hidden files should be ignored during crawling (`true` or `false`).
- _**includeSubDirectory**_: Whether sub-directories should be crawled (`true` or `false`).
- _**enabled**_: Whether this new location should be eligible for crawling (`true` or `false`).
- _**delay**_: delay between each file access in ms (defaults to 0).
- _**username**_: username.
- _**password**_: password.
- _**tenant**_: name of tenant.
- _**container**_: name of container.
- _**authUrl**_: authentication URL.
- _**authType**_: authentication type.

### Success response
The new location has been created or updated.

**HTTP code:**
200

**Content (json):**

    {
      "successful":true,
      "info":"Inserted"
    }

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500
