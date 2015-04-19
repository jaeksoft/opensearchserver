## Getting the status of a task in the scheduler

Use this API to retrieve the last status of a task in the scheduler.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/scheduler/{scheduler_name}/run```

**Method:** ```GET```

**Header** (optional returned type):

- Accept: ```application/json```
- Accept: ```application/xml```

**URL parameters:**

- _**index_name**_ (required): The name of the index.
- _**scheduler_name**_ (required): The name of the scheduler.

### Success response
Information about this task.

**HTTP code:**
200

**Content (application/json):**

    {  
      "successful":true,
      "isRunning":false,
      "isActive":true,
      "lastExecutionDate":"2014-12-29T23:00:00+0000",
      "taskInfos":[  
         {  
            "name":"Query check",
            "startDate":"2014-12-29T23:00:00+0000",
            "endDate":"2014-12-29T23:00:00+0000",
            "duration":0,
            "infos":"JSON Path succeed: 637"
         },
         {  
            "name":"Query check",
            "startDate":"2014-12-28T23:00:00+0000",
            "endDate":"2014-12-28T23:00:00+0000",
            "duration":0,
            "infos":"JSON Path succeed: 691"
         }
      ]
    }
    

### Error response

The request failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/scheduler/my_job/run
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/scheduler/my_job/run
    }).done(function (data) {
       console.log(data);
    });
    
