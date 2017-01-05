## How to build OpenSearchServer

Would you like to contribute to OpenSearchServer?

Here is how to compile and build OSS.
 
### Prerequisites
Here are the tools you need to build OpenSearchServer:

* To build the war: [Maven](http://maven.apache.org/).
* To build the archive package (zip and tar.gz): [Ant](http://ant.apache.org/).

### Extract the source code using GIT
The default and currently active branch is 1.5.
```
git clone https://github.com/jaeksoft/opensearchserver.git
```

### Go to the opensearchserver directory
```shell
cd opensearchserver
```

### Use Maven to build the artifacts
```shell
mvn -Dgpg.skip=true package
```

The artifacts are available in the target directory:

Alternatively, you can download [these packages at SourceForge](http://sourceforge.net/projects/opensearchserve/files/).
