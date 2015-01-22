## How to crawl a MongoDB server

OpenSearchServer 1.5.10 embeds a new powerful feature allowing for crawling a MongoDB server.

### Creating the crawl process

Go to tab Crawler / Database, choose `MongoDB` in the select list `Type` and click the button `New crawl...`:

![Creating the crawl process](mongo1.jpg)


This crawler looks a bit like the [Database crawler](http://www.opensearchserver.com/documentation/tutorials/crawling_a_database.md). It has two tabs: 

* first one (`General settings`) is for **configuring access to the MongoDB and some parameters for indexing**,
* second tab (`FieldMap`) is for defining **relations between information retrieved from the MongoDB and fields** of your schema.

### Configuring the crawl process

![Creating the crawl process](mongo2.jpg)

`MongoDB url` must be built with this format: `mongodb://XX.XX.XX.XX:<port>`.

Parameters `Criteria` and `Projection` are the ones **defined by the function `find`** of MongoDB: [http://docs.mongodb.org/manual/reference/method/db.collection.find/](http://docs.mongodb.org/manual/reference/method/db.collection.find/).

For example, you could use `{ _id:1, title:1}` for the parameter `Projection`.

### Indexing data into fields

Tab `FieldMap` use [JSONPath](http://goessner.net/articles/JsonPath/) for **targetting precise properties** in the returned object. 

For example, use `$._id.$oid` and `$.title` to target the object id and the title of the document.

![Creating the crawl process](mongo3.jpg)

As usual, configure **which value should go in which field** of your schema. 

### Starting the crawl

To start the crawl, simply click the button with the green icon in the list of all process:

![Creating the crawl process](mongo4.jpg)

This crawl can also be **started from a job** in the scheduler, using the task `Database crawler - run`.

