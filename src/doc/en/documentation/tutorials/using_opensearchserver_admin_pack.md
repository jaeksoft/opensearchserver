## Using OpenSearchServer Admin Pack

Admin Pack is a great service that will allow for easy and reliable monitoring of your OpenSearchServer instances.

### What is OpenSearchServer Admin Pack

Admin Pack is a new service from OpenSearchServer that you can use to easily monitor your OpenSearchServer instances.

**OpenSearchServer version 1.5.10 at least is needed** to be able to use this service.

**Features are:**

1. Getting an **email status notification** when **status of your instance changes**. Status are:
    * `No data`: if no data has been received from your instance in the last 5 minutes.
    * `OK`: if previous status was `No data` and new data has been received in the last 5 minutes.
2. Getting an **email threshold notification** as soon as **free memory** or **free disk space** drop **under a configurable threshold**.

_More features will come soon in version 2 of Admin Pack._

Several Admin Pack can be subscribed. Each Admin Pack allow for using with one OpenSearchServer instance.

### Subscribing to an Admin Pack

_- TODO, with screenshot -_

### Configuring an Admin Pack

When logged in to your dashboard on [http://www.opensearchserver.com](http://www.opensearchserver.com) go to tab **Monitoring**.

If you just subscribed to an Admin Pack you will see only one line here. Click on button **Manage**.

_- Add screenshot here -_

On this page you will be able to:

* get the `UUID` that you will need for configuring your OpenSearchServer instance.
* give a `name` to this monitoring plan. This name will be used in the notification emails sent.
* choose the email that will receive notifications.
* choose a `Timezone`. This timezone will be used for every date information related to the notifications.
* get the last `Status` for this monitoring plan.
* get the last time the status changed.

Give a name to this Admin Pack, configure an email address and a timezone, and copy the `UUID`.

_- Add screenshot here -_

### Configuring an OpenSearchServer instance

The OpenSearchServer instance you want to monitor must now be configured to regularly send data to the monitoring service of OpenSearchServer. This is a quick and easy process.

1. Choose one existing index. 
    * _You may want to create a dedicated empty index for this process. It will only use one job of scheduler._
2. Go to tab Scheduler, click on button "Create new scheduler job":
	* Give a name to the job, for example "**Monitoring**".
	* Check **checkbox `Enabled`**.
	* In CRON expression, write `0/4` in field `Minutes` and leave the other fields with default values (full expression is: `0 0/10 * * * ? *`
	* Add one task: `Monitoring upload`
		* Leave default value for field `URL`: `https://cloud.opensearchserver.com/oss-monitor/`.
		* Leave fields `Login` and `Password` empty.  
		* Paste the previously copied `UUID` in field `Instance ID`
	* Click button "**Create**".

![Creating job](oss_job_monitor.png)

That's it! This job will now run every 4 minutes. It will post some monitoring information to your Admin Pack. You will receive immediate email notification as soon as status changes.  

Information received by the monitoring service can be seen by clicking on button "**History**" in the "**Monitoring**" tab of your dashboard on http://www.opensearchserver.com.