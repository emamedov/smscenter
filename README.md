SMSCenter
=========

Note: Here and everywhere in documentations SMS is a text message.

SMSCenter is a set of applications to organize SMS distribution based on customer's clients database. The application set consists of:

* SMSCenter Server - the server part for receiving SMS messages from clients and sending them to other SMSC providers (including final cell operators)
* SMSCenter Server WebUI - Web application for administration and monitoring of the server

SMSCenter Server
=========

SMSCenter Server provides next features:

* Receiving SMS messages from registered users via SMPP and HTTP protocols. Other protocols can also be implemented. You have to implement org.eminmamedov.smscenter.receivers.SmsReceiver interface only.
* Sending the received SMS messages to other SMSC via SMPP protocol. Other protocols can be implemented also. You have to implement org.eminmamedov.smscenter.senders.SmsSender interface only.
* Updating the status of SMS messages
* Splitting large SMS messages
* Support multiple SMSC (Channels). For each SMS corresponding SMSC will be choosen depending on the specified sender.

Information about Users, Channels (SMSC providers), Registered Senders and received SMS messages is stored in database.
