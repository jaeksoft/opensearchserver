## Linux generic installation

Please use this page for reference when installing OSS on a Linux system (Ubuntu, CentOS, Xandros, etc.).

The Short Version should be sufficient ; if not use the More Detailed Version.

## Short version

- Make sure you have a Java 6 Virtual Machine or newer correctly installed
- Download the latest stable build [tar.gz](http://www.open-search-server.com/download/  "Download")
- Deflate it and run start.sh, which you'll find within the OSS folder
- Open your favorite browser with the URL [http://yourserver:9090](http://yourserver:9090) (replace **yourserver** by **localhost** if it's running on your own machine)
- Enjoy discovering OpenSearchServer

## More Detailed version

Install a Java runtime (JVM). Here are some commons JVMs:

- OpenJDK (http://openjdk.java.net/)
- Oracle/SUN Java (http://www.java.com/en/download/)
- IBM Java (https://www.ibm.com/developerworks/java/jdk/")

The following covers the same steps as above, in more detail.

### Checking your Java version ###

In a shell, enter the command line

    java -version

Check whether your Java version is 6 or newer:

- If your version is not Version 6 or greater you'll need to update your Java engine
- If you don't have Java, install it (see below)

### Installing Java

Our Open Source-friendly recommendation is that you install the latest OpenJDK (1.7.0 as of this writing).

To install OpenJDK just follow the instructions provided at [the openJDK site](http://openjdk.java.net/install)

### Downloading OpenSearchServer

We recommend that you always get the latest version of OSS on [SourceForge](http://www.open-search-server.com/download/ "Download").

Please download the **tar.gz** package for Linux/BSD, then deflate it as in the example below:

    tar -xzvf open-search-server-1.5.tar.gz
    
Once unzipped you'll get a folder called **opensearchserver**. All your OSS binaries (and your future data) will be stored within this folder.

### Running it

Just launch the start.sh present in your OSS base folder to get the application running:

    cd open-search-server
    ./start.sh
    
To access your OSS Back Office, open you browser (Firefox, Chromium, Opera, Safari...) and open the page [http://yourserver:9090](http://yourserver:9090)

If everything went right you'll see the OSS interface. If nothing is displaying and you are sure you followed this installation procedure correctly, check the troubleshooting section.

So far, so good ? You can now go to the next step and create your first index.

## Troubleshooting

### Checking whether your 9090 port is free

If by chance you already have a server listening on the 9090 tcp port you'll have to make a change in the Catalina config files.

To check whether the 9090 port is already occupied use the following command line:

    netstat -an | grep :9090
   
If you have a response resembling the one below, then your port isn't free:

    TCP    0.0.0.0:9090           0.0.0.0:0              LISTENING       676
    
If port 9090 is already being used, we suggest that you simply use another port for OpenSearchServer. To do so:

Open in you OSS folder the file **start.sh**
Locate the line starting with:

    SERVER_PORT=9090

Then change that to:

    SERVER_PORT=9091

Save your file and start OpenSearchServer with **start.sh**

Your OpenSearchServer Back Office is now available at this address: [http://yourserver:9091](http://yourserver:9091)
