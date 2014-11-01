OpenSearchServer
================
http://www.opensearchserver.com

Copyright Emmanuel Keller / Jaeksoft (2008-2014)
This software is licensed under the GPL v3.

OpenSearchServer is a powerful enterprise-class search engine program. Using its user interface web pages, the crawlers (web, file, database, ...) and the REST/RESTFul API you will be able to quickly and easily integrate advanced full-text search capabilities in your application.

OpenSearchServer runs on Linux/Unix/BSD/Windows.

Quickstart
----------
### One requirement
You need to have a JAVA 1.7 (or newer) runtime on your server.

### Download the last ZIP or TAR.GZ archive:
http://www.opensearchserver.com/#download

### Deflate the content to get the following files:
- ```FILE``` opensearchserver.jar -> the main library
- ```FILE``` README.md -> this file
- ```DIR``` data -> will contain your index
- ```DIR``` server -> will contain the servers files
- ```FILE``` start.sh -> Shell to start the server on Unix
- ```FILE``` start.bat -> Batch to start the server on Windows
- ```FILE``` NOTICE.txt -> the third-party licensing information
- ```DIR``` LICENSES -> contains the detailled licenses

### Edit the parameters 
If desired, one can change the following parameters in the start.sh or start.bat scripts:
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
+ Download the binaries at: http://www.opensearchserver.com/#download
+ Read the documentation at: http://www.opensearchserver.com/documentation 
+ Review the issues (bugs and enhancements): https://github.com/jaeksoft/opensearchserver/issues

Features
--------
### Search functions
- Advanced full-text search features
- Phonetic search
- Advanced Boolean search using a querying language
- Clustered results with faceting and collapsing
- Filter searches using sub-requests (including negative filters)
- Geolocation
- Spell-checking
- Customized parameters for relevance computation
- Search suggestions (auto-completion)

### Indexation
- Supports 18 languages
- Fields schema with analyzers in each language
- Multiple filters: n-gram, lemmatization, shingle, stripping diacritic from words,…
- Automatic language recognition
- Named entities recognition
- Words synonyms and expressions synonyms
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
- Web crawler crawling the internet, extranets and intranets
- File systems crawler crawling local and remote files (NFS, SMB/CIFS, FTP, FTPS, SWIFT)
- Database crawler crawling any JDBC databases (MySQL, PostgreSQL, Oracle, SQL Server, …)
- Filter inclusions or exclusions, with wildcards
- Session parameters removal
- SQL join and linked files support
- Screenshot capture
- Sitemap import

### General
- REST API (XML and JSON)
- Monitoring module
- Index replication
- Scheduler for management of periodic tasks
- WordPress plugin and Drupal module
