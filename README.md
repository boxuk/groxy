
# Groxy

A JSON API proxy for talking to Gmail.  Allows using persistent IMAP connections for non-resident scripting languages.

There is [literate documentation](http://boxuk.github.com/groxy/) available.

### Disclaimer

At the moment it _seems_ this only works with Gmail accounts that are part of a
Google Apps for Business domain.  But I can't find any documentation to indicate that
this shouldn't work with normal Gmail accounts.  So YMMV.

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

### API Status

```
GET /api
```

### Searching

```
GET /api/messages
```

With parameters:

```
email - Your Gmail email address
access_token - A valid OAuth access token
query - Your query string (supports X-GM-RAW extension)
```

### Messages

```
GET /api/messages/:id
```

Where _:id_ is the ID of the message you want to fetch.  You will also need 
specify the specified parameters.

```
email - Your Gmail email address
access_token - A valid OAuth access token
```

## Access Tokens

Obtaining and refreshing access tokens is not handled by Groxy, you need to do this yourself.
If you make a request with an invalid access token you'll receive a 403 response, which is 
your notification to refresh your token and try again.

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

