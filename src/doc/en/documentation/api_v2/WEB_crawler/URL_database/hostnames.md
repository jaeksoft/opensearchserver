## Get the hostname list

Use this API to get the list of hostnames present in the URL database.

The number of known URL per hostname are also returned.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/hostnames```

**Method:** ```GET```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**min_count**_ (optional): A filter to get only the hostname who have equals or more URL than this parameter.


### Success response

**HTTP code:**
200

**Content (application/json):**

```json
{
  "successful": true,
  "hostnames": {
    "fr.news.yahoo.com": 2231,
    "www.lemonde.fr": 1,
    "www.opensearchserver.com": 310
  }
}
```

### Error response

The insertion failed. The reason why is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '["http://www.open-search-server.com/"]' \
        http://localhost:8080/services/rest/index/my_index/crawler/web/hostnames?min_count=10
    
