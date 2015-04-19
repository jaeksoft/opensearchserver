## Creating or updating a replication

Use this API to create or update a replication.

**Requirement:** OpenSearchServer v1.5.10

### Call parameters

**URL:** ```/services/rest/index/{index_name}/replication```

**Method:** ```PUT```

**Header**:

- _**Content-Type**_ (required): ```application/json```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**replication_name**_ (required): The name of the replication.

**Raw data (PUT):**

Details about the replication sent as JSON. The name will be automatically generated.

    {
      "replicationType":"BACKUP_INDEX",
      "remoteUrl":"http://localhost:8080",
      "remoteIndexName":"geo_repl2",
      "remoteLogin": "admin",
      "remoteApiKey": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
      "secTimeOut":120
    }

### Success response
Replication has been created or updated.

    {
      "successful": true,
      "info": "Item created: http://localhost:8080/geo_repl2"
    }

**HTTP code:**
200
    
### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT http://localhost:8080/services/rest/index/my_index/replication
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/replication
    }).done(function (data) {
       console.log(data);
    });
    
