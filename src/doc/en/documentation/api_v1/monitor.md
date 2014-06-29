## Monitor API

_**This API is deprecated, have a look at the [new RESTFul API](../api_v2/README.html)**_

    http://{server_name}:9090/monitor

This API Monitors the index with available processors,system information etc.

**Parameters:**
- _**use**_ (required): It is the index name
- _**login**_ (optional): The login parameter. This is required once you create a user.
- _**key**_ (optional): The key parameter related to the login (api key). This is required once you create a user.

### Example

HTTP Request:

    http://localhost:9090/monitor 
 

HTTP response:

```xml
<response>
  <system>
    <availableProcessors value="2"/>
    <freeMemory value="10110504" rate="40.175533294677734"/>
    <maxMemory value="674824192"/>
    <totalMemory value="25165824"/>
    <indexCount value="2"/>
    <freeDiskSpace value="231921213440" rate="74.33024735165283"/>
    <dataDirectoryPath value="/home/data"/>
  </system>
  <properties>
    <property name="java.runtime.name" value="OpenJDK Runtime Environment"/>
    <property name="sun.boot.library.path" value="/usr/lib/jvm/java-6-openjdk/jre/lib/i386"/>
    <property name="java.vm.version" value="20.0-b11"/>
    <property name="shared.loader" value=""/>
    <property name="java.vm.vendor" value="Sun Microsystems Inc."/>
    <property name="java.vendor.url" value="http://java.sun.com/"/>
    <property name="path.separator" value=":"/>
    <property name="tomcat.util.buf.StringCache.byte.enabled" value="true"/>
    <property name="java.vm.name" value="OpenJDK Server VM"/>
    <property name="file.encoding.pkg" value="sun.io"/>
    <property name="sun.java.launcher" value="SUN_STANDARD"/>
    <property name="user.country" value="US"/>
    <property name="sun.os.patch.level" value="unknown"/>
    ....
  </properties>
</response>
```

