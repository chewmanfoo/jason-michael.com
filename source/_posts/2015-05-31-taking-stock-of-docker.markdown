---
layout: post
title: "taking stock of docker"
date: 2015-05-31 16:58
comments: true
categories: development, docker, devops 
---
I've been using docker for about 6 months now as a main go-to tool for developing Rails apps. In many ways, it rocks. In a few
ways it's a little lacking. I thought I'd write up my feelings on the subject at this time. I'm definately not an authority
on any subject I'm writing about here. But if you're a humble developer like me, you might find these thoughts useful. 
<!-- more -->
First some background.

## Why do I use docker?
I develop rails apps. Up until recently, I haven't owned a mac, but instead a lowly Windows PC. Rails runs most easily on a unix 
system. It's non-trivial to get a unix system running on Windows. Let's start there. I *used* to download CentOS boxes for
VirtualBox, setup virtual networking so that the image could get to the internet if needed, and so that my browser could get to
the images web interface ports. Add in the need for other's in my team to get to the web interfaces of the rails apps on the VM.
This setup task was *never easy*. When my laptop was using wireless networking, different network configuration was required than
when it was wired. And when it was both wired and wireless, another config was required. And when I was working from the office,
I had to deal with the corporate firewall and web proxy server. At home, things were easier, but notice that now we have _two
different configs_ - one for at home, and one for at work! Anyway, the requirements for network connectivity for running a rails 
app in dev is complex and often conflicts with security requirements and the complexities of windows networking in interesting
ways. 

I thought I could overcome this with *Vagrant*. But actually I discovered that adding Vagrant to the mix only adds another level
of complexity. Vagrant is supposed to be able to configure all the other stuff for you - setup networking on the VM, setup 
networking in VirtualBox. But it didn't always work in an easy way. Vagrant adds another DSL to the mix, with all of the standard
issues brought to the party by a new DSL. And vagrant's mapping to the filesystem on Windows is confusing and, in my opinion, 
silly. Vagrant maintains an index of boxes which you can launch. It gives you commands for working with those images. But since
the filesystem contains boxes, and the index references boxes, there's a subtle interplay between what is on the filesystem and
what is in the index. If you run out of space on your laptop, and go delete a VirtualBox box, Vagrant freaks out.
WHY?!?! Why oh why?!?! etc.

So I started using VM's on AWS and Digital Ocean instead. To be fair, I already had VM's everywhere, and I had already done a
considerable amount of work on them for Rails development. I dipped my toe in VM's on Windows using a variety of tools and I gave
up and went back to bare-metal VM's.

### Bare-metal VM's for Rails Development
Most things work great in the development lifecycle for rails apps on bare-metal VM's. I ssh into the box, open up 3 or 4 tabs
on my ssh client of choice (Zoc), and go to town. Sometimes I can even get a nice ANSI color output. Sometimes I can't.
Interacting with linux via a shell over ssh is different than interacting with linux in a console. I need to add in a lot of
shell customizations, tmux, etc. to really make the shell work for me. But I'm satisfied with what I have, and my favorite text
editor by far is still *vim*.  Suck it if you don't agree.

But life is not always peaches and cream. Suppose a rails app I am working with needs to test against an LDAP server, and I 
don't have easy access to an LDAP server I can bang on any time I want. My sysadmin friends tell me it's super easy to setup a 
private LDAP server, even one with SSL. But in my experience, it ain't. But this command: `docker run -d osixia/openldap` starts
an LDAP server in a docker container on my VM. Hmm 1 command vs an entire setup routine. I'll pick the 1 command. Essentially,
docker will allow me to treat system services as appliances rather than take on the role of running services myself (since I own
the VM and I have to be solely responsible for running the services securely myself). And I have experienced running services in
the cloud that get hacked and become the property of some jerk with a bag of tricks to steal other people's computing resources
(a mail server is a hard thing to run securely, unless you know what you're doing.)

## Things get a lot better with docker
Docker allows you to download and run services in containers. You can 'play stupid' about what's in the container if you want to.
You don't have to be an LDAP systems engineer to run LDAP for testing a rails app. Better, docker makes it very very (very) easy
to setup a rails enviroment.  Just follow these steps: [docker compose + rails](https://docs.docker.com/compose/rails/). There
are about 11 things you have to do on that page, but once you've done them, the next rails app you develop would require only a
few steps:

   1. create a new directory for your new project, cd in
   1. copy docker-compose.yml from a working project into this new directory
   1. copy Dockerfile from a working project into this new directory
   1. create a new Gemfile with `source 'httpd://rubygems.org"` and `gem 'rails', '4.2.0'`
   1. run `docker-compose run web rails new . --force --database=postgresql --skip-bundle`
   1. `sudo chown -R you:you *` because docker-compose makes everything root.root
   1. copy a good Gemfile from an existing working project (so you get all the bootstrap gems you always use)
   1. run `docker-compose build`  this is how the gems you need get into the container
   1. edit `config/database.yml` to point the rails database to the db container  
   1. run `docker-compose up` - it's running.  Check out http://what-ever-your-public-ip-is:3030.  See welcome to rails page.
   1. run `docker-compose run web rake db:create` to put the database from rails onto the database container
   1. run `bundle install` to create a *local rails development environment*.  The rails environment on the web container works great, but you also need a local environment for running `rails` commands.  Yeah it's weird - there's probably a cleaner way to do this, like having a global rails environment. The trick is that the gems installed on your system locally have to match the gems in the rails container.
   1. run `rails g...` and `docker-compose run web rake db:migrate` to create stuff and get it running

So yeah, that's really cool. RVM allows you to virtualize ruby and gemsets to keep projects separate. Docker allows you to keep
projects separate in their own chrooted environments. Very cool.

## Caveats: it ain't perfect
From the list above it may seem that 'they thought of everything' for running rails in docker.  But not so fast.  In the above
example, I've found that the gems inside the container often get out of sync with the gems in your local development environment
for some random reason I have yet to put my finger on. The clumsy solution I use is to delete `Gemfile.lock` if this ever happens.
This seems like it might be a bad idea, but whatevs.  Also note above, that you have to do `docker-compose build` if you ever
modify the Gemfile locally.  That's how gems get into the rails app container.

Other concerns: notice how I've innocently assumed that every docker container is holy and lilly-white containing no nefarious
code. Obviously you wouldn't download a docker image called 'please_rape_me' but it would be hard to know if the LDAP image I
recommended above is rooted to be a member of a botfarm using cycles to farm gold in WoW for a bunch of Czech thugs. And since
most docker image users like me would be using images in this way, docker needs to provide some way of ensuring the images are
_bone-fide good shit_. Also, are docker containers themselves secure? If you need to stand up a service that is visible to the
web, then you need to 1.) know that it's hard for a cracker to take over the service and 2.) if the service is compromised, it is impossible
to use that entry to harm the host system. So, if that invariant is true for VMWare or Xen or AWS's virtualization engine, can it
also be assumed to be true for docker?
 
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
