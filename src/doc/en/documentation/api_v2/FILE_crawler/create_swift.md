## Create or update a "Swift" repository (location)

Use this API to create a new location to crawl files located on a Swift server.

**Requirement:** OpenSearchServer v1.5

**This API has several differences with the others APIs in the URLs and HTTP methods it uses, please carefully read this doc.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/inject/swift/{index_name}/{result_type}?path={path}&ignoreHiddenFile={ignoreHiddenFile}&includeSubDirectory={includeSubDirectory}&enabled={enabled}&delay={delay}`

**Method:** ```PUT```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).
- _**path**_: path.
- _**ignoreHiddenFile**_: whether hidden files should be ignored or not during crawling (`true` or `false`).
- _**includeSubDirectory**_: whether sub directories should be crawled or not (`true` or `false`).
- _**enabled**_: whether this new location should be enabled in order to be taken into account for crawling (`true` or `false`).
- _**delay**_: delay between each file access in ms (defaults to 0).
- _**username**_: username.
- _**password**_: password.
- _**tenant**_: name of tenant.
- _**container**_: name of container.
- _**authUrl**_: authentication URL.
- _**authType**_: authentication type.

### Success response
New location has been created or updated.

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