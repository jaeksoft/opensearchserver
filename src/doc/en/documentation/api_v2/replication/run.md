## Running a replication

Use this API to run a replication.

**Requirement:** OpenSearchServer v1.5.10

### Call parameters

**URL:** ```/services/rest/index/{index_name}/replication/run?name={replication_name}```

**Method:** ```PUT```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**replication_name**_ (required): The name of the replication.

### Success response
The replication has been started.

    {
      "successful": true,
      "info": "Item started: http://localhost:8080/articles_backup"
    }

**HTTP code:**
200
  
### Error response

The replication could not be started. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XPUT -H "Content-Type: application/json" \
         http://localhost:8080/services/rest/index/my_index/replication/run()?name=http://localhost:8080/articles_backup
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/replication/run()?name=http://localhost:8080/articles_backup"
    }).done(function (data) {
       console.log(data);
    });
    
