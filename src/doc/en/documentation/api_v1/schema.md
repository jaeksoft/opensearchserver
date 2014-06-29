## Schema API

_**This API is deprecated, have a look at the [new RESTFul API](/api_v2/README.html)**_

    http://{server_name}:9090/schema

The Schema API performs actions such as:
* **createindex**: Create a index
* **deleteindex**: Delete a index
* **indexlist**: List all the available index
* **setfield**: set a schema field for a index.
* **deletefield**: delete a schema field for a index.

**Global parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**cmd**_ (required): The cmd to perform create index, delete index,list index,delete field,set field.

**Parameters for index creation:**
- _**index.name**_ (required): The name of the index to be created.
- _**index.template**_ (required): The crawler template for creating OpenSearchServer instance. OpenSearchServer has three template:
  - EMPTY_INDEX: This is an empty index. If you use this setting you will have to set a schema manually.
  - WEB_CRAWLER: This is an index with predefined fields, analysers and parsers. This template is suited to web crawling and indexation.
  - FILE_CRAWLER: This is an index with predefined fields, analysers and parsers. This template is suited to parsing and indexing files on file systems (.doc, .pdf, etc.)

**Parameters for index deletion:**
- _**index.name**_ (required): The name of the index to be deleted.

**Parameters for field creation/update:**
- _**field.default**_ (optional): The field.default denotes the given field is set to default or not. The field.default can be "YES" or "NO".
- _**field.unique**_ (optional): The field.unique denotes the field is set to unique or not The field.unique can be "YES" or "NO".
- _**field.name**_ (required): The name of the field to be create or set as default field or unique field.
- _**field.analyzer**_ (optional): The name of the analyzer.
- _**field.stored**_ (optional): The field.stored indicates that the field need to be stored or not. It has two options "YES"  or "NO".
- _**field.indexed**_ (optional): The field.indexed indicates that the field need to be indexed or not. It has two options "YES"  or "NO".
- _**field.termVector**_ (optional): The field.termVector indicates that the that the term vector need to be saved or not. It has two options "YES"  or "NO" or POSITIONS_OFFSETS.

**Parameters for field deletion:**
- _**field.name**_ (required): The name of the field to be deleted.

### Example

**HTTP request:**

Creating an index:

    http://localhost:9090/schema?cmd=createindex&index.name=index1&index.template=WEB_CRAWLER
 
List all the available index:

    http://localhost:9090/schema?cmd=indexlist
 
Deleting an index:

    http://localhost:9090/schema?cmd=deleteindex&index.name=index1&index.delete.name=index1
 
Setting/Creating a schema field:

    http://localhost:9090/schema?cmd=setField&field.name=titleNew&field.analyzer=StandardAnalyzer&use=index1&field.stored=YES&field.indexed=YES&term.termVector=NO
    
**HTTP response:**

Response for deleting an index:

```xml
<response>
    <entry key="Info">Index deleted: index</entry>
    <entry key="Status">OK</entry>
</response>
```

Response for creating an new schema field:

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