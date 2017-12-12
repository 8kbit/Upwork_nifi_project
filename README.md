The requirement is to create SFTP server that can be downloaded from Apache NIFI web UI. This server would be used to push files from the server and also cater to the pull files request from a NIFI SFTP Client Processor. The server is also capable of connecting to a local database and sending data form that databse as a file. The server is configured thorugh the client. These configurations are sent to the server as a file in a get request. Push configurations are also sent to the client in the same way. Push configuraitons push files from the server to the client at configurable time intervals. NIFI SFTP client processor can be created as shown in  [http://www.nifi.rocks/developing-a-custom-apache-nifi-processor-json/](http://www.nifi.rocks/developing-a-custom-apache-nifi-processor-json/) And NIFI itslef has a GetSFTP and some other SFTP processors. Code from these processors can be copied and improved upon to create new processor.

**Technical Project requirements:**

1. Enhance functionality of Apache NIFI web and create a new NIFI processor (SFTP Client) similar to NIFI SFTP fetch processor.
2. Create a SFTP server using Apache Mina(SFTP Server) that can be downloaded, and configured before download (per request).
3. Create a JavaFX UI for SFTP server using  [http://www.jfoenix.com/index.html#start](http://www.jfoenix.com/index.html#start). The UI is to allow the user to configure the SFTP server

**Enhancements to NIFI SFTP Client:**  a) Place SFTP Server in the web directory of NIFI such that it can be downloaded as a self contained .exe Or a runnable Jar, depending on the if it is being downloaded for Windows or Linux platform.

b) Each SFTP Server instance (created per download) and SFTP Client instance should have a unique ID. This ID is used By the server to identify the client and By the client to identify the server.

c) Each SFTP Server shall accept four types of configurations per request. Server Instance Configuration, Push Files Configuration, Pull File Configurations and Database request configurations.

d) Add Additional Client Configurations Following configurations are additional configurations in addition to those already existing in Nifi GetSFTP / FetchSFTP / ListSFTP/ PutSFTP processors Client. These processors would be used as SFTP clients.

// One Client can send same request to multiple droppoints.Provide a Comma separated list of SFTP servers to send the fetch requests to.

sftp.client.droppoints.list=

**Enhancements to MINA SFTP Server:**  a) This SFTP server would ping the SFTP client with it&#39;s ID at configurable intervals to confirm to the client that it is up and running.

b) The server keeps a track of which files have already been successfully transferred. This setting is used to ensure that the same file is not processed again. The configuration is sftp.server.filter.ignore.processed.file

c) The server must support ZLIB compression and would be turned on based on the rule file sent to the server.

