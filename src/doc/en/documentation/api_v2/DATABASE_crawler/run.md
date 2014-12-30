## Running a database crawl process

Use this API to run a database crawl process.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/database/{crawl_name}/run```

**Method:** ```PUT```

**Header**:

- _**Content-Type**_ (required): ```application/json```
- Optionnal return type:
  * Accept: ```application/json```
  * Accept: ```application/xml```

**URL parameters:**

- _**index_name**_ (required): The name of the index.

**Body of request:**

Variables can be sent to the database crawl process. Variables must be written as placeholders in the SQL query. For example SQL query could be: `SELECT * FROM articles WERE mod_date < {modDate}`. Variables could then be:

```json
    {
      "mod_date": "20141205"
    }
``` 

### Success response
The crawl has been executed.

**HTTP code:**
200

**Content (application/json):**

    {
      "successful": true,
      "info": "5 document(s) indexed"
    }
    

### Error response

The crawl failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XPUT -H "Content-Type: application/json" \
         http://localhost:8080/services/rest/index/my_index/crawler/database/my_crawl/run
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/crawler/database/my_crawl/run"
    }).done(function (data) {
       console.log(data);
    });
    
