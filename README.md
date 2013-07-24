
# Groxy

A JSON API proxy for talking to Gmail.  Allows using persistent IMAP connections for non-resident scripting languages.

## Usage

Clone the repo and start the application.

```
lein run
```

You can then browse the web application at:

```
http://localhost:4545
```

![](http://github.com/boxuk/groxy/raw/master/images/www.png)

## Endpoints

All endpoints (except _/api_) require you send the following parameters:

```
email - Your Gmail email address
access_token - A valid OAuth access token
```

By default For the 'All Mail' folder Groxy uses '[Gmail]/All Mail' - which will be incorrect
for Google Mail accounts (and possibly others) which use '[Google Mail]/All Mail'.

To specify another folder to use the _folder_ parameter to give its name.  But be aware
that the message IDs returned are folder specific, so you'll need to use these consistently.

### API Status

```
GET /api
```

### Searching

The endpoint searches the 'All Mail' folder.

```
GET /api/messages
```

With parameters:

```
query - Your query string (supports X-GM-RAW extension)
folder - The name of the folder to use
```

### Single Messages

```
GET /api/messages/:messageid
```

With parameters:

```
messageid - The ID of the message to fetch
folder - The name of the folder to use
```

### Attachments

```
GET /api/messages/:messageid/attachments/:attachmentid
```

You will also need specify the specified parameters.  This will stream back the
raw content for the attachment.

```
messageid - The ID of the attachments message
attachmentid - The ID of the attachment to fetch
folder - The name of the folder to use
```

## Access Tokens

Obtaining and refreshing access tokens is not handled by Groxy, you need to do this yourself.
If you make a request with an invalid access token you'll receive a 403 response, which is 
your notification to refresh your token and try again.

To try Groxy out the easiest way is to get an access token from the [OAuth Playground](https://developers.google.com/oauthplayground/).
But don't select the Gmail service listed, enter the scope explicitly as...

```
https://mail.google.com/
```

## Configuration

Configuration is supplied via environment variables with the _GROXY_ prefix.

```
GROXY_PORT=4545
GROXY_LOGFILE="logs/access.log"
GROXY_LOGLEVEL="debug"
GROXY_LOGPATTERN="%d %m"
```

## Logging

Logging is configured to use log4j, and writes to the log file configured by _GROXY_LOGFILE_.

## Java Classes

The library uses some Java classes to implement the custom Gmail X-GM-RAW IMAP extension.
There are stored in _src/java_.  You can compile these on their own using Leiningen.

```
lein javac
```

They will be built automatically once though when running the project (or via REPL).

## Documentation

Documentation can be generated with Marginalia via Leiningen:

```
lein marg
```

And is then available in the _docs_ folder.

## Deployment

You can build a standalone WAR file for deployment using:

```
lein ring uberwar
```

This will build a version numbered file in _target/groxy-X.X.X.war_. Alternatively you can
build an executable:

```
lein bin
```

The task will output the name of the executable built, but it should be _target/groxy-VERSION_

## Box UK

 * [Packaging](docs/packaging.md)
 * [Statistics](docs/statistics.md)

