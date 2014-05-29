## Out of memory issue

By default, OpenSearchServer only uses 256 megabytes of RAM. This is not enough for advanced uses. To allow OSS to use more memory, add two lines at the start.sh file.

### Allowing more RAM

The memory allocated to OSS is defined by the JAVA_OPTS options.

**For Linux/Mac (start.sh)**

    JAVA_OPTS="-Xms1G -Xmx1G"

**For Windows (start.bat)**

    set JAVA_OPTS=-Xms1G -Xmx1G  

`-Xms2G -Xmx2G` means 2 GB of RAM.
You may also use `-Xms768m -Xmx768m` to allow OSS to use 768 megabytes of RAM.

### Using more than 2GB of RAM

OpenSearchServer must be run in 64 bits mode in order to use more than 2 gigabytes of RAM.

Make sure that you are using a 64 bits operating system, and that you have installed a 64 bits Java runtime.

Once this has been verified, modify the start.sh file as follows:
  
JAVA_OPTS="-d64 -Xms6G -Xmx6G -server"  

The `Xms` and `Xmx` parameters control the memory (6 gigabytes in this example).

The `-d64` parameter enables 64 bits operations.

### How much RAM is used/available?

Look at the **free memory rate** value on the monitoring panel to check whether OpenSearchServer is running comfortably. Go to the `/Runtime/System/General` panel. A value higher than 20% is recommended.

![Memory usage](outofmemory.png)
