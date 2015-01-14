## How to monitor an OpenSearchServer instance

### Using API calls

You can call a dedicated API to get several monitoring information about the instance.

Simply make a `GET` request on `http://<host>:<port>/services/rest/monitor/json?full=true`.

You will get lots of useful data, such as free disk space, memory use, etc.:

![Monitoring](oss_api_monitor_result.png)

If you want to get only basic information do not send the `?full=true` parameter.
If you want to get results in XML instead of JSON use `http://<host>:<port>/services/rest/monitor/xml?full=true`.

### Using OpenSearchServer to regularly post data to an URL

_**This requires OpenSearchServer > 1.5.10**_

Using the _Scheduler_ you can quickly and easily set up a job that will regularly post monitoring information to a particular URL.

To do so:

* Go to tab Scheduler
* Click "Create new scheduler job"
	* Give a name to the job
	* Check **checkbox `Enabled`**
	* Configure a CRON expression, following those guidelines: [http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger](http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger)
	* Add one task: `Monitoring upload`
		* **This task perfoms a `POST` request to the given URL.** 
		* HTTP authentication can be used with parameters `Login` and `Password`. 
		* Body of the request will be a **text string with all monitoring information concatenated**. 
		* An additionnal information, called `Instance ID`, can be added to the body of the request. This can be useful for example to identify the instance making the request.
		* Here is an example (truncated) of the body of a request:
 
```  
instanceId=productionInstance&version=OpenSearchServer+v1.5.10-SNAPSHOT+-+build+98d156cdbb&availableProcessors=4&freeMemory=424985328&freeMemoryRate=22.61705313410078&maxMemory=1879048192&totalMemory=698875904&indexCount=61&freeDiskSpace=30216572928&freeDiskRate=28.81705882750464&dataDirectoryPath=E%3A%5CCopensearchserver%5Cdata&property_java_runtime_name=Java%28TM%29+SE+Runtime+Environment...
```

Example of a job that will make a `POST` request every 10 minutes:

![Creating job](oss_monitor_create_job.png)