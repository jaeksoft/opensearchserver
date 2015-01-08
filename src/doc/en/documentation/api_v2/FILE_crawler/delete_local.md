## Deleting a repository of type "Local files"

Use this API to delete a repository of type "Local files".

**Requirement:** OpenSearchServer v1.5

**This API has several differences with the others APIs in the URLs and HTTP methods it uses, please carefully read this doc.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/remove/localfile/{index_name}/{result_type}?path={path}`

**Method:** ```DELETE```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).
- _**path**_ (required): path used by this repository.

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

### Sample call

**Using CURL:**
Simple call:

    curl -XGET -H \
         http://localhost:8080/services/rest/crawler/file/repository/remove/localfile/my_index/json?path=E:/_temp
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/repository/remove/localfile/my_index/json?path=E:/_temp"
    }).done(function (data) {
       console.log(data);
    });
    
