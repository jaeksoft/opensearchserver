## Create new scheduler

Use this API to create new scheduler.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/scheduler/create```

**Method:** ```PUT```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.


**Raw data (PUT):**
All the information for the Scheduler creation in Json.

    {
      "name": "PrayTheSun",
      "type": "test",
      "cron": {
        "seconds": "13",
        "minutes": "16",
        "hours": "12",
        "month": "5",
        "dayOfMonth": "3",
        "dayOfWeek": "*",
        "year": "2016"
      },
      "active": true,
      "mailRecipients": "git gud",
      "emailNotification": true,
      "tasks": {
        "TaskBuildAutocompletion": {
          "Buffer size": "1000",
          "Item name": "autocomplete",
          "Time out": "14400"
        }
      }
    }



### Success response
The scheduler was created.

**HTTP code:**
200

**Content (application/json):**

     {
       "successful": true,
       "isRunning": false,
       "isActive": false,
       "taskInfos": []
     }


### Error response

The creation failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        http://localhost:8080/services/rest/index/my_index/scheduler/create


**Using jQuery:**

    $.ajax({
       type: "PUT",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/scheduler/create console.log(data);
    });
