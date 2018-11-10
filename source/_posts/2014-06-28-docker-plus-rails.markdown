---
layout: post
title: "docker + Rails"
date: 2014-06-28 04:55
comments: true
categories: rails, devops, linux, virtual machine
---
Heard of docker yet?  If not here's a brief summary, but first a warning: if you're not learning docker now, you're ignoring something *huge*. 
 Time to get up and start running!

<!-- more -->
Docker is a new kind of virtualization (a container, actually) which is bolted nicely to a git-style versioning system with a nice public/private distribution
 system (you 'pull' images from a public registry, make changes to the images, 'commit' those changes to a new image, and push that image to the registry. 
See?  Kinda git-like.) The docker images are structured in layers, and you only need to download the diffs if you have the 'earlier' layers.
And the docker images are stored in repositories called registries - there are both public and private registries.
So docker is kind of like an RPM, only with all the necessary infrastructure wrapped around it to make a whole VM.  

The benefit of this ought to be obvious, but a workflow nicely illustrates it: imagine if developers created code on their desktops, 
and the checked that code into a source control system, and then a build system like Jenkins grabbed the source code changes and built a package, 
and then ran the tests the developers had written, and if the tests passed, Jenkins wrapped the package in a docker. 
This docker could then be delivered back to the developer on their laptop, and to the certification system, and eventually (after several iterations 
of this loop) to production. At the moment the docker appears in production, the developers on their laptops, and the QA guys in certification and 
integration, and the ops guys in pilot and production *are all running the same exact same docker container*.  The exact same "thing" is running everywhere.  There is zero chance the docker has been inadvertently altered in this workflow. That's the beauty of the system.

With all this handwaving, skepticism ought to be bubbling to the surface right about now. Understandable, given the preponderance of former saviors in
the DevOps space offering this sort of stuff.  But docker is for real.  Each and every part of the workflow above is relatively easy to build, works well, 
and integrates nicely into existing systems.  I'll use a Rails app to illustrate the process.

## Start with Source Code
On your laptop, build a ruby on rails environment as simply as you can, and keep track of the way you did it. 
Your system probably relies on a database backend - we'll create a docker for that database server later on. 
If your rails app relies on redis, or some other service, save that as well - we'll build another docker for it. 
We'll stick to building the rails docker, but the others ought to be easy to imagine from this description. 
When you're done coding (and writing tests), you're ready to proceed. Check your code into source control.

## On the Build System
On the build system, instruct the build engine to export your code, test it, package it as necessary, and wrap it in a tarball (as an example). 
You could also wrap it in an RPM, which would be nice for future-proofing your system. 
Run your unit tests to eliminate dumb mistakes.  Next we can roll a docker for integration tests.

## A Docker needs a Dockerfile
You build a docker by taking a base image (which is a very basic linux install, for example) and adding things to it to build the minimally functional 
system that runs your application. The steps required to do that last part are expressed in a Dockerfile. 
The Dockerfile is a very simple text file.  Here's an example Dockerfile, which creates a container which runs VNC and firefox:

```
FROM ubuntu
RUN apt-get update
RUN apt-get install -y x11vnc xvfb firefox
RUN mkdir /.vnc
# Setup a password
RUN x11vnc -storepasswd 1234 ~/.vnc/passwd
# Autostart firefox (might not be the best way, but it does the trick)
RUN bash -c 'echo "firefox" >> /.bashrc'

EXPOSE 5900
CMD    ["x11vnc", "-forever", "-usepw", "-create"]
```
  
That file says: start with ubuntu (a super-simple base image).  Next, update the packages and install vnc and firefox.
Set the vnc password and add firefox to bashrc so it always starts when you fire up a shell.
Tell docker that this image listens on 5900, and finally setup the docker so that it always only runs vnc when you connect to it. 

For our rails app, the dockerfile needs to start with a base image of linux which is in prod, load ruby, load rails and the necessary gems,
load passenger and apache.  Next, setup apache as a service and make sure it starts when the docker starts. 
Next, copy your source code into the docker, setup a virtual host in apache, and so forth. 
If your source code is a tarball, you can tell docker to extract the tarball onto your docker's filesystem and create the necessary directories underneath. 
Remember earlier when we kept track of how we built our rails system?  We can use that information now.

## Test the docker
In order to run automated testing using the docker - you need the docker system running in the build environment. 
At the end of a successful build, after you've made a docker, you publish it to a local registry and fire up tests which download the new docker 
and test against it.  If tests pass, you can publish the docker to a registry in a wider sphere.

## Cert and the docker
The docker should now be in a registry visible by the cert instances. They can grab it and run it as a daemon. Remember that when the cert instances download
the docker, they only have to download diffs from the previous images they have already used. This makes this sucker incredibly fast. 
If cert is satisfied with the docker, they can promote the docker to a registry with production visibility.

## Prod and the docker
The dockers that appear in prod are *identical* to the dockers that first ran in test on the build servers. 
That's all that needs to be said. And since dockers are so easy to deploy (fast!), they are painless to deploy in load-balancing and capacity 
managing systems. If you need more VM's to manage traffic, it takes 15 minutes per VM - if you need more dockers, you get a new one in less than 2 minutes. 
Seems hard to believe until you see it working.  Pretty freaking amazing.  

## What About Rails?
One of the thorniest issues with learning and using Ruby on Rails is installation and maintenance of ruby and the gems rails requires.
It's a pain because it's almost always a bolt-on to current technology - are there any modern linux distro's which have a decent modern ruby? 
For a comparison, how does one setup Erlang for programming and learning? Well, for centos, you issue the following commands: 
`rpm -Uvh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm` and `yum -y install erlang`. 
But for ruby and for rails, the list of commands required is as long as your arm (provided, you play for the NBA), and fraught with difficulty.
People have come up with lots of ways to simplify the process, but in the end you have to *maintain* that mess, and it's a pretty tough task.
But docker could make this easier.

Suppose you found a centos docker image which contained rails, like this one:

[docker-rails](https://registry.hub.docker.com/u/lgsd/docker-rails/) 

It looks like it has everything you need to start developing in rails. 
Here's how simple it is to use it: `docker run -i -p 3000:3000 -t lgsd/docker-rails` - 1 command. 
It redirects port 3000 to your host OS so you can fire up your browser on http://localhost:3000 and see your app. Wow. 
And remember, docker supports volumes, so you can keep your source code out of the docker, and include it in a volume so the docker doesn't need to be 
rebuilt when ever the source code changes. This is perfect, in my mind.

The benefit of this approach is that it treats the rails framework as an appliance - you turn it on, it works, you turn it off it does nothing. 
No user servicible parts inside. This leaves you, the rails developer, with the single task of writing great ruby code.

## Conclusion
Docker is damn disruptive. It supplants a heck of a lot of stuff you commonly use today. Strictly speaking, you don't need VMWare, you don't need puppet 
or chef or ansible really. Deployment becomes "sitting on the target server, I pull the image diff, and then start the docker." 
This can be done with puppet, I guess.
