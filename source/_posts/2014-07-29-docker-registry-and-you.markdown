---
layout: post
title: "docker registry and you"
date: 2014-07-29 21:14
comments: true
categories: continuous integration, linux, enterprise, virtualization, containers, programming 
---
*Updated! - I got it working finally!*

This past week I've been trying to get a docker registry running inside our firewall to *just work*.  According to the [github page](https://github.com/docker/docker-registry), you can simply run

```
# docker run -p 5000:5000 registry
```

<!-- more -->

and you'll get the latest/greatest container running docker registry direct from the docker mind-hive.  As far as I see, there's no good reason not to do this, other than the fact that, at a whim, the docker brainiacs might change the tool so significantly that the next time you run it, upon grabbing the latest image from docker, it doesn't work any more.  But that's a risk which is rewarded by the fact that you'll constantly be running the latest.

It is better still if you save the storage used within that docker registry locally, by mounting a volume when you run the container, like so:

```
# docker run -p 5000:5000 -v /tmp/registry:/tmp/registry registry
```

This sticks any docker images which are saved to your registry in `/tmp/registry`, which is a clever idea since otherwise, upon killing the registry, your images will vanish.

Now, to make it work properly...

## Client #1: The Builder
I have a VM where I build docker images using a bash script which fetches tomcat, a jdk rpm and an app war file and some config files from dynamic, external sources.  I did this because integrating docker directly into Jenkins is taking some doing and more time than I have allotted for the project - but the [jenkins docker plugin](https://wiki.jenkins-ci.org/display/JENKINS/Docker+Plugin) will inevitably be next. This client is a rhel 6 box running docker version 1.0.0.  The script ends by building the docker image, tagging it and pushing it to the remote registry.  Here's how that looks:

```
The push refers to a repository [10.14.244.190:5000/cem_centos] (len: 1)
Sending image list
Pushing repository 10.14.244.190:5000/cem_centos (1 tags)
511136ea3c5a: Image successfully pushed
34e94e67e63a: Image successfully pushed
1a7dc42f78ba: Image successfully pushed
660c928cf15c: Image successfully pushed
2ed269308845: Image successfully pushed
821240aca174: Image successfully pushed
eeffcb636519: Image successfully pushed
e174cb83eb46: Image successfully pushed
c2fb605c483f: Image successfully pushed
58333ff1aec1: Image successfully pushed
9ba6f2eb6cd9: Pushing [==================================================>] 283.4 MB/283.4 MB
2014/07/29 15:21:01 Oops!  Unable to push to 10.14.244.190:5000.  Try docker push 10.14.244.190:5000/cem_centos later
```

So, there is sadness in the village, for the hero has fallen from his noble steed.

## Client #2: A laptop vagrant VM
I have a laptop running vagrant with a centos vm and docker 1.0.0.  I try to run the docker image I created and pushed above.  Here's how that goes:

```
[root@localhost ~]# docker run 10.14.244.190:5000/cem_centos -i /bin/bash
Unable to find image '10.14.244.190:5000/cem_centos' locally
Pulling repository 10.14.244.190:5000/cem_centos
2014/07/29 14:49:00 Tag latest not found in repository 10.14.244.190:5000/cem_centos
```

The people are in despair.

## On the registry
On the registry VM, I have docker version 1.0.0 running and the registry is started as shown above.  In the logs I see this:

```
151.193.220.29 - - [29/Jul/2014:21:46:10] "GET /v1/_ping HTTP/1.1" 200 4 "-" "Go 1.1 package http"
2014-07-29 21:46:10,933 INFO: 151.193.220.29 - - [29/Jul/2014:21:46:10] "GET /v1/_ping HTTP/1.1" 200 4 "-" "Go 1.1 package http"
2014-07-29 21:46:11,005 DEBUG: args = {}
151.193.220.27 - - [29/Jul/2014:21:46:11] "PUT /v1/repositories/cem_centos/ HTTP/1.1" 200 2 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,053 INFO: 151.193.220.27 - - [29/Jul/2014:21:46:11] "PUT /v1/repositories/cem_centos/ HTTP/1.1" 200 2 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,124 DEBUG: args = {'image_id': u'511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158'}
2014-07-29 21:46:11,126 DEBUG: api_error: Image not found
151.193.220.28 - - [29/Jul/2014:21:46:11] "GET /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,129 INFO: 151.193.220.28 - - [29/Jul/2014:21:46:11] "GET /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,192 DEBUG: args = {'image_id': u'511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158'}
151.193.220.29 - - [29/Jul/2014:21:46:11] "PUT /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,196 INFO: 151.193.220.29 - - [29/Jul/2014:21:46:11] "PUT /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,276 DEBUG: args = {'image_id': u'511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158'}
151.193.220.27 - - [29/Jul/2014:21:46:11] "PUT /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,280 INFO: 151.193.220.27 - - [29/Jul/2014:21:46:11] "PUT /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,344 DEBUG: args = {'image_id': u'511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158'}
151.193.220.28 - - [29/Jul/2014:21:46:11] "PUT /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,347 INFO: 151.193.220.28 - - [29/Jul/2014:21:46:11] "PUT /v1/images/511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,407 DEBUG: args = {'image_id': u'34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a'}
2014-07-29 21:46:11,409 DEBUG: api_error: Image not found
151.193.220.29 - - [29/Jul/2014:21:46:11] "GET /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,411 INFO: 151.193.220.29 - - [29/Jul/2014:21:46:11] "GET /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,478 DEBUG: args = {'image_id': u'34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a'}
151.193.220.27 - - [29/Jul/2014:21:46:11] "PUT /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,482 INFO: 151.193.220.27 - - [29/Jul/2014:21:46:11] "PUT /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,576 DEBUG: args = {'image_id': u'34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a'}
151.193.220.28 - - [29/Jul/2014:21:46:11] "PUT /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,580 INFO: 151.193.220.28 - - [29/Jul/2014:21:46:11] "PUT /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,650 DEBUG: args = {'image_id': u'34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a'}
151.193.220.29 - - [29/Jul/2014:21:46:11] "PUT /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,653 INFO: 151.193.220.29 - - [29/Jul/2014:21:46:11] "PUT /v1/images/34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,720 DEBUG: args = {'image_id': u'1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041'}
2014-07-29 21:46:11,722 DEBUG: api_error: Image not found
151.193.220.27 - - [29/Jul/2014:21:46:11] "GET /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,724 INFO: 151.193.220.27 - - [29/Jul/2014:21:46:11] "GET /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,792 DEBUG: args = {'image_id': u'1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041'}
151.193.220.28 - - [29/Jul/2014:21:46:11] "PUT /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:46:11,796 INFO: 151.193.220.28 - - [29/Jul/2014:21:46:11] "PUT /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:48:23,337 DEBUG: args = {'image_id': u'1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041'}
151.193.220.29 - - [29/Jul/2014:21:49:31] "PUT /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:31,837 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:31] "PUT /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:32,014 DEBUG: args = {'image_id': u'1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041'}
151.193.220.29 - - [29/Jul/2014:21:49:32] "PUT /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:32,018 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:32] "PUT /v1/images/1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:32,193 DEBUG: args = {'image_id': u'660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf'}
2014-07-29 21:49:32,195 DEBUG: api_error: Image not found
151.193.220.27 - - [29/Jul/2014:21:49:32] "GET /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:32,197 INFO: 151.193.220.27 - - [29/Jul/2014:21:49:32] "GET /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:32,281 DEBUG: args = {'image_id': u'660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf'}
151.193.220.28 - - [29/Jul/2014:21:49:32] "PUT /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:32,286 INFO: 151.193.220.28 - - [29/Jul/2014:21:49:32] "PUT /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:33,929 DEBUG: args = {'image_id': u'660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf'}
151.193.220.29 - - [29/Jul/2014:21:49:33] "PUT /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:33,935 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:33] "PUT /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:33,999 DEBUG: args = {'image_id': u'660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf'}
151.193.220.27 - - [29/Jul/2014:21:49:34] "PUT /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:34,002 INFO: 151.193.220.27 - - [29/Jul/2014:21:49:34] "PUT /v1/images/660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:34,069 DEBUG: args = {'image_id': u'2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305'}
2014-07-29 21:49:34,071 DEBUG: api_error: Image not found
151.193.220.28 - - [29/Jul/2014:21:49:34] "GET /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:34,073 INFO: 151.193.220.28 - - [29/Jul/2014:21:49:34] "GET /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:34,153 DEBUG: args = {'image_id': u'2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305'}
151.193.220.29 - - [29/Jul/2014:21:49:34] "PUT /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:34,157 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:34] "PUT /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,214 DEBUG: args = {'image_id': u'2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305'}
151.193.220.27 - - [29/Jul/2014:21:49:35] "PUT /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,218 INFO: 151.193.220.27 - - [29/Jul/2014:21:49:35] "PUT /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,283 DEBUG: args = {'image_id': u'2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305'}
151.193.220.28 - - [29/Jul/2014:21:49:35] "PUT /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,286 INFO: 151.193.220.28 - - [29/Jul/2014:21:49:35] "PUT /v1/images/2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,352 DEBUG: args = {'image_id': u'821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1'}
2014-07-29 21:49:35,354 DEBUG: api_error: Image not found
151.193.220.29 - - [29/Jul/2014:21:49:35] "GET /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,356 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:35] "GET /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,425 DEBUG: args = {'image_id': u'821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1'}
151.193.220.27 - - [29/Jul/2014:21:49:35] "PUT /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:35,429 INFO: 151.193.220.27 - - [29/Jul/2014:21:49:35] "PUT /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,498 DEBUG: args = {'image_id': u'821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1'}
151.193.220.28 - - [29/Jul/2014:21:49:36] "PUT /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,502 INFO: 151.193.220.28 - - [29/Jul/2014:21:49:36] "PUT /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,561 DEBUG: args = {'image_id': u'821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1'}
151.193.220.29 - - [29/Jul/2014:21:49:36] "PUT /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,564 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:36] "PUT /v1/images/821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,631 DEBUG: args = {'image_id': u'eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7'}
2014-07-29 21:49:36,633 DEBUG: api_error: Image not found
151.193.220.27 - - [29/Jul/2014:21:49:36] "GET /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,635 INFO: 151.193.220.27 - - [29/Jul/2014:21:49:36] "GET /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,712 DEBUG: args = {'image_id': u'eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7'}
151.193.220.28 - - [29/Jul/2014:21:49:36] "PUT /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:36,716 INFO: 151.193.220.28 - - [29/Jul/2014:21:49:36] "PUT /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,715 DEBUG: args = {'image_id': u'eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7'}
151.193.220.29 - - [29/Jul/2014:21:49:37] "PUT /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,719 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:37] "PUT /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,783 DEBUG: args = {'image_id': u'eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7'}
151.193.220.27 - - [29/Jul/2014:21:49:37] "PUT /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,786 INFO: 151.193.220.27 - - [29/Jul/2014:21:49:37] "PUT /v1/images/eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,851 DEBUG: args = {'image_id': u'e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3'}
2014-07-29 21:49:37,853 DEBUG: api_error: Image not found
151.193.220.28 - - [29/Jul/2014:21:49:37] "GET /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,855 INFO: 151.193.220.28 - - [29/Jul/2014:21:49:37] "GET /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,921 DEBUG: args = {'image_id': u'e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3'}
151.193.220.29 - - [29/Jul/2014:21:49:37] "PUT /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:49:37,925 INFO: 151.193.220.29 - - [29/Jul/2014:21:49:37] "PUT /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:35,058 DEBUG: args = {'image_id': u'e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3'}
151.193.220.27 - - [29/Jul/2014:21:50:40] "PUT /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,390 INFO: 151.193.220.27 - - [29/Jul/2014:21:50:40] "PUT /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,455 DEBUG: args = {'image_id': u'e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3'}
151.193.220.29 - - [29/Jul/2014:21:50:40] "PUT /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,459 INFO: 151.193.220.29 - - [29/Jul/2014:21:50:40] "PUT /v1/images/e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,521 DEBUG: args = {'image_id': u'c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5'}
2014-07-29 21:50:40,523 DEBUG: api_error: Image not found
151.193.220.27 - - [29/Jul/2014:21:50:40] "GET /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,525 INFO: 151.193.220.27 - - [29/Jul/2014:21:50:40] "GET /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,590 DEBUG: args = {'image_id': u'c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5'}
151.193.220.28 - - [29/Jul/2014:21:50:40] "PUT /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:40,594 INFO: 151.193.220.28 - - [29/Jul/2014:21:50:40] "PUT /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,775 DEBUG: args = {'image_id': u'c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5'}
151.193.220.29 - - [29/Jul/2014:21:50:41] "PUT /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,779 INFO: 151.193.220.29 - - [29/Jul/2014:21:50:41] "PUT /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/layer HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,837 DEBUG: args = {'image_id': u'c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5'}
151.193.220.27 - - [29/Jul/2014:21:50:41] "PUT /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,840 INFO: 151.193.220.27 - - [29/Jul/2014:21:50:41] "PUT /v1/images/c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5/checksum HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,897 DEBUG: args = {'image_id': u'58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde'}
2014-07-29 21:50:41,898 DEBUG: api_error: Image not found
151.193.220.28 - - [29/Jul/2014:21:50:41] "GET /v1/images/58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,900 INFO: 151.193.220.28 - - [29/Jul/2014:21:50:41] "GET /v1/images/58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde/json HTTP/1.1" 404 28 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,976 DEBUG: args = {'image_id': u'58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde'}
151.193.220.29 - - [29/Jul/2014:21:50:41] "PUT /v1/images/58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-07-29 21:50:41,980 INFO: 151.193.220.29 - - [29/Jul/2014:21:50:41] "PUT /v1/images/58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde/json HTTP/1.1" 200 4 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
```

Considering the client shows this when I push:

```
[root@ltxl0787 ~]# docker push 10.14.244.190:5000/cem_centos
The push refers to a repository [10.14.244.190:5000/cem_centos] (len: 1)
Sending image list
Pushing repository 10.14.244.190:5000/cem_centos (1 tags)
511136ea3c5a: Image successfully pushed
34e94e67e63a: Image successfully pushed
1a7dc42f78ba: Image successfully pushed
660c928cf15c: Image successfully pushed
2ed269308845: Image successfully pushed
821240aca174: Image successfully pushed
eeffcb636519: Image successfully pushed
e174cb83eb46: Image successfully pushed
c2fb605c483f: Image successfully pushed
58333ff1aec1: Pushing [==================================================>] 126.9 MB/126.9 MB
```

and returns exit code: 

```
2014/07/29 16:54:00 [root@ltxl0787 ~]# echo $?
1
```

It's all bad.

So what's wrong?  There's an awful lot of gears turning and smoke billowing for nothing to be accomplished!  On the registry server, I see this in `/tmp/registry`:

```
[root@ltxl0905 ~]# tree /tmp/registry
/tmp/registry
|-- images
|   |-- 1a7dc42f78ba213ec1ac5cd04930011334536214ad26c8000f1eec72e302c041
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- 2ed26930884555c7315b76ec1fefc629205c24934cb4e82d494fb6696f2f8305
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- 34e94e67e63a0f079d9336b3c2a52e814d138e5b3f1f614a0cfe273814ed7c0a
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- 511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- 58333ff1aec18e4543ba20d0b4b6e273d315643029975d909e117d7c6aa01cde
|   |   |-- ancestry
|   |   |-- _inprogress
|   |   `-- json
|   |-- 660c928cf15c4bb80f14b5cb54b532d3af294ead76feff019ad1980b73a76cdf
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- 821240aca17444a5e2fd935833220530390b82fee173e4f4547a4a610eaeb6a1
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- c2fb605c483fe1645bc7c3fc1a99a3d83823b05bcffbc34ca7bb31442c6385a5
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   |-- e174cb83eb4694fdee7e56a7e3c182d74ebb3c08eaa5e04b0ab29c252c0816c3
|   |   |-- ancestry
|   |   |-- _checksum
|   |   |-- json
|   |   `-- layer
|   `-- eeffcb636519b1dc518c2ac1f521f87b01cb75a36e0121baf64e087c67304aa7
|       |-- ancestry
|       |-- _checksum
|       |-- json
|       `-- layer
`-- repositories
    `-- library
        `-- cem_centos
            `-- _index_images

14 directories, 40 files
```
stuff is being saved to the registry.

Again, I try on the laptop to pull the image:

```
[root@localhost ~]# docker run 10.14.244.190:5000/cem_centos -i /bin/bash
Unable to find image '10.14.244.190:5000/cem_centos' locally
2014/07/29 16:58:16 Error: <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<title>404 Not Found</title>
<h1>Not Found</h1>
<p>The requested URL was not found on the server.  If you entered the URL manually please check your spelling and try again.</p>
```

and I see this on the registry logs:

```
2014-07-29 21:58:21,069 INFO: 10.16.55.114 - - [29/Jul/2014:21:58:21] "POST /images/create?fromImage=10.14.244.190%3A5000%2Fcem_centos&tag=latest HTTP/1.1" 404 233 "-" "Docker-Client/1.0.0"
```

What the hell is going on?

More to follow as soon as I get my head outta my ass.

## Resolved!
Durrrrr.

Apparently, assuming `docker run` automagically works for a non-local image housed on a private registry is _not so much_.  When I tried `docker pull` followed by `docker run`, it works every time.

## Summary

### on the build server:
(here, 10.14.244.190 is my host running the registry container, listening on port 5000)
```
$ sudo docker build --force-rm=true -t $DOCKER_IMAGE_NAME .
$ sudo docker tag cem_base_centos 10.14.244.190:5000/cem_base_centos
$ sudo docker push 10.14.244.190:5000/cem_base_centos
```

### on the registry server:
I see a lot in the logs:
```
2014-08-06 19:32:51,739 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/_ping HTTP/1.1" 200 4 "-" "Go 1.1 package http"
2014-08-06 19:32:51,752 DEBUG: args = {}
10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/repositories/cem_base_centos/images HTTP/1.1" 200 2508 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,755 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/repositories/cem_base_centos/images HTTP/1.1" 200 2508 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,768 DEBUG: args = {}
2014-08-06 19:32:51,769 DEBUG: [get_tags] namespace=library; repository=cem_base_centos
10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/repositories/library/cem_base_centos/tags HTTP/1.1" 200 78 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,773 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/repositories/library/cem_base_centos/tags HTTP/1.1" 200 78 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,786 DEBUG: args = {'image_id': u'6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55'}
10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/ancestry HTTP/1.1" 200 1224 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,789 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/ancestry HTTP/1.1" 200 1224 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,801 DEBUG: args = {'image_id': u'14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79'}
10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/images/14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79/json HTTP/1.1" 200 1522 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,804 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/images/14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79/json HTTP/1.1" 200 1522 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,817 DEBUG: args = {'image_id': u'14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79'}
10.16.55.118 - - [06/Aug/2014:19:32:51] "HEAD /v1/images/14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,820 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "HEAD /v1/images/14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,980 DEBUG: args = {'image_id': u'14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79'}
10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/images/14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:51,983 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:51] "GET /v1/images/14e2a7a71d114c03205642ba751762be52de842039a931e6695eed71da674c79/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:53,569 DEBUG: args = {'image_id': u'118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372'}
10.16.55.118 - - [06/Aug/2014:19:32:53] "GET /v1/images/118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372/json HTTP/1.1" 200 1498 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:53,573 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:53] "GET /v1/images/118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372/json HTTP/1.1" 200 1498 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:53,584 DEBUG: args = {'image_id': u'118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372'}
10.16.55.118 - - [06/Aug/2014:19:32:53] "HEAD /v1/images/118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372/layer HTTP/1.1" 200 260 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:53,588 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:53] "HEAD /v1/images/118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372/layer HTTP/1.1" 200 260 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:53,679 DEBUG: args = {'image_id': u'118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372'}
10.16.55.118 - - [06/Aug/2014:19:32:53] "GET /v1/images/118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372/layer HTTP/1.1" 200 260 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:53,684 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:53] "GET /v1/images/118a953e3c1a0a729eaf8743489510a3599a0931a58a0c746d9f598c3d8e2372/layer HTTP/1.1" 200 260 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:55,071 DEBUG: args = {'image_id': u'daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103'}
10.16.55.118 - - [06/Aug/2014:19:32:55] "GET /v1/images/daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103/json HTTP/1.1" 200 1480 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:55,076 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:55] "GET /v1/images/daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103/json HTTP/1.1" 200 1480 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:55,087 DEBUG: args = {'image_id': u'daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103'}
10.16.55.118 - - [06/Aug/2014:19:32:55] "HEAD /v1/images/daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:55,089 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:55] "HEAD /v1/images/daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:55,140 DEBUG: args = {'image_id': u'daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103'}
10.16.55.118 - - [06/Aug/2014:19:32:55] "GET /v1/images/daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:55,144 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:55] "GET /v1/images/daa07f6d74d7f43f3d19fa97853bbf946269197a0383eae3efc03667e178a103/layer HTTP/1.1" 200 23 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:56,201 DEBUG: args = {'image_id': u'06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9'}
10.16.55.118 - - [06/Aug/2014:19:32:56] "GET /v1/images/06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9/json HTTP/1.1" 200 1485 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:56,205 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:56] "GET /v1/images/06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9/json HTTP/1.1" 200 1485 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:56,216 DEBUG: args = {'image_id': u'06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9'}
10.16.55.118 - - [06/Aug/2014:19:32:56] "HEAD /v1/images/06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9/layer HTTP/1.1" 200 143 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:56,218 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:56] "HEAD /v1/images/06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9/layer HTTP/1.1" 200 143 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:56,282 DEBUG: args = {'image_id': u'06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9'}
10.16.55.118 - - [06/Aug/2014:19:32:56] "GET /v1/images/06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9/layer HTTP/1.1" 200 143 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:56,285 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:56] "GET /v1/images/06451776e44c71d98a8bb966a1aa8048c3cced3dc3de27ff042d7c3be9d940c9/layer HTTP/1.1" 200 143 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:57,371 DEBUG: args = {'image_id': u'3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98'}
10.16.55.118 - - [06/Aug/2014:19:32:57] "GET /v1/images/3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98/json HTTP/1.1" 200 1540 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:57,375 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:57] "GET /v1/images/3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98/json HTTP/1.1" 200 1540 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:57,385 DEBUG: args = {'image_id': u'3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98'}
10.16.55.118 - - [06/Aug/2014:19:32:57] "HEAD /v1/images/3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98/layer HTTP/1.1" 200 8240325 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:57,388 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:57] "HEAD /v1/images/3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98/layer HTTP/1.1" 200 8240325 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:57,516 DEBUG: args = {'image_id': u'3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98'}
10.16.55.118 - - [06/Aug/2014:19:32:58] "GET /v1/images/3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98/layer HTTP/1.1" 200 8240325 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:32:58,527 INFO: 10.16.55.118 - - [06/Aug/2014:19:32:58] "GET /v1/images/3c6c6b3097f31b84400363e78e85092d876928ef8518e18ff0393fbbee8eee98/layer HTTP/1.1" 200 8240325 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:02,528 DEBUG: args = {'image_id': u'7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f'}
10.16.55.118 - - [06/Aug/2014:19:33:02] "GET /v1/images/7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f/json HTTP/1.1" 200 1528 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:02,532 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:02] "GET /v1/images/7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f/json HTTP/1.1" 200 1528 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:02,543 DEBUG: args = {'image_id': u'7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f'}
10.16.55.118 - - [06/Aug/2014:19:33:02] "HEAD /v1/images/7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f/layer HTTP/1.1" 200 158 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:02,546 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:02] "HEAD /v1/images/7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f/layer HTTP/1.1" 200 158 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:02,630 DEBUG: args = {'image_id': u'7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f'}
10.16.55.118 - - [06/Aug/2014:19:33:02] "GET /v1/images/7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f/layer HTTP/1.1" 200 158 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:02,633 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:02] "GET /v1/images/7565fa083a7e6bca7674fbb1dbce2c3fa405cc4bccbe16a4f07d4b85b0e9ad5f/layer HTTP/1.1" 200 158 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:03,888 DEBUG: args = {'image_id': u'14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0'}
10.16.55.118 - - [06/Aug/2014:19:33:03] "GET /v1/images/14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0/json HTTP/1.1" 200 1510 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:03,892 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:03] "GET /v1/images/14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0/json HTTP/1.1" 200 1510 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:03,902 DEBUG: args = {'image_id': u'14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0'}
10.16.55.118 - - [06/Aug/2014:19:33:03] "HEAD /v1/images/14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0/layer HTTP/1.1" 200 126212387 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:03,905 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:03] "HEAD /v1/images/14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0/layer HTTP/1.1" 200 126212387 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:03,993 DEBUG: args = {'image_id': u'14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0'}
10.16.55.118 - - [06/Aug/2014:19:33:20] "GET /v1/images/14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0/layer HTTP/1.1" 200 126212387 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:20,463 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:20] "GET /v1/images/14e5075ee717c195b22afdaa73e28960ad845eaf00b738d7dd5ec5284eaacdb0/layer HTTP/1.1" 200 126212387 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:23,253 DEBUG: args = {'image_id': u'881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23'}
10.16.55.118 - - [06/Aug/2014:19:33:23] "GET /v1/images/881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23/json HTTP/1.1" 200 1504 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:23,257 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:23] "GET /v1/images/881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23/json HTTP/1.1" 200 1504 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:23,268 DEBUG: args = {'image_id': u'881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23'}
10.16.55.118 - - [06/Aug/2014:19:33:23] "HEAD /v1/images/881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23/layer HTTP/1.1" 200 148295327 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:23,271 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:23] "HEAD /v1/images/881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23/layer HTTP/1.1" 200 148295327 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:23,489 DEBUG: args = {'image_id': u'881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23'}
10.16.55.118 - - [06/Aug/2014:19:33:54] "GET /v1/images/881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23/layer HTTP/1.1" 200 148295327 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:54,268 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:54] "GET /v1/images/881a5384236e6847ead6ba81984efa5d8da80f984301a72f5ef817756259bf23/layer HTTP/1.1" 200 148295327 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:57,855 DEBUG: args = {'image_id': u'8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4'}
10.16.55.118 - - [06/Aug/2014:19:33:57] "GET /v1/images/8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4/json HTTP/1.1" 200 1493 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:57,860 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:57] "GET /v1/images/8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4/json HTTP/1.1" 200 1493 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:57,873 DEBUG: args = {'image_id': u'8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4'}
10.16.55.118 - - [06/Aug/2014:19:33:57] "HEAD /v1/images/8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4/layer HTTP/1.1" 200 150 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:57,876 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:57] "HEAD /v1/images/8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4/layer HTTP/1.1" 200 150 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:57,994 DEBUG: args = {'image_id': u'8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4'}
10.16.55.118 - - [06/Aug/2014:19:33:57] "GET /v1/images/8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4/layer HTTP/1.1" 200 150 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:57,997 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:57] "GET /v1/images/8d4e7651004c2eb91659dacbb93ea7c9d87566deaaf8b6151f93ee33bb5460d4/layer HTTP/1.1" 200 150 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:59,600 DEBUG: args = {'image_id': u'04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf'}
10.16.55.118 - - [06/Aug/2014:19:33:59] "GET /v1/images/04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf/json HTTP/1.1" 200 1473 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:59,603 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:59] "GET /v1/images/04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf/json HTTP/1.1" 200 1473 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:59,616 DEBUG: args = {'image_id': u'04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf'}
10.16.55.118 - - [06/Aug/2014:19:33:59] "HEAD /v1/images/04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf/layer HTTP/1.1" 200 132 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:59,619 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:59] "HEAD /v1/images/04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf/layer HTTP/1.1" 200 132 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:59,694 DEBUG: args = {'image_id': u'04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf'}
10.16.55.118 - - [06/Aug/2014:19:33:59] "GET /v1/images/04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf/layer HTTP/1.1" 200 132 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:33:59,697 INFO: 10.16.55.118 - - [06/Aug/2014:19:33:59] "GET /v1/images/04312f453ea28240e65cbe0b1c42eb52eef4f33c0e8162ffe653982081208abf/layer HTTP/1.1" 200 132 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:01,092 DEBUG: args = {'image_id': u'5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636'}
10.16.55.118 - - [06/Aug/2014:19:34:01] "GET /v1/images/5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636/json HTTP/1.1" 200 1531 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:01,095 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:01] "GET /v1/images/5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636/json HTTP/1.1" 200 1531 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:01,108 DEBUG: args = {'image_id': u'5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636'}
10.16.55.118 - - [06/Aug/2014:19:34:01] "HEAD /v1/images/5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636/layer HTTP/1.1" 200 2921 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:01,110 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:01] "HEAD /v1/images/5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636/layer HTTP/1.1" 200 2921 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:01,157 DEBUG: args = {'image_id': u'5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636'}
10.16.55.118 - - [06/Aug/2014:19:34:01] "GET /v1/images/5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636/layer HTTP/1.1" 200 2921 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:01,160 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:01] "GET /v1/images/5fe70cbd5dd292acc1e5c7408889cbad23a24f8088e3d57397a5f8be16297636/layer HTTP/1.1" 200 2921 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:02,588 DEBUG: args = {'image_id': u'3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c'}
10.16.55.118 - - [06/Aug/2014:19:34:02] "GET /v1/images/3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c/json HTTP/1.1" 200 1529 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:02,592 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:02] "GET /v1/images/3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c/json HTTP/1.1" 200 1529 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:02,604 DEBUG: args = {'image_id': u'3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c'}
10.16.55.118 - - [06/Aug/2014:19:34:02] "HEAD /v1/images/3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c/layer HTTP/1.1" 200 519 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:02,606 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:02] "HEAD /v1/images/3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c/layer HTTP/1.1" 200 519 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:02,658 DEBUG: args = {'image_id': u'3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c'}
10.16.55.118 - - [06/Aug/2014:19:34:02] "GET /v1/images/3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c/layer HTTP/1.1" 200 519 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:02,661 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:02] "GET /v1/images/3a0a75d181b760e00f3c21f9468de39a4e1f93aaddf03ebc9b1cd5b08f76232c/layer HTTP/1.1" 200 519 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:04,126 DEBUG: args = {'image_id': u'fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930'}
10.16.55.118 - - [06/Aug/2014:19:34:04] "GET /v1/images/fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930/json HTTP/1.1" 200 1498 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:04,129 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:04] "GET /v1/images/fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930/json HTTP/1.1" 200 1498 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:04,140 DEBUG: args = {'image_id': u'fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930'}
10.16.55.118 - - [06/Aug/2014:19:34:04] "HEAD /v1/images/fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930/layer HTTP/1.1" 200 324 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:04,142 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:04] "HEAD /v1/images/fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930/layer HTTP/1.1" 200 324 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:04,218 DEBUG: args = {'image_id': u'fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930'}
10.16.55.118 - - [06/Aug/2014:19:34:04] "GET /v1/images/fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930/layer HTTP/1.1" 200 324 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:04,220 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:04] "GET /v1/images/fd72025f9011b3e135706477fe813d4762d296c5dbdfebe90774e91df4b86930/layer HTTP/1.1" 200 324 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:05,777 DEBUG: args = {'image_id': u'07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4'}
10.16.55.118 - - [06/Aug/2014:19:34:05] "GET /v1/images/07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4/json HTTP/1.1" 200 1491 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:05,780 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:05] "GET /v1/images/07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4/json HTTP/1.1" 200 1491 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:05,791 DEBUG: args = {'image_id': u'07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4'}
10.16.55.118 - - [06/Aug/2014:19:34:05] "HEAD /v1/images/07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4/layer HTTP/1.1" 200 326 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:05,793 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:05] "HEAD /v1/images/07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4/layer HTTP/1.1" 200 326 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:05,832 DEBUG: args = {'image_id': u'07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4'}
10.16.55.118 - - [06/Aug/2014:19:34:05] "GET /v1/images/07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4/layer HTTP/1.1" 200 326 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:05,835 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:05] "GET /v1/images/07dc6b5a4481912d30b239e9c7fcecc133fde1f5548497eef4978586c3770dc4/layer HTTP/1.1" 200 326 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:07,555 DEBUG: args = {'image_id': u'6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55'}
10.16.55.118 - - [06/Aug/2014:19:34:07] "GET /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/json HTTP/1.1" 200 1484 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:07,559 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:07] "GET /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/json HTTP/1.1" 200 1484 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:07,570 DEBUG: args = {'image_id': u'6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55'}
10.16.55.118 - - [06/Aug/2014:19:34:07] "HEAD /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/layer HTTP/1.1" 200 3240 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:07,573 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:07] "HEAD /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/layer HTTP/1.1" 200 3240 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:07,790 DEBUG: args = {'image_id': u'6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55'}
10.16.55.118 - - [06/Aug/2014:19:34:07] "GET /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/layer HTTP/1.1" 200 3240 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
2014-08-06 19:34:07,793 INFO: 10.16.55.118 - - [06/Aug/2014:19:34:07] "GET /v1/images/6d41ffd37232a9aa0273a28ac9139e49611f7a7a0ae52b8471104ac1f53c1d55/layer HTTP/1.1" 200 3240 "-" "docker/1.0.0 go/go1.2.2 kernel/2.6.32-431.20.3.el6.x86_64 os/linux arch/amd64"
```

### on a laptop (a developer's workstation)
```
[root@localhost ~]# docker pull 10.14.244.190:5000/cem_base_centos
Pulling repository 10.14.244.190:5000/cem_base_centos
6d41ffd37232: Download complete
511136ea3c5a: Download complete
34e94e67e63a: Download complete
1a7dc42f78ba: Download complete
14e2a7a71d11: Download complete
118a953e3c1a: Download complete
daa07f6d74d7: Download complete
06451776e44c: Download complete
3c6c6b3097f3: Download complete
7565fa083a7e: Download complete
14e5075ee717: Download complete
881a5384236e: Download complete
8d4e7651004c: Download complete
04312f453ea2: Download complete
5fe70cbd5dd2: Download complete
3a0a75d181b7: Download complete
fd72025f9011: Download complete
07dc6b5a4481: Download complete
[root@localhost ~]# docker run -t -i 10.14.244.190:5000/cem_base_centos /bin/bash
bash-4.2# ls /apps/
cem
bash-4.2# exit
```
## It works.  It frikkin works.
What's next?  Need to add security on the registry image so that not everybody can pull (`docker login` will be required before a pull), and imagine what that will mean in a large multi-tier infrastructure.  Also need to work more on Jenkins to make the build create the images after the build is complete.  

<!-- see https://github.com/Shopify/liquid/wiki/Liquid-for-Designers for stuff 
# H1
## H2
[I'm an inline-style link](https://www.google.com)
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png 'Logo Title Text 1')
```javascript
var s = 'JavaScript syntax highlighting';
alert(s);
```
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
-->
