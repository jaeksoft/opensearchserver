Please use this page for reference when installing OSS on a Debian linux system using .DEB package.

The Short Version should be sufficient ; if not use the More Detailed Version.

## Short version

- Make sure you have a Java 7 Virtual Machine or newer correctly installed
- Download the latest stable build [opensearchserver.deb](http://www.open-search-server.com/download/  "Download")
- Install it with the following command: `dpkg -i opensearchserver.deb`
- Start the service using: `service opensearchserver start`
- Open your favorite browser with the URL [http://yourserver:9090](http://yourserver:9090) (replace **yourserver** by **localhost** if it's running on your own machine)
- Enjoy discovering OpenSearchServer

## More Detailed version

### Checking your Java version ###

In a shell, enter the command line

    java -version

Check whether your Java version is 7 or newer:

- If your version is not Version 7 or greater you'll need to update your Java engine
- If you don't have Java, install it (see below)

### Installing Java

Our Open Source-friendly recommendation is that you install the latest OpenJDK (1.7.0 as of this writing).

To install OpenJDK just use your favorite package manager:

    apt-get install jdk

### Installing OpenSearchServer

We recommend that you always get the latest version of OSS on [SourceForge](http://www.open-search-server.com/download/ "Download").

Please download the **opensearchserver.deb** package for Debian, then install it as in the example below:

    dpkg -i opensearchserver.deb
    
Once installed you will get the following folders:

- **/var/lib/opensearchserver**: Contains all your data (indexes).
- **/usr/share/opensearchserver**: Contains all your OSS binaries.

You also get the following files:
- **/etc/opensearchserver**: The configuration file of your OpenSearchServer instance
- **/etc/init.d/opensearchserver**: The script used by the system to start and stop the OpenSearchServer instance.

### Running it

Just use the service command:

    services opensearchserver start
    
To access your OSS Back Office, open you browser (Firefox, Chromium, Opera, Safari...) and open the page [http://yourserver:9090](http://yourserver:9090)

If everything went right you'll see the OSS interface. If nothing is displaying and you are sure you followed this installation procedure correctly, check the troubleshooting section.

So far, so good ? You can now go to the next step and create your first index.

### Stopping it

Use the service command with the stop parameter:

    services opensearchserver stop
    
## Configuration

The configuration file is located at **/etc/opensearchserver**.

### Changing the listening port

If by chance you already have a server listening on the 9090 tcp port you'll have to make a change in the config files.

Locate the line starting with:

    SERVER_PORT=9090

Then change that to another port:

    SERVER_PORT=9091

Restart your OpenSearchServer instance:

    services opensearchserver stop
    services opensearchserver start

Your OpenSearchServer Back Office is now available at this address: [http://yourserver:9091](http://yourserver:9091)

### Changing the memory usage

By default, OpenSearchServer don't set the memory allocation. It uses the default value from the Java Virtual Machine.

If you need to increase it, just edit the configuration file.

Locate the line with:

    #JAVA_OPTS="-Xms1G -Xmx1G"

Uncomment the line, and set the memory value as you need:

    JAVA_OPTS="-Xms2G -Xmx2G"
    
Restart OpenSearchServer.

Be aware that if you set a value greater than the physical memory available, OpenSearchServer will not start.