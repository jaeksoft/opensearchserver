## Installation on Windows

## Short Version

- Make sure you have JAVA 6 or newer correctly installed
- Download the latest stable build [zip](http://www.open-search-server.com/download/ "Download")
- Deflate it and run **start.bat**, which you'll find within the OpenSearchServer folder
- Open your favorite browser with the url [http://yourserver:9090](http://yourserver:9090) (your server being **localhost** if it's running on your local machine)
- Enjoy discovering OpenSearchServer

## More Detailed Version

The following covers the same steps as above, in more detail.

### Checking your version of Java

- Go to this [page](http://www.java.com/en/download/installed.jsp) to know which version of Java is running on your computer
- If your version is not a Version 6 or greater, you will need to update your Java engine
- If the web page states that you don't have any Java machine, please install one

In the latter two cases, you will be presented with a **Free Java Download** button. Just go ahead - press it and follow the provided instructions.

### Downloading OpenSearchServer

We recommend that you always get the latest version of OSS on [SourceForge](http://www.open-search-server.com/download/ "Download").

Please download the **ZIP** package for Windows and unzip it on your disk.

When the file is unzipped you'll get a folder **opensearchserver**. All your OSS binaries (and your future data) will be stored in this folder.

### Running it

Just launch the **start.bat** file in your OpenSearchServer base folder.

To access your OSS Back Office, open you browser (Firefox, Chromium, Opera, Safari...) and open the page [http://yourserver:9090](http://yourserver:9090)

If everything went right you'll see the OpenSearchServer interface. If nothing gets displayed and you're certain you've done everything right, please check the troubleshooting section.

## Troubleshooting

This section reviews the possible causes for trouble. You only need to go through them if things are not going right.

### Checking the environnement variables

- Right click **My Computer**
- Select **Properties**
- Select **Advanced** tab
- Select **Environment Variables**

You will see a list of variables. What you need is either a **JRE_HOME** or **JAVA_HOME** variable, depending on the type of Java engine you are running.

If the correct variable is present, please check that it targets the correct folder.

If the desired variable is not present, create it thus:

- If you have a JRE, create or update the environment variable **JRE_HOME**. Set it to target the folder where your Java JRE is, for example **C:\Program Files\Java\jre1.7.0_51**
- If you have a JDK, create or update the environment variable **JAVA_HOME**. Set it to target the folder where your Java JDK is, for example **C:\Program Files\Java\jdk1.7.0_51**

Once you have done that try again to launch OpenSearchServer through **start.bat**.

If you have Java, do not see the variable *and* do not remember whether you have a JDK or JRE, go to the Start menu, Click Run, type cmd to call up the command line interface, and type java -version to see what's installed.

### Checking whether your 9090 port is free

If by chance you already have a server listening on the 9090 tcp port you'll have to change it in the **start.bat** config files.

To check whether the 9090 port is already occupied use the following command line:

    netstat -o -n -a | findstr 0.0:9090
    
If you have a response resembling the one below, your port isn't free:

    TCP    0.0.0.0:9090           0.0.0.0:0              LISTENING       676
    
If port 9090 is already being used, we suggest that you simply use another port for OpenSearchServer. To do so:

Edit in you OpenSearchServer folder the file **start.bat**
Locate the line starting with:

    SERVER_PORT=9090

Then change that to:

    SERVER_PORT=9091

Save your file and start OpenSearchServer with **start.bat**

Your OpenSearchServer Back Office is now available at this address: [http://yourserver:9091](http://yourserver:9091)