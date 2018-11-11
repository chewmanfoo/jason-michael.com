---
layout: post
title: "AWSome Lambda!"
date: 2016-09-03 17:28
comments: true
categories: 
---
I have been working in DevOps the past year, even if my blog has given no indication that I've been doing anything at all.
Lately, I've been working on building a monitoring system for a coporate IT wad of crap (mostly Microsoft bullshit), where
you can't get TCP ingress or egress without a court order, so you gotta be creative.  Essentially the system is a client-server
architecture where (normally) a client sits on a server, runs a pre-defined list of checks on the server (like disc space
checks or memory checks), and reports back to the server via a TCP connection (https POST).  But since we can't get the client
visibility to the server in a different network, we need some way of communicating.  These servers are managed by HPE, and let
me tell you, HPE has devolved a lot since Hewlett and Packard were silly-walking around silicon valley with their cloth shirts
with dark ties and slacks and matching pocket protectors and slide rules. 
<!-- more -->
Anyway HPE sucks.  Back to the problem - how can you communicate with a monitoring serve when you can't see it on the network?
We solved the problem by compiling an email on the server with all the checks and results in the body of the email, and sending
that email to AWS SS, which is bolted on to a Lambda function that detects the email, parses the content, then opens a TCP connection
to the monitoring server to report the results.  It works beautifully.

## Problems with using emails as a conduit for monitoring
First and foremost, if you're monitoring an email system by sending an email, you're holding it wrong.  People in IT had an odd
reationship with email (Exchange community, I'll be looking at you for the rest of this rant).  Email is never treated like a critical
system, rarely do people build real high-availability into their mail systems, but when email is not working, everybody has their
collective hair on fire.  Fun.

## Problems with Lambda
Java, Javascript and Python.  That's it.  No ruby.  Why no ruby?  Because, reasons.  Seriously, folks, ruby is better.  Get your
head out of your asses.  Nowadays, choosing Java is like choosing Fortran. Yeah, I know, the Java language has evolved.  It's now
a modern language.  But the same can be said for Fortran.  Don't believe me?  Google that shit.  Fortran developers think Fortran
is a morden language, too.  Denial ain't just a river in Egypt.  Javascript is a hateful evil mess, with a gigantic community who
refuses to let it die.  In many ways it's very similar to Java.  And Python is a newer language built with a lot of bad ideas.
From a programmers perspective, and I am admitting ignorance here - I have no idea what's under the hood, ruby is so much nicer
to use.  A few examples I've discovered:

### Python
```
print "I got a key!: {}".format(key)

```

### ruby
```
p "I got a key!: #{key}"
```

See?  So many extra characters, so many needless syntax foibles.

<!-- see https://github.com/Shopify/liquid/wiki/Liquid-for-Designers for stuff 
# H1
## H2
[I'm an inline-style link](https://www.google.com)
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png 'Logo Title Text 1')
```javascript
var s = 'JavaScript syntax highlighting';
alert(s);
```
   * an unordered list item (note a newline is required before the list begins)
   1. an ordered list item
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
-->
