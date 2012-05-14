---
layout: post
title: "A New Erlang Project"
date: 2012-04-24 22:08
comments: true
categories: development, Erlang, programming, projects 
---
I have a new Erlang project which may pay $$$!  
<!-- more -->
I'll need to develop a RESTful interface for connecting with a cellular BSC (or *being* a cellular BSC), logging to syslog, and interfacing with a billing vendor, among other things.

### Links
 * [erlang + syslog](https://github.com/Vagabond/erlang-syslog)
 * [os_sup](http://www.erlang.org/doc/man/os_sup.html) for Solaris - OS can send messages to Erlang
 * [logging to syslog](http://erlang.2086793.n4.nabble.com/logging-to-syslog-td2099303.html)
 * [erlsyslog](https://github.com/lemenkov/erlsyslog) - another erlang syslog attempt - sends messages to UDP port 514
 * [ranch](https://github.com/extend/ranch) - a TCP port pool
 * [cowboy](https://github.com/extend/cowboy) - a web server and TCP port/connection pool

