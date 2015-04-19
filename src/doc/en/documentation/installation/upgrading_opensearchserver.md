## Upgrading OpenSearchServer

Upgrading OpenSearchServer to a newer version is a quick and easy process.

### Backing up your data

Before upgrading OpenSearchServer **it is strongly recommended to back up all your data**. To do so, locate the `data` folder used by OpenSearchServer. This is the folder where all indexes and settings are stored.

The location of this folder is written in the `start.bat` script (used on Windows) or `start.sh`, on the `set OPENSEARCHSERVER_DATA=%cd%\data` or `OPENSEARCHSERVER_DATA=data` line. Generally, the `data` folder is located in the root folder wherein the `.zip` or `.tar.gz` was uncompressed. 

If using the Debian package you will find the location of the `data` folder in the file `/etc/opensearchserver`. Location is defined in the `OPENSEARCHSERVER_DATA` variable. The default value in the Debian package is `/var/lib/opensearchserver/data`.

To actually make your backup, stop OpenSearchServer then simply copy the whole `data` folder to a safe location.

### Upgrading OpenSearchServer

#### Upgrading a default install

If OpenSearchServer was installed by uncompressing a `.zip` or `.tar.gz` file, upgrading simply consists in replacing the existing `.war` with the new one.

To do so:

1. stop OpenSearchServer
2. in the root folder wherein OpenSearchServer was uncompressed, open the `server/webapps` subfolder
3. rename the `ROOT.war` file to `ROOT.war.backup` to create a backup of the existing version
4. **delete the `server/webapps/ROOT` folder**
5. download the `.war` file for the new version
6. copy it into the `server/webapps`folder
6. **rename the new `.war` file to `ROOT.war`**
7. start OpenSearchServer

OpenSearchServer is now upgraded! You can verify by checking the version number at the top of the interface. 


It is recommended to **first test a new version of OpenSearchServer on a test server**, using your data. To do so you can make a fresh install of OpenSearchServer and copy your data there:

1. download the `.zip` or `.tar.gz` package
2. uncompress it on your test server
3. copy your data from your current `data` folder to the `data` folder of this fresh install
4. start your test version of OpenSearchServer
5. check the behaviour of this new version before actually upgrading your production environment. 

**Restoring a previous version of OSS**

If you notice irregularities after upgrading you can restore a previous version of the software. Since some versions of OpenSearchServer modify the index, you may also need to restore your data.

To resume running an older version follow the upgrading steps above -- but with your backup `.war` file (`ROOT.war.backup`) being the one that becomes active.

#### Upgrading a Debian package

To update a version of OpenSearchServer installed with a Debian package:

1. stop OpenSearchServer
2. download the new `.deb` file and run the `dpkg -i <downloaded_package.deb>` command
3. start OpenSearchServer
