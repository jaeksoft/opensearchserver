## Index API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/update

API Update is the interface to insert/update documents into an index of the OpenSearchServer search engine.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.


### Posting a XML file

One easy way to populate data is to upload an XML file using a post or a put http request. Then you have to post the file using a post or a put http request.
In our example we use CURL:

    curl -o log.out -H "Content-type: text/xml; charset=utf-8" -T documents.xml "http://localhost:9090/update?use=indexName"
    
The XML format is described below

```xml
<?xml version="1.0" encoding="UTF-8"?>
<index>
  <document lang="en">
    <field name="id"><value>1</value></field>
    <field name="title"><value>Open Search Server</value></field>
    <field name="url"><value>http://www.open-search-server.com</value></field>
    <field name="user">
      <value>emmanuel_keller</value>
      <value>philcube</value>
    </field>
  </document>
  <document lang="en">
    <field name="id"><value>2</value></field>
    <field name="title"><value>SaaS services | OpenSearchServer</value></field>
    <field name="url"><value>http://www.open-search-server.com/services/saas_services</value></field>
    <field name="user">
      <value>emmanuel_keller</value>
    </field>
  </document>
</index>
```

### Using PHP

The request is done using the PHP5 method OssApi::update (see the examples above).
This class can be downloaded along with OpenSearchServer source code, and found here: oss_api.class.php.


#### Example

The following creates an instance of the OSS_IndexDocument class. This object can carry one or more documents to index 
Stopping the FileCrawler instance

```php
$index = new OssIndexDocument();
```

The following adds a document

```php
$document = $index->newDocument('en');
```

The following adds a field within the document:

```php
$document->newField('id', '1234');
$document->newField('title', 'Open Search Server');
$document->newField('content', 'Open Source Search Engine');
$document->newField('meta', 'Open Source');
$document->newField('meta', 'Search Engine'); // Multi value field
```

The following inserts the document(s) within the index.

```php
$server = new OssApi('http://localhost:9090', 'indexName');
$server->update($index);
```