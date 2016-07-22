## Inserting site map

Use this API to insert site map in the site map list.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/sitemap```

**Method:** ```PUT```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**site_map_url**_ (required): It's the url or the url's array injected in database.

**Raw data (PUT):**
An array of site map.

    [
      "http://http://www.google.com/sitemap.xml",
      "https://http://www.exemple.com/sitemap.xml"
    ]


### Success response
The patterns has been inserted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "2 patterns injected"
    }


### Error response

The insertion failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        http://localhost:8080/services/rest/index/my_index/crawler/web/sitemap?site_map_url=http://www.google.com/sitemap.xml


**Using jQuery:**

    $.ajax({
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/sitemap?site_map_url=http://www.google.com/sitemap.xml  console.log(data);
    });
