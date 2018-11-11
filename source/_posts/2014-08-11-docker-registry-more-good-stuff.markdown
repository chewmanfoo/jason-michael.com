---
layout: post
title: "Docker registry: more good stuff"
date: 2014-08-11 16:40
comments: true
categories: docker, registry, infrastructure, containers 
---
If you've read my earlier posts, you know I'm learning docker and using private registries.
It's a battle because it appears as though docker truly intends for you to post your private corporate images to their public docker [hub](https://hub.docker.com/) (similar to GitHub), albeit in a private registry (for a fee).
If I lived in such a world and worked for such a company, all would be excellent.  But I don't.  I venture to guess, few people do.
Consequently, I have to maintain a private registry, and deal with all the issues of being a stepchild.  But enough whining.
<!-- more -->
So I have it working now.  I can build a docker image on one VM, push it to a private registry on another VM, and pull it down and run it on a third VM (my laptop).
After I had it all running I started wondering what other goodies a consumer of such a tool would want to use.  I realized that a web UI would be sweet.
I found one here:  [link](https://github.com/atc-/docker-registry-web).  This isn't necessarily the end-all, be-all of registry UI's but it works... kinda.
I have it running on the same VM as the registry, using the following script:
```
#!/bin/bash

echo "loading registry..."
$sudo docker run -d -e STORAGE_PATH=/login/sg218049/docker-store -p 5000:5000 registry

sleep 10

echo "loading registry ui..."
$sudo docker run -p 8080:8080 -e REG1=http://0.0.0.0:5000/v1/ atcol/docker-registry-ui
```
It works to the extent that it shows me in the browser the local registry and details about it.  However, when I move into the 'Images' context of the webapp, it fails with a 500 error:
```
Error 500: Internal Server Error
URI
/search/search
Class
java.net.ConnectException
Message
Connection refused
```

More to come when I figure out what is wrong.  
