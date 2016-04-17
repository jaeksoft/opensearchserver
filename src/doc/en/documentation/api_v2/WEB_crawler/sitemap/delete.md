## Deleting site map

Use this API to delete site map from the site map list.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/sitemap```

**Method:** ```DELETE```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**site_map_url**_ (required): It's the url or the url's array deleted in database.

**RAW data**: an array of site map

    [
      "http://www.google.com/sitemap.xml",
      "http://www.exemple.com/sitemap.xml"
    ]


### Success response
The patterns have been deleted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "2 patterns deleted"
    }


### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
404, 500

### Sample call

**Using CURL:**

    curl -XDELETE -H "Content-Type: application/json" \
        http://localhost:8080/services/rest/index/my_index/crawler/web/sitemap?site_map_url=http://www.google.com/sitemap.xml


**Using jQuery:**

    $.ajax({
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/sitemap?site_map_url=http://www.google.com/sitemap.xml  console.log(data);
    });
