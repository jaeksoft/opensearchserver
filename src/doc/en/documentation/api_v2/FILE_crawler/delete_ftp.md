## Deleting a repository of type "FTP"

Use this API to delete a repository of type "FTP".

**Requirement:** OpenSearchServer v1.5

**This API has several differences with the others APIs in the URLs and HTTP methods it uses, please carefully read this doc.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/remove/ftp/{index_name}/{result_type}?path={path}&username={username}&host={host}&ssl={ssl}`

**Method:** ```DELETE```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).
- _**path**_ (required): path used by this repository.
- _**username**_ (required): username used by this repository.
- _**host**_ (required): host used by this repository.
- _**ssl**_ (required): whether the repository to delete is configured for using SSL or not (`true` or `false`)


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

    curl -XDELETE -H \
         http://localhost:8080/services/rest/crawler/file/repository/remove/ftp/my_index/json?path=/test&username=ftpuser&host=mysite.com&ssl=true
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/repository/remove/ftp/my_index/json?path=/test&username=ftpuser&host=mysite.com&ssl=true"
    }).done(function (data) {
       console.log(data);
    });
    