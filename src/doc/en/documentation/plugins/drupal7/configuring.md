## Configuring the OpenSearchServer module for Drupal 7

Further details about configuring Search API will be found on [Search API's documentation pages](https://www.drupal.org/node/1251246).

Search API uses to main entities: **server** and **index**. We will configure a new **server** that will be our running OpenSearchServer instance, and a new **index** that will use this server.

### Creating a server

Go to page _/admin/config/search/search\_api_ and click "Add server". 

![Adding a server](drupal_addserver.png)

Fill in the form:

* Server name: **OpenSearchServer**
* Enabled: **checked**
* Service class: choose **OpenSearchServer service**
  * OpenSearchServer host: _URL for your OpenSearchServer instance_. Example: **localhost**.
  * OpenSearchServer port: _port to use_. Example: **9090**.
  * OpenSearchServer path: _path that identifies the OpenSearchServer instance to use on the server_. Example: **/**.
  * OpenSearchServer Login: _login of a user with read and write access_. Example: **admin**.
  * OpenSearchServer Password: _API key for this login_.

Click "Create server".

![Filling in the form](drupal_addserver2.png)
  
### Adding an index

Go to page _/admin/config/search/search\_api_ and click "Add index". 

Fill in the form:

* Index name: **OpenSearchServer Index**
* Item type: **Node**
* Enabled: **checked**
* Server: **OpenSearchServer**
* Index items immediately: you may want to check this checbkox for development purposes if your website contains few documents. It will simplify testing.
* Cron batch size: leave default value **50** for now. It can be changed later.

![Adding an index](drupal_addindex.png)
  
Click "Create index".

---

If error _Fatal error: Class 'OssApi' not found in E:\Mes documents\Boulot\Workspace\drupal-7.33_2\sites\all\modules\drupalplugin_oss\service.inc on line 242_ is shown: please check that you followed steps from the section **Installing OpenSearchServer's PHP library** in [the Installing page](installing.md).

---

#### Configuring fields

Next page allows for choosing what fields should be indexed.

In "Add Related Fields" at the bottom of the page, choose "The main body text" and click "Add fields".

![Add related fields](drupal_addrelatedfields.png)

You could start by choosing those fields, they can be changed later:

![Choose fields to index](drupal_indexfields.png)
  

Click "Save changes".

On next page do not check any _Data Alteration_ or any _Processor_ for now and click "Save configuration".

#### Enabling facets

Go to the newly created index main page (_/admin/config/search/search\_api/index/opensearchserver\_index_) and click on tab "Facets".

As a demonstration purpose, check "Content type" and "Author" and click "Save configuration".

---

Let's now see [how search pages can be created](searching.md).