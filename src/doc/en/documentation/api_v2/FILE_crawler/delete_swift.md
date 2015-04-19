## Deleting a Swift repository

Use this API to delete a repository stored on a Swift system.

**Requirement:** OpenSearchServer v1.5

**The URLs and HTTP methods used by this API are different from other OSS APIs, please give this document a careful read.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/remove/swift/{index_name}/{result_type}?path={path}&username={username}&container={container}`

**Method:** ```DELETE```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).
- _**path**_ (required): path used by this repository.
- _**username**_ (required): username used by this repository.
- _**container**_ (required): container used by this repository.


### Success response
The repository has been deleted.

**HTTP code:**
200

**Content (json):**

    {
      "successful":true
    }

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500
