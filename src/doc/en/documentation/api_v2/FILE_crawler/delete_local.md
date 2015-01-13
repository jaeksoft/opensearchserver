## Deleting a local files repository

Use this API to delete a files repository located on the same server as the OSS instance. 

**Requirement:** OpenSearchServer v1.5

**The URLs and HTTP methods used by this API are different from other OSS APIs, please give this document a careful read.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/remove/localfile/{index_name}/{result_type}?path={path}`

**Method:** ```DELETE```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): The type of returned result (`json` or `xml`).
- _**path**_ (required): The path used by this repository.

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

### Sample call

**Using CURL:**
Simple call:

    curl -XDELETE -H \
         http://localhost:8080/services/rest/crawler/file/repository/remove/localfile/my_index/json?path=E:/_temp
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/repository/remove/localfile/my_index/json?path=E:/_temp"
    }).done(function (data) {
       console.log(data);
    });
    