d) Create a JavaFX UI for SFTP server using  [http://www.jfoenix.com/index.html#start](http://www.jfoenix.com/index.html#start). The UI is to allow the user to configure the SFTP server

**Code for the rules can be found at:**

- .SFTP Client: nifi/nifi-nar-bundles/nifi-standard-bundle/nifi-standard-processors/src/main/java/org/apache/nifi/processors/standard/FetchSFTP.java
- .SFTP Server  [https://github.com/apache/mina-sshd](https://github.com/apache/mina-sshd)
- .SFTP Server UI:  [http://www.jfoenix.com/index.html#start](http://www.jfoenix.com/index.html#start)

**Warnings and Error messages:**  File must at-least have a read permission. Specified Remote Port is busy, please change the port number. No connections available in the database connection pool.

# EXAMPLE JSON TO BE SENT AS A RULE FILE.

Meaning of each of the property is described below the json

As demnstrated below, each request can have multiple rules for Pull, Push and DB calls. The follwong JSON demonstartes two of each kind.

```{              sftp.client.identity.id: Abc-123-xYz,

        sftp.server.heartbeat.interval: 180sec,

        Pull Accounting Files: {

                sftp.server.rule: pullfile,

                sftp.server.rule.name: Get accounting files,

                sftp.server.filter.dir.path: ,

                sftp.server.filter.recursion.depth: 10,

                sftp.server.filter.filename: ,

                sftp.server.filter.ignore.dotted.files: true,

                sftp.server.filter.file.includefileregexlist: [^.\*\\.(?!jpg$|png$)[^.]+$, ^.\*\\.(?!jpg$|jpg$)[^.]+$],

                sftp.server.filter.file.excludefileregexlist: [^.\*\\.(?!jpg$|doc$)[^.]+$, ^.\*\\.(?!jpg$|exe$)[^.]+$],

                sftp.server.filter.filetype: ,

                sftp.server.filter.filecontent: ,

                sftp.server.filter.files.per.batch: 10,

                sftp.server.filter.total.files: 500,

                sftp.server.filter.delete.original: true,

                sftp.server.filter.ignore.processed.file: true

        },

        Pull HR Files: {

                sftp.server.rule: pullfile,

                sftp.server.rule.name: Get HR files,

                sftp.server.filter.dir.path: ,

                sftp.server.filter.recursion.depth: 10,

                sftp.server.filter.filename: ,

                sftp.server.filter.ignore.dotted.files: true,

                sftp.server.filter.file.includefileregexlist: [^.\*\\.(?!jpg$|png$)[^.]+$, ^.\*\\.(?!jpg$|jpg$)[^.]+$],

                sftp.server.filter.file.excludefileregexlist: [^.\*\\.(?!jpg$|doc$)[^.]+$, ^.\*\\.(?!jpg$|exe$)[^.]+$],

                sftp.server.filter.filetype: ,

                sftp.server.filter.filecontent: ,

                sftp.server.filter.files.per.batch: 10,

                sftp.server.filter.total.files: 500,

                sftp.server.filter.delete.original: true,

                sftp.server.filter.ignore.processed.file: true

        },

        Push new admin files: {

                sftp.server.rule: pushfile,

                sftp.server.rule.name: Keep sending new admin files,

                sftp.server.polling.interval: 180sec,

                sftp.server.push.recursively: true,

                sftp.server.use.compression: true,

                sftp.server.use.natural.ordering: true,

                sftp.server.filter.dir.path: ,

                sftp.server.filter.recursion.depth: 10,

                sftp.server.filter.filename: ,

                sftp.server.filter.ignore.dotted.files: true,

                sftp.server.filter.file.includefileregexlist: [^.\*\\.(?!jpg$|png$)[^.]+$, ^.\*\\.(?!jpg$|jpg$)[^.]+$],

                sftp.server.filter.file.excludefileregexlist: [^.\*\\.(?!jpg$|doc$)[^.]+$, ^.\*\\.(?!jpg$|exe$)[^.]+$],

                sftp.server.filter.filetype: ,

                sftp.server.filter.filecontent: ,

                sftp.server.filter.files.per.batch: 10,

                sftp.server.filter.total.files: 500,

                sftp.server.filter.delete.original: true,

                sftp.server.filter.ignore.processed.file: true

        },

        Push new IT Department files: {

                sftp.server.rule: pushfile,

                sftp.server.rule.name: Keep sending new files for IT Department,

                sftp.server.polling.interval: 180sec,

                sftp.server.push.recursively: true,

                sftp.server.use.compression: false,

                sftp.server.use.natural.ordering: true,

                sftp.server.filter.dir.path: ,

                sftp.server.filter.recursion.depth: 10,

                sftp.server.filter.filename: ,

                sftp.server.filter.ignore.dotted.files: true,

                sftp.server.filter.file.includefileregexlist: [^.\*\\.(?!jpg$|png$)[^.]+$, ^.\*\\.(?!jpg$|jpg$)[^.]+$],

                sftp.server.filter.file.excludefileregexlist: [^.\*\\.(?!jpg$|doc$)[^.]+$, ^.\*\\.(?!jpg$|exe$)[^.]+$],

                sftp.server.filter.filetype: ,

                sftp.server.filter.filecontent: ,

                sftp.server.filter.files.per.batch: 10,

                sftp.server.filter.total.files: 500,

                sftp.server.filter.delete.original: true,

                sftp.server.filter.ignore.processed.file: true

        },

        Pull HR files from DB: {

                sftp.server.rule: sql,

                sftp.server.rule.name: Get HR data from DB,

                sftp.server.use.compression: true,

                sftp.server.database.connection.url: 225.225.278.144:2554,

                sftp.server.database.connection.driver.class.name: com.mysql.jdbc.Driver,

                sftp.server.database.connection.driver.location(s): [Server/sqljdbc\_4.0/enu/sqljdbc4.jar, C:/Oracle/product/11.2.0/Db\_1/jdbc/lib/ojdbc5.jar],

                sftp.server.database.max.total.connections: 10,

                sftp.server.database.max.wait.time: 500millis,

                sftp.server.database.validation.query: select count(\*) from hr\_table,

                sftp.server.database.user: #Abcd1234,

                sftp.server.database.password: xxxxxxx,

                sftp.server.filter.DBConnectionNames (List): [my\_connectin\_name, other\_conncetion\_name],

                sftp.server.database.sql.query: select \* from hr\_table

        },

        Pull employee files from DB: {

                sftp.server.rule: sql,

                sftp.server.rule.name: Get employee data from DB,

                sftp.server.use.compression: true,

                sftp.server.database.connection.url: 225.225.278.144:2554,

                sftp.server.database.connection.driver.class.name: com.mysql.jdbc.Driver,

                sftp.server.database.connection.driver.location(s): [Server/sqljdbc\_4.0/enu/sqljdbc4.jar, C:/Oracle/product/11.2.0/Db\_1/jdbc/lib/ojdbc5.jar],

                sftp.server.database.max.total.connections: 10,

                sftp.server.database.max.wait.time: 500millis,

                sftp.server.database.validation.query: select count(\*) from employee\_table,

                sftp.server.database.user: #Abcd1234,

                sftp.server.database.password: xxxxxxx,

                sftp.server.filter.DBConnectionNames (List): [my\_connectin\_name, other\_conncetion\_name],

                sftp.server.database.sql.query: select \* from employee\_table

        }

}
```

# Meaning of each property explained below.

A little outdated as the ordering ofproperties may have changed.

**Server Instance Configuration**

// unique system generated id for each downloaded server. The ID is non-editable, encrypted and unique.

sftp.server.id

// fully qualified hostname or ip address of the server

sftp.server.hostname

//port that the server will listen on for file transfers

sftp.server.port

//username required to log into the server

sftp.server.username

//pasword required to log into the server

sftp.server.password

//fully qualified path of the private key file

sftp.server.private.key.path

//password for the private key

sftp.server.private.key.passphrase

//if supplied, the given file will be used as a host key. Otherwise no use host key would be used.

sftp.server.host.key

**Transaction rules**  Following rules (configurations) are sent to the server with each Push/Pull file(s) request.

//identity of the calling client

sftp.client.identity.id

// server heartbeat interval

sftp.server.heartbeat.interval=180sec

// options are pushfiles (Push remote files), pullfiles (Pull remote files), sql (select or update database records),

sftp.server.rule= pushfile /  pullfiles / sql

**Pull Files Configuration**

Pull file is when NIFI SFTP Client requests for files from the SFTP server. A SFTP client is capable of requesting multiple files of different types from different directories, hence multiple rules can be configured per server instance. Based on these rules files will be returned to the client. The rules are sent to the server as a encrypted JSON.

//User defined name of the rule. File(s) would be pushed or pulled from the server based on the configurations defined in each rule. Multiple rules can be sent in one request.

sftp.server.rule.name

// unique id for the calling client. The filed is mandatory to push files. The ID is non-editable, encrypted and unique. Multiple clients exist, this ID is used by the push file server to identify which client to push the files to.

sftp.client.id

// determines how long to wait between the push requests.

sftp.server.polling.interval=180sec

//if true, will traverse nested sub-directories and push files based on the filters specified.

sftp.server.push.recursively=true

// Indicates weather or not to ZLIB compression should be used when transferring files.

sftp.server.use.compression=false

// Indicates weather or not to use the order in which the files are naturally listed. Otherwise the order is not specified.

sftp.server.use.natural.ordering=true

**Pull File Filters**

Following configurations filters define the criteria based on which the files would be processed. Multiple rules can be set per request.

// directory path on the server from which to pull or push files

sftp.server.filter.dir.path

// will traverse nested sub-directories till the provided directory structure depth

sftp.server.filter.recursion.depth=10

//provides the exact name of the file to be fetched.

sftp.server.filter.filename

// provides a java regular expression for filtering file-names. If the filter is supplied, only files whose name match

// the regular expression will be fetched.

sftp.server.filter.file.regex

// only get files of certain type

sftp.server.filter.filetype

// read the file and only get files that have a certain content

sftp.server.filter.filecontent

// if specified, breaks the number of available, processable files into batches and transfers files in batches.

// Ensures that each batch does not process the files twice within a transaction.

sftp.server.filter.files.per.batch=10

// if specified, limits the number of files from the server in one transaction.

sftp.server.filter.total.files=500

//if true, files whose name begin with a dot will be ignored

sftp.server.filter.ignore.dotted.files=true

//if true, file on the remote system will be deleted once successfully transferred

sftp.server.filter.delete.original=true

//if true, the files once successfully transferred would be ignored in the next call.

sftp.server.filter.ignore.processed.file=true

**Push File Configurations**  SFTP server pushes files at specified time intervals from a remote system to the SFTP client based on the following configurations.

// unique id for the calling client. The filed is mandatory to push files. The ID is a encrypted and unique key that identifies the SFTP client to which the files will be sent. Multiple clients exist, this ID is used by the push file server to identify which client to push the files to. Same file can be sent to multiple clients.

sftp.client.id.list

//if true, will traverse nested sub-directories and push files based on the filters specified.

sftp.server.push.recursively=true

// Indicates weather or not to ZLIB compression should be used when transferring files.

sftp.server.use.compression=false

// Indicates weather or not to use the order in which the files are naturally listed. Otherwise the order is not specified.

sftp.server.use.natural.ordering=true

**Push File Filters**

Following configurations filters define the criteria based on which the files would be processed.

// directory path on the server from which to pull or push files

sftp.server.filter.dir.path

// will traverse nested sub-directories till the provided directory structure depth

sftp.server.filter.recursion.depth=10

//provides the exact name of the file to be fetched.

sftp.server.filter.filename

// provides a java regular expression for filtering file-names. If the filter is supplied, only files whose name match

// the regular expression will be fetched.

sftp.server.filter.file.regex

// only get files of certain type

sftp.server.filter.filetype

// read the file and only get files that have a certain content

sftp.server.filter.filecontent

// if specified, breaks the number of available, processable files into batches and transfers files in batches.

// Ensures that each batch does not process the files twice within a transaction.

sftp.server.filter.files.per.batch=10

// if specified, limits the number of files from the server in one transaction.

sftp.server.filter.total.files=500

//if true, files whose name begin with a dot will be ignored

sftp.server.filter.ignore.dotted.files=true

//if true, file on the remote system will be deleted once successfully transferred

sftp.server.filter.delete.original=true

//if true, the files once successfully transferred would be ignored in the next call.

sftp.server.filter.ignore.processed.file=true

**Database Request Configurations**  These configurations are used to run a select statement on a database add send the results as a Json file.

//Database connection url used to connect to a database. May contain, database system name, host,port,

// databse name, and some parameters. The exact syntax of your database connection URL is

// specified in your DBMS

sftp.server.database.connection.url

//Database driver class name

sftp.server.database.connection.driver.class.name

//Comma separated list of files/folders and/or URLs containing the driver jars and it&#39;s dependencies (if any)

// example: &#39;var/tmp/mariadb-java-client-1.7.7.jar&#39;

sftp.server.database.connection.driver.location(s)

//The maximum number of active connections to create a connection pool.

sftp.server.database.max.total.connections

//the maximum amount of time the pool would wait(when there are no connections in the pool) before returing a

// warning. -1 to wait indefinitely.

sftp.server.database.max.wait.time=500millis

//validation query used to validate connections before returning them. When connection is invalid, the connection

// is dropped and a new connection is returned from the pool.

sftp.server.database.validation.query

//database user name

sftp.server.database.user

//The password for the database user

sftp.server.database.password

//database configurations

sftp.server.filter.DBConnectionNames (List)

// SQL select query to be executed

sftp.server.database.sql.query
