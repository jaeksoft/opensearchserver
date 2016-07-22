## Listing site map

This API returns the site map list.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/sitemap```

**Method:** ```GET```

**URL parameters:**
- _**index_name**_ (required): The name of the index.


**Header** (optional returned type):
- Accept: ```application/json```
- Accept: ```application/xml```

### Success response
The map site is returned either in JSON or XML format.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "1 item(s) found",
        "items": [
            "http://www.google.com/sitemap.xml"
        ]
    }


### Error response

The list was not returned. The reason is provided in the content.

**HTTP code:**
500

**Content (text/plain):**

    An internal error occurred


### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/crawler/web/sitemap


**Using jQuery:**

    $.ajax({
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/sitemap  console.log(data);
    });

