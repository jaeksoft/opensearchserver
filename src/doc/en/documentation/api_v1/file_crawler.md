## File Crawler API

_**This API is deprecated, please refer to the [new RESTFul API](../api_v2/README.html)**_ instead.

    http://{server_name}:9090/filecrawler

The fileCrawler API performs actions such as:
* create/update: creates a file crawler item.
* start: starts the file crawler.
* stop: stops the file crawler.

**Parameters:**
- _**use**_ (required): The name of the index.
- _**login**_ (optional): The login parameter. This becomes required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This becomes required once you create a user.
- _**cmd**_: The command to perform: create, stop, start.
- _**type**_: The file crawler type, chosen among the following :
  - file - Local file system instances.
  - smb - SMB file instance.
  - ftp - FTP file instance.
  - ftps - FTP with SSL file instance.
  - dropbox - Dropbox file instance.
- _**enabled**_: This enables or disables the file crawler. It is a Boolean parameter that can be set to true or false.
- _**withsubdirectory**_: The withsubdirectory parameter allows for creating file crawler that includes the sub directories under the current path.
It is a Boolean parameter that can be set to true or false.
- _**delay**_: The crawl delay for the file crawler instance.
- _**ignorehidden**_: The ignorehidden parameter allows for ignoring hidden files in the file system. It is a Boolean parameter that can be set to true or false.
- _**domain**_: The domain parameter for SMB and Dropbox file types.
- _**username**_: The username parameter for SMB, FTP, FTPS or Dropbox file types.
- _**password**_: The password parameter for SMB, FTP, FTPS or Dropbox file types.
- _**host**_: The host parameter for SMB, FTP, FTPS or Dropbox file types.

### Examples

Starting the FileCrawler instance:

    http://localhost:9090/filecrawler?use=index1&cmd=start
 
Stopping the FileCrawler instance:

    http://localhost:9090/filecrawler?use=index1&cmd=stop
 
Creating a FileCrawler instance:

    http://localhost:9090/filecrawler?use=file&cmd=create&type=file&path=/home/opensearchserver&withsubdirectory=true&delay=10&enabled=true&ignorehidden=true

### HTTP response

The following indicates that 14 documents have been deleted from the index:

```xml
<response>
    <entry key="info">A new file crawler instance is created.</entry>
</response>
```

