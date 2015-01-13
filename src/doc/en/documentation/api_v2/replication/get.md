## Getting information about a replication

Use this API to retrieve details about a replication.

**Requirement:** OpenSearchServer v1.5.10

### Call parameters

**URL:** ```/services/rest/index/{index_name}/replication?name={replication_name}```

**Method:** ```GET```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**replication_name**_ (required): The name of the replication.

### Success response
Information about the replication.

**HTTP code:**
200

**Content (application/json):**

	{  
	   "successful":true,
	   "name":"http://localhost:8080/articles_backup",
	   "replicationType":"BACKUP_INDEX",
	   "remoteUrl":"http://localhost:8080",
	   "remoteIndexName":"articles_backup",
	   "secTimeOut":120,
	   "isActiveThread":false,
	   "lastThread":{  
		  "info":"100% completed - 0 file(s) sent - 0 bytes sent",
		  "durationMs":2324,
		  "startDate":"2015-01-06T13:36:23.912+0000"
	   }
	}
 
### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/replication?name=http://localhost:8080/articles_backup
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/replication?name=http://localhost:8080/articles_backup
    }).done(function (data) {
       console.log(data);
    });
