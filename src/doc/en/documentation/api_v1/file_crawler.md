## File Crawler API

_**This API is deprecated, have a look at the [new RESTFul API](../api_v2/README.html)**_

    http://{server_name}:9090/filecrawler

The fileCrawler API performs actions such as:
* create/update: create a file crawler item.
* start: start the file crawler.
* stop: stop the file crawler

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.
- _**cmd**_: The cmd to perform: create, stop, start
- _**type**_: The file crawler type. File crawler has following types:
  - file - Local file system instances
  - smb - SMB file instance
  - ftp - FTP file instance
  - ftps - FTP with ssl file instance.
  - dropbox - Dropbox file instance.
- _**enabled**_: The enabled parameter for enabling and disabling the file crawler.This is an Boolean parameter with inputs true or false.
- _**withsubdirectory**_: The withsubdirectory parameter is for creating an file crawler which includes all the sub directory under the current path.
The parameter is an Boolean parameter with inputs true or false.
- _**delay**_: The crawl delay for the file crawler instance.
- _**ignorehidden**_: The ignorehidden parameter is to ignore the hidden files from the file system.This is an Boolean parameter with inputs true or false.
- _**domain**_: The domain parameter for SMB or Dropbox file types.
- _**username**_: The username parameter for SMB,FTP,FTPS or Dropbox file types.
- _**password**_: The password parameter for SMB,FTP,FTPS or Dropbox file types.
- _**host**_: The host parameter for SMB,FTP,FTPS or Dropbox file types.

### Example

Starting the FileCrawler instance

    http://localhost:9090/filecrawler?use=index1&cmd=start
 
Stopping the FileCrawler instance

    http://localhost:9090/filecrawler?use=index1&cmd=stop
 
Creating a FileCrawler instance

    http://localhost:9090/filecrawler?use=file&cmd=create&type=file&path=/home/opensearchserver&withsubdirectory=true&delay=10&enabled=true&ignorehidden=true

### HTTP response

This indicates that 14 documents has been deleted from the index:

```xml
<response>
    <entry key="info">A new file crawler instance is created.</entry>
</response>
```

