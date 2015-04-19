## Deleting a replication

Use this API to delete a replication.

**Requirement:** OpenSearchServer v1.5.10

### Call parameters

**URL:** ```/services/rest/index/{index_name}/replication?name={replication_name}```

**Method:** ```DELETE```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**replication_name**_ (required): The name of the replication.

### Success response
The replication has been deleted.

    {
      "successful": true,
      "info": "Item http://localhost:8080/articles_backup deleted."
    }

**HTTP code:**
200
    
### Error response

The deletion failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XDELETE http://localhost:8080/services/rest/index/my_index/replication?name=http://localhost:8080/articles_backup
    

**Using jQuery:**

    $.ajax({ 
       type: "DELETE",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/replication?name=http://localhost:8080/articles_backup
    }).done(function (data) {
       console.log(data);
    });
    
