Installing OSS on a Mac is very straightforward. The Short Version below should be sufficient ; if not use the More Detailed Version.

## Short Version

- Make sure you have JAVA 6 or newer correctly installed
- Download the latest stable build [tar.gz](http://www.open-search-server.com/home/download/ "download")
- Deflate it and run **start.sh**, which you'll find within the OSS folder
- Open your favorite browser with the URL [http://yourserver:9090](http://yourserver:9090) (replace **yourserver** by **localhost** if it's running on your own machine)
- Enjoy discovering OpenSearchServer

## More Detailed Version
The following covers the same steps as above, in more detail.

### Checking your Java version ###

In a shell, enter the command line

    java -version

Check whether your Java version is 6 or newer:

- If your version is not Version 6 or greater you'll need to update your Java engine
- If you don't have Java, install it (see below)

### Downloading OSS

We recommend that you always get the latest version of OSS on [SourceForge](http://www.open-search-server.com/download/ "Download").

Please download the **tar.gz** or **zip** package, then deflate it as in the example below:

    tar -xzvf open-search-server-1.5.tar.gz
    
or

	unzip open-search-server-1.5.zip
    
Once unzipped you'll get a folder called **opensearchserver**. All your OSS binaries (and your future data) will be stored within this folder.

### Running it
At this stage, running OSS on Mac still requires one command line. This means you have to use the Terminal application, which is in the Utilities folder within the Applications folder of your Mac.

Using the Terminal, go to your newly-downloaded OpenSearchServer folder. Once there, launch the Start application by typing in **./start.sh**. That's it!

#### Running it without knowing anything about Terminal mode

If you have never used the Terminal before, you may not know how to go to the OpenSearchServer folder. This is not very complicated, as you'll only need to understand the basics of two simple Unix-style commands:

- if you type ls (the letter L and the letter S, short for "list"), the Terminal will list all the files and folders in the folder you're currently in. This allows you to know where you are and what you can access.
- if you type cd (short for "change directory"), the Terminal will go the place you specify (for instance cd downloads to enter the "downloads" directory).

Thus, if you type **ls** and see that the folder you are currently in has a folder called **Downloads**, typing **cd Downloads** will take you within that folder. There, a typical user would type ls again to see whether the OpenSearchServer folder is indeed there.

Typing **cd opensearchserver** will take the user into the OpenSearchServer folder, where the **start.sh** file we're looking for is.