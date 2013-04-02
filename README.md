
# Groxy

A JSON API proxy for talking to the Gmail API.  Allows using persistent IMAP connections
for non-resident scripting languages.

## Usage

Clone the repo and start the application.

```
lein run
```

You cna then browse the web application at:

```
http://localhost:4545
```

## Endpoints

### API Status

```
GET /api
```

### Inbox List

```
GET /api/inbox
```

With parameters:

```
email - Your Gmail email address
access_token - A valid OAuth access token
```

## Access Tokens

Obtaining and refreshing access tokens is not handled by Groxy, you need to do this yourself.
If you make a request with an invalid access token you'll receive a 403 response, which is 
your notification to refresh your token and try again.

