SMSCenter
=========

*Note: Here and everywhere in documentations SMS is a text message.*

SMSCenter is a set of applications to organize SMS distribution based on customer's clients database. The application set consists of:

* SMSCenter Server - the server part for receiving SMS messages from clients and sending them to other SMSC providers (including final cell operators)
* SMSCenter Server WebUI - Web application for administration and monitoring of the server
* SMSCenter Client - java stand-alone application that allows to connect to SMSCenter Server and send SMS messages, check message statuses.

SMSCenter Server
=========

Introduction
-------------------------

SMSCenter Server provides next features:

* Receiving SMS messages from registered users via SMPP and HTTP protocols. Other protocols can also be implemented. You have to implement org.eminmamedov.smscenter.receivers.SmsReceiver interface only.
* Sending the received SMS messages to other SMSC via SMPP protocol. Other protocols can be implemented also. You have to implement org.eminmamedov.smscenter.senders.SmsSender interface only.
* Updating the status of SMS messages
* Splitting large SMS messages
* Support multiple SMSC (Channels). For each SMS corresponding SMSC will be choosen depending on the specified sender.

Information about Users, Channels (SMSC providers), Registered Senders and received SMS messages is stored in database.

Requirements
-------------------------

* JRE 1.6+
* MySQL 5.1 (in general, any other RDBMS can be used)

Installation
-------------------------

* Download the latest release (zip, tar.gz, tar.bz2 are available)
* Unpack into any directory
* Connect to MySQL and create schema `smscenter`
* Connect to schema and run script `db/create_script_mysql.sql`
* For Windows users execute `bin/smscenter.bat install` to create new service

Start/Stop Service
-------------------------

* For Windows users:
  After installation "SMSCenter Server" Service will be created so you can start/stop SMSCenter Server via services.msc. Or use `bin/smscenter.bat start` to start service and `bin/smscenter.bat stop` to stop service.
* For Unix users:
  Use `bin/smscenter start` to start application and `bin/smscenter stop` to stop application 

SMSCenter Client
=========

SMSCenter Client is still under construction. I didn't create repository for it yet. But you can implement your own client because SMSCenter Server can receive messages via HTTP. XML is used data representation protocol.

HTTP Request Example:

```
<?xml version="1.0" encoding="UTF-8"?>
<request>
    <login>test</login>
    <password>test</password>
    <sendMessages>
        <message>
            <clientId>1</clientId>
            <receiver>79853869839</receiver>
            <text>Test message</text>
        </message>
    </sendMessages>
    <checkMessages>
        <message>
            <serverId>1</serverId>
        </message>
    </checkMessages>
</request>
```

HTTP Response Example:

```
<?xml version="1.0" encoding="UTF-8"?>
<response>
    <sendMessages>
        <message>
            <clientId>1</clientId>
            <serverId>1</serverId>
            <text>Test message</text>
            <groupIndex>0</groupIndex>
            <groupCount>0</groupCount>
        </message>
    </sendMessages>
    <checkMessages>
        <message>
            <clientId>3</clientId>
            <serverId>1</serverId>
            <status>1</status>
        </message>
    </checkMessages>
</response>
```
