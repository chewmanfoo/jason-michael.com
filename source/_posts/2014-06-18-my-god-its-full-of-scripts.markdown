---
layout: post
title: "My God - It's Full of Scripts"
date: 2014-06-18 21:42
comments: true
categories: engineering, CI, DevOps 
---
I witnessed a demo today for a deployment tool for tomcat apps.  I had seen snippets of it before in a few meetings but today I was able to really get into the design and function of the framework.  I felt like Dave on 2001: _"My God, it's full of scripts!"_
<!-- more -->

I'm not sure I'm qualified or have thought enough about it to provide a formal criticism of a framework which is essentially an orderly structure of folders and a few scripts that move stuff around within those folders and occasionally uses scp to move stuff around across hosts, but I will comment on bad smells in frameworks and tools I've seen over the years, in light of the framework I saw today.  Here are a few bad smells:

  * When your framework consists solely of the execution of scripts from the command line, bad smell
  * When your scripts aren't *idempotent*, and *atomic*, bad smell
  * When your scripts have multiple positional parameters which are not marked, bad smell
  * When your scripts prompt the user for a password into a remote host, several times (oftentimes the same password), bad smell
  * When your framework can _put_ but not _get_, or _get_ but not _put_ (you run a script to _get_ something from a remote host, but you can't also run a similar script on the remote host to _put_ that same something to the target), bad smell
  * When your framework touts having no agents, but instead requires the user to login to a host to do things an agent would do, bad smell
  * When critical parameters of the execution of your framework are hard-coded in your scripts (such as maven heap space), bad smell
  * When your framework is written to deploy software, but it only deploys one type of software or package, bad smell
  * When your framework deploys software to RHEL, but doesn't use RPM's, OMG bad smell
  * When your framework consists primarily of wrappers for an existing framework, but doesn't simplify or extend the functionality of that existing framework, bad smell
  * When your framework relys heavily on an existing framework, but doesn't fully utilize it---for example when your framework contains scripts to perform functions provided by the existing framework, but you didn't know or care that the existing framework already does this better than you do, bad smell 

It's tricky working in unix-land.  There are a lot of critics and a lot more cynics.  Tread lightly.
