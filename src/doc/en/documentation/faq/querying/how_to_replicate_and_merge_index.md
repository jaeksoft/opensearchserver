## How to replicate and merge index

OpenSearchServer can easily replicate one index to another location, locally or on a remote instance of OpenSearchServer. Several index can also be merged into one index. This article will explain all this.

### Replicating index

Different types of replication are available. Most of the time the replication feature will be used to fully copy an index, for example to serve as a backup. 

One other **common best practice** is to dedicate an index for data feeding (using API or Crawlers), and **one replicated index for querying**.

To do so:

* **Start by creating the target index**. It should be an "Empty index". For instance create index "articles_query", which will be a replicated index from "articles" that will be used for query purpose only.
  * This target index can be created on the same instance than the one used by the main index, or **it could be created on a remote instance of OpenSearchServer**, as long as this instance can be reached through HTTP by the first one. 
* Go back to the main index and go to tab ` Replication`. Create a "Target" as shown below:
![Creating a replication target](replication1.png)
  * Take care to choose "Backup" for `Replication type`.
* Click button "Create". This new replication target is added to the list displayed below. To start the replication simply click on the button with the green icon.
![Starting a replication target](replication2.png)
  * **Take care**: crawlers should be stopped before starting replication, otherwise targeted index may be corrupted after replicating.

#### Scheduling replication

Replication can be scheduled with a job in the Scheduler (tab `Scheduler`). For instance create a task as shown below:

![Scheduling replication](replication3.png)

This task first stops the web crawler, then proceed to the replication and at last starts again the web crawler.

When working with the `Replication - run` task, to find the name of the replication, simply press the "Down arrow" key on your keyboard or click the button on the right of the field, it will display an autocomplete list of available replications.

![Scheduling replication](replication4.png)

Always leave the field `Only if the index has been updated` to `false`. Replication is incremental anyway.

### Merging index

Several local index can be merged into one index. This can be very useful when several index use different types of crawlers for example. You may want to dedicate one index to database crawling, and one other index to web crawling. Or you may want to distribute crawling of several websites through several index.
However when data is split through several index it can not be searched in a consolidated way. This is where the merging feature comes in handy.

By merging several index into one **you will be able to search through all your data**.

The different index must share some common fields. It is a best practice when working with merging feature to use the same schema for all the index, when possible.

#### Creating the merging job

To merge an index into another one, simply create a job of scheduler in the final index and use a task `Merge index`.

For instance, for merging index `articles_db` and `articles_web` into a third index named `articles_query` you could use these tasks:

![Merging index](merging1.png)

#### Controlling the status for the web crawler

However, when merging index we should ensure that the merged index are not currently changing their data. It is then a **best practice to stop the crawlers** (Web and File crawler) from these index. 

Doing this is easy using the scheduler since **it can start jobs into other index**.

The above job could then become:
 
![Merging index](merging2.png)

Here the job of scheduler from the index `articles_query` drives some other jobs from the index `articles_web` (those jobs would use some `Web crawler - stop` and `Web crawler - start` tasks).

The same process could be applied to an index that would be using a File crawler.

#### Handling the Database crawler

The Database crawler can not be stopped or started the same way. We could however decide to fully handle the Database crawling process from the job of scheduler in `articles_query` too.

Job could be:

![Merging index](merging3.png)

Here we added as a first task another `Other scheduler` task whose role is to start a full database crawl in the `articles_db` index. The `startfullCrawl` job would use a `Database crawler - run` task.

#### Deleting content before merging

Merging is a low-level process that do not take care of unique keys used for identifying documents. Thus, merging several times the same index could lead to duplicated content.

Since the final merged index is often used for query only and no content is added to it in another way than merging, we could simply delete all the content from it before merging. This is a common practice.

Here is the final job:

![Merging index](merging4.png)

Steps are:

1. A full databse crawling is started on `articles_db`
2. Web crawler is stopped on `articles_web`
3. All content is deleted from `articles_query`
4. Content is merged from `articles_db` to `articles_query`
5. Content is merged from `articles_web` to `articles_query`
6. Web crawler is started on `articles_web`

#### Going further

When executing the above process, there is a small amount of time where index will be empty. 

To avoid this, you could **think of using the `articles_query` as an intermediate index**, and add a final replication to another index at the end of the merging job. For example, replicating a full backup to `articles_final_query` after the final `start_crawl` task would allow for **smooth merging with no side effects to users**.