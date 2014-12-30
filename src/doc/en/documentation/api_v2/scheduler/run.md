## Running a job of scheduler

Use this API to run a job of scheduler.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/scheduler/{scheduler_name}/run```

**Method:** ```PUT```

**Header**:

- _**Content-Type**_ (required): ```application/json```
- Optionnal return type:
  * Accept: ```application/json```
  * Accept: ```application/xml```

**URL parameters:**

- _**index_name**_ (required): The name of the index.

**Body of request:**

Variables can be sent to the job of scheduler, which will in turn give them to the tasks which need them (like the `Database crawler - run` task). Variables must be given as a JSON array.

```json
    {
      "mod_date": "20141205"
    }
``` 

Empty variable can be sent as empty string:

```json
    {
      "mod_date": ""
    }
``` 


### Success response
The job of scheduler has been started. Status is returned.

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

The crawl failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**
Simple call:

    curl -XPUT -H "Content-Type: application/json" \
         http://localhost:8080/services/rest/index/my_index/scheduler/my_job/run
    

**Using jQuery:**

    $.ajax({ 
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/scheduler/my_job/run"
    }).done(function (data) {
       console.log(data);
    });
    
