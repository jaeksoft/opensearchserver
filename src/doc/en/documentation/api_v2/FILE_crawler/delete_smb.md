## Deleting a repository of type "SMB/CIFS"

Use this API to delete a repository of type "SMB/CIFS".

**Requirement:** OpenSearchServer v1.5

**This API has several differences with the others APIs in the URLs and HTTP methods it uses, please carefully read this doc.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/remove/smb/{index_name}/{result_type}?path={path}&username={username}&domain={domain}&host={host}`

**Method:** ```DELETE```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).
- _**path**_ (required): path used by this repository.
- _**username**_ (required): username used by this repository.
- _**domain**_ (required): domain used by this repository.
- _**host**_ (required): host used by this repository.


### Success response
Repository has been deleted.

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