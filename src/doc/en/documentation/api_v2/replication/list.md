## Listing replications

Use this API to retrieve a list of existing replications.

**Requirement:** OpenSearchServer v1.5.10

### Call parameters

**URL:** ```/services/rest/index/{index_name}/replication```

**Method:** ```GET```

**URL parameters:**

- _**index_name**_ (required): The name of the index.

### Success response
A list of replications.

**HTTP code:**
200

**Content (application/json):**

	{
	  "successful":true,
	  "info":"2 item(s) found",
	  "items": 
	  [ 
		{
		"name":"http://localhost:8080/geo_repl",
		"replicationType":"BACKUP_INDEX",
		"remoteUrl":"http://localhost:8080",
		"remoteIndexName":"geo_repl",
		"secTimeOut":120,
		"isActiveThread":false
		}, 
		{
		"name":"http://localhost:8080/geo_repl2",
		"replicationType":"BACKUP_INDEX",
		"remoteUrl":"http://localhost:8080",
		"remoteIndexName":"geo_repl2",
		"secTimeOut":120,
		"isActiveThread":false
		}
	  ]
	}


    
### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/replication
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/replication
    }).done(function (data) {
       console.log(data);
    });
    
