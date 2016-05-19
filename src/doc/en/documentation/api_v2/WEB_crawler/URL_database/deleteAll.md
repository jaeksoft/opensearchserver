## Delete all url in Url Browser

Use this API to delete all URLs in the URL database.


**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/urls```

**Method:** ```DELETE```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.



### Success response

**HTTP code:**
200

**Content (application/json):**

```json
{
  "successful": true,
  "info": "delete all"
}
```

### Error response

The delete is failed. The reason why is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XDELETE -H "Content-Type: application/json" \
        -d '["http://www.open-search-server.com/"]' \
        http://localhost:8080/services/rest/index/my_index/crawler/web/urls


**Using jQuery:**

    $.ajax({
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/web/urls console.log(data);
    });

