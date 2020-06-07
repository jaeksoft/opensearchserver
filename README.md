[OpenSearchServer](http://www.opensearchserver.com)
===================================================

[![Build Status](https://travis-ci.org/jaeksoft/opensearchserver.svg?branch=master)](https://travis-ci.org/jaeksoft/opensearchserver)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jaeksoft/opensearchserver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jaeksoft/opensearchserver)
[![Join the chat at https://gitter.im/jaeksoft/opensearchserver](https://badges.gitter.im/jaeksoft/opensearchserver.svg)](https://gitter.im/jaeksoft/opensearchserver)

OpenSearchServer is a powerful, enterprise-class, search engine software based on Lucene.
Using the web user interface, the crawlers (web, file, database, ...) and the JSON webservice you will be able to integrate quickly and easily advanced full-text search capabilities in your application. OpenSearchServer runs on Linux/Unix/BSD/Windows.


Quickstart
----------
### Docker image

Not yet there.. coming soon..

### Go with the interface and/or the API
http://localhost:9090

Useful links
------------
+ Download binaries: https://www.opensearchserver.com/#download
+ The documentation: https://www.opensearchserver.com/documentation 
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

### General
- JSON web service
- Index replication and sharding
- Federated search

License
-------
Copyright Emmanuel Keller / Jaeksoft (2008-2020)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
