## How to build OpenSearchServer

Do you want to contribute to OpenSearchServer?

Here is how to compile and build OSS.
 
### Prerequisite
Here are the tools you need to build OpenSearchServer:

* To build the war: [Maven](http://maven.apache.org/)
* To build the archive package (zip and tar.gz): [Ant](http://ant.apache.org/)

### Extract the source code using GIT
The default and current active branch is 1.5.
```
git clone https://github.com/jaeksoft/opensearchserver.git
```

### Go to the opensearchserver directory
```shell
cd opensearchserver
```

### Install the custom jar in your local maven repository
Few libraries (jar files) are not available in [public Maven repositories](http://search.maven.org/). The recommended way is to put them in your local repository using the following commands:
```shell
mvn install:install-file -Dfile=lib/pojodbc-1.1.2.jar \
  -DgroupId=com.jaeksoft -DartifactId=pojodbc -Dversion=1.1.2 -Dpackaging=jar
mvn install:install-file -Dfile=lib/icepdf-viewer-4.4.0.jar \
  -DgroupId=org.icepdf -DartifactId=icepdf-viewer -Dversion=4.4.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/icepdf-core-4.4.0.jar \
  -DgroupId=org.icepdf -DartifactId=icepdf-core -Dversion=4.4.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/hunspell-dren-1.0.jar \
  -DgroupId=dk.dren -DartifactId=hunspell-dren -Dversion=1.0 -Dpackaging=jar
```

### Use Maven to build the war file
```shell
mvn package
```

The builded war is available here:

target/opensearchserver-1.5-DEV.war

### To build the zip and tar.gz package
The archive includes Apache Tomcat and the start and stop scripts.

```shell
ant dist
```

The builded zip and tar.gz archive are available here:

dist/open-search-server.tar.gz
dist/open-search-server.zip

Alternatively, you can also download [these packages at SourceForge](http://sourceforge.net/projects/opensearchserve/files/)