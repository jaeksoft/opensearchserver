## Schema API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/schema

The Schema API performs actions such as:
* **createindex**: Creates a index.
* **deleteindex**: Deletes a index.
* **indexlist**: Lists all available indexes.
* **setfield**: Sets a schema field for an index.
* **deletefield**: Deletes a schema field in an index.

**Global parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**cmd**_ (required): The command to perform - create index, delete index, list index, delete field, set field.

**Parameters for index creation:**
- _**index.name**_ (required): The name of the index to be created.
- _**index.template**_ (required): The crawler template that will be used to create the OpenSearchServer instance. OpenSearchServer has three templates:
  - EMPTY_INDEX: This is an empty index. If you use this setting you will have to set a schema manually.
  - WEB_CRAWLER: This is an index with predefined fields, analysers and parsers. This template is suited to web crawling and indexation.
  - FILE_CRAWLER: This is an index with predefined fields, analysers and parsers. This template is suited to parsing and indexing files in file systems (.doc, .pdf, etc.).

**Parameters for index deletion:**
- _**index.name**_ (required): The name of the index to be deleted.

**Parameters for field creation/update:**
- _**field.default**_ (optional): This denotes whether the given field is set to default. The field.default parameter can be "YES" or "NO".
- _**field.unique**_ (optional): This denotes whether the field is set to unique. The field.unique parameter can be "YES" or "NO".
- _**field.name**_ (required): The name of the field to be created or set as a default field or an unique field.
- _**field.analyzer**_ (optional): The name of the analyzer.
- _**field.stored**_ (optional): This indicates whether the field needs to be stored. It has two options, "YES" and "NO".
- _**field.indexed**_ (optional): This indicates whether the field needs to be indexed. It has two options, "YES" and "NO".
- _**field.termVector**_ (optional): This indicates whether the term vector needs to be saved. It has three options - "YES", "NO" and POSITIONS_OFFSETS.

**Parameters for field deletion:**
- _**field.name**_ (required): The name of the field to be deleted.

### Examples

**HTTP request:**

Creating an index:

    http://localhost:9090/schema?cmd=createindex&index.name=index1&index.template=WEB_CRAWLER
 
Listing all available indexes:

    http://localhost:9090/schema?cmd=indexlist
 
Deleting an index:

    http://localhost:9090/schema?cmd=deleteindex&index.name=index1&index.delete.name=index1
 
Setting/creating a schema field:

    http://localhost:9090/schema?cmd=setField&field.name=titleNew&field.analyzer=StandardAnalyzer&use=index1&field.stored=YES&field.indexed=YES&term.termVector=NO
    
**HTTP response:**

Response for deleting an index:

```xml
<response>
    <entry key="Info">Index deleted: index</entry>
    <entry key="Status">OK</entry>
</response>
```

Response for creating a new schema field:

```xml
<response>
    <entry key="Info">field 'titleNew' added/updated</entry>
    <entry key="Status">OK</entry>
</response>
```

**Using PHP:**

```php
$ossAPI = new OssApi('http://localhost:9090');
$ossAPI->createIndex('index1');
  
$ossAPI = new OssApi('http://localhost:9090',index1);
$ossAPI->setField('id','','NO','YES','YES','','NO','YES');
```
