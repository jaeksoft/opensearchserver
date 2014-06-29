OpenSearchServer
================
http://www.opensearchserver.com

Copyright Emmanuel Keller / Jaeksoft (2008-2013)
This software is licensed under the GPL v3.

OpenSearchServer is a powerful, enterprise-class, search engine program. Using the web user interface, the crawlers (web, file, database, ...) and the REST/RESTFul API you will be able to integrate quickly and easily advanced full-text search capabilities in your application. OpenSearchServer runs on Linux/Unix/BSD/Windows.

Quickstart
----------
### One requirement
You need to have a JAVA 6 (or newer) runtime on your server

### Download the last ZIP or the TAR.GZ archive:
http://www.opensearchserver.com/#download

### Deflate the content to get the following files:
- ```FILE``` opensearchserver.jar -> the main library
- ```FILE``` README.md -> this file
- ```DIR``` data -> will contains your index
- ```DIR``` server -> will contains servers files
- ```FILE``` start.sh -> Shell to start the server on Unix
- ```FILE``` start.bat -> Batch to start the server on Windows
- ```FILE``` NOTICE.txt -> the third-party license informations
- ```DIR``` LICENSES -> Contains the detailled licenses

### Edit the parameters 
Optionally, can you change the parameters in the start.sh/start.bat script:
- The allowed memory size
- The TCP port (9090 by default)

### Start the server
```
cd opensearchserver
./start.sh
```

### Go with the interface and/or the API
http://localhost:9090

Useful links
------------
+ Download binaries: http://www.opensearchserver.com/#download
+ The documentation: http://www.opensearchserver.com/documentation 
+ Issues (bugs, enhancements): https://github.com/jaeksoft/opensearchserver/issues

Features
--------
### Search functions
- Advanced full-text search features
- Phonetic search
- Advanced boolean search with query language
- Clustered results with faceting and collapsing
- Filter search using sub-requests (including negative filters)
- Geolocation
- Spell-checking
- Relevance customization
- Search suggestion facility (auto-completion)

### Indexation
- Supports 18 languages
- Fields schema with analyzers in each language
- Several filters: n-gram, lemmatization, shingle, stripping diacritic from words,…
- Automatic language recognition
- Named entity recognition
- Word synonyms and expression synonyms
- Export indexed terms with frequencies
- Automatic classification

### Document supported
- HTML / XHTML
- MS Office documents (Word, Excel, Powerpoint, Visio, Publisher)
- OpenOffice documents
- Adobe PDF (with OCR)
- RTF, Plaintext
- Audio files metadata (wav, mp3, AIFF, Ogg)
- Torrent files
- OCR over images

### Crawlers
- The web crawler for internet, extranet and intranet
- The file systems crawler for local and remote files (NFS, SMB/CIFS, FTP, FTPS, SWIFT)
- The database crawler for all JDBC databases (MySQL, PostgreSQL, Oracle, SQL Server, …)
- Filter inclusion or exclusion with wildcards
- Session parameters removal
- SQL join and linked files support
- Screenshot capture
- Sitemap import

### General
- REST API (XML and JSON)
- SOAP Web Service
- Monitoring module
- Index replication
- Scheduler for management of periodic tasks
- WordPress plugin and Drupal module
