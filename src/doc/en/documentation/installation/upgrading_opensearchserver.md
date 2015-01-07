## Upgrading OpenSearchServer

Upgrading OpenSearchServer to a newer version is a quick and easy process.

### Backuping data

Before upgrading OpenSearchServer **it is a good practice to backup all your data**. To do so, locate the `data` folder used by OpenSearchServer. This is the folder where all index and configuration are stored.

Location of this folder can be found in script `start.bat` (Windows) or `start.sh`, in line `set OPENSEARCHSERVER_DATA=%cd%\data` or `OPENSEARCHSERVER_DATA=data`. `data` folder is usually located in the root folder where the `.zip` or `.tar.gz` was uncompressed. 

To backup this folder, first stop OpenSearchServer and then merely copy the whole `data` folder to a backup location.

If you are using the Debian package you will find the location of the `data` folder in the file `/etc/opensearchserver`. Location is defined in variable `OPENSEARCHSERVER_DATA`. Default value for Debian package is `/var/lib/opensearchserver/data`.

### Upgrading OpenSearchServer

#### Upgrading default install

If OpenSearchServer was installed by uncompressing a `.zip` or `.tar.gz` file, upgrading simply consist in replacing the current `.war` file by a new one.

Steps are:

1. stop OpenSearchServer
* in the root folder where OpenSearchServer was uncompressed go to subfolder `server/webapps`
* rename file `ROOT.war` to `ROOT.war.backup` to create a backup of the previous version
* **delete folder `server/webapps/ROOT`**
* download the `.war` file for the new version you want to install and copy it to `server/webapps`
* **rename the new `.war` file to `ROOT.war`**
* start OpenSearchServer

OpenSearchServer is now upgraded! Check new version number at the top of the interface. 


It is a best practice to **first test a new version of OpenSearchServer on a test server** with your data. To do so you can make a fresh install of OpenSearchServer and copy your data there:

* download the `.zip` or `.tar.gz` package, 
* uncompress it on your test server
* copy your data from your current `data` folder to the `data` folder of this fresh install,
* start the new OpenSearchServer,
* check the behaviour of this new version before applying real upgrade to your production environment. 

##### Restoring previous version

If you notice some trouble after upgrading you can restore previous version. Since some versions of OpenSearchServer apply some transformation to the index you may also need to restore your data if restoring previous version of OpenSearchServer does not solve the issues. 

To restore previous version simply use the upgrading steps but use your backuped `.war` file (`ROOT.war.backup`) as the "new `.war` file".

#### Upgrading debian package

To update OpenSearchServer installed as a debian package steps are:

1. stop OpenSearchServer
* download new `.deb` file and run command `dpkg -i <downloaded_package.deb>`
* start OpenSearchServer 
