## Create or update a "local files" repository (location)

Use this API to create a new location to crawl files located on the same server than the OpenSearchServer instance.

**Requirement:** OpenSearchServer v1.5

**This API has several differences with the others APIs in the URLs and HTTP methods it uses, please carefully read this doc.**

### Call parameters

**URL:** `/services/rest/crawler/file/repository/inject/localfile/{index_name}/{result_type}?path={path}&ignoreHiddenFile={ignoreHiddenFile}&includeSubDirectory={includeSubDirectory}&enabled={enabled}&delay={delay}`

**Method:** ```PUT```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**result_type**_ (required): Type of returned result (`json` or `xml`).
- _**path**_: path to root folder where crawler must start. 
- _**ignoreHiddenFile**_: whether hidden files should be ignored or not during crawling (`true` or `false`).
- _**includeSubDirectory**_: whether sub directories should be crawled or not (`true` or `false`).
- _**enabled**_: whether this new location should be enabled in order to be taken into account for crawling (`true` or `false`).
- _**delay**_: delay between each file access in ms (defaults to 0).

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

### Sample call

**Using CURL:**
Simple call:

    curl -XPUT -H \
         http://localhost:8080/services/rest/crawler/file/repository/inject/localfile/my_index/json?path=E:/_temp&ignoreHiddenFile=true&includeSubDirectory=false&enabled=true&delay=2
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/crawler/file/status/my_index/json"
    }).done(function (data) {
       console.log(data);
    });
    
