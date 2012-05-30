---
layout: post
title: "Holy Mother of God - Amazon EC2 With No Pain"
date: 2012-05-21 11:23
comments: true
categories: Ruby on Rails, Provisioning, Programming 
---
This weekend I watched an eye-opening [railscast](http://railscasts.com/episodes/347-rubber-and-amazon-ec2) explaining how to use Rubber to deploy Rails apps to Amazon EC2.  Sounds pretty benign until you actually see what can be done.  I'll detail the whole experience in this post.

<!-- more -->

First, your Rails app has gotta be in the 3.x version.  It's really good if you have the understanding that your app will be deployed to Amazon EC2 from the git-go.  

Second, your gonna get a Ubuntu linux instance when you deploy - deal with it.  If you're a redhat bigot like me, you'll have to overcome the initial shock.  But magically, Rubber with Rails does to linux packages and server configuration what Rails does to programming (Convention over Configuration), so it's almost like Ubuntu isn't even there under the covers.  But it is...  (so essentially, there aren't _unlimited_ choices for server/database etc.)  But there are an awful lot of good default configurations for things like apache with Postgresql, apache with MySQL, ngix with Postgresql etc.  There's also some nosql configs - I'm interested in trying those out since Rubber seems to set up the connector and the database automagically.  Amazing.

Third, you want to be on a workstation with rails directly loaded on it. I typically ssh into my development workstation from my Windows PC (I know, Windows?  SHAME!!) for various reasons.  I can do everything with this setup and rubber except test the app in my browser (since my browser would have to be on the remote workstation.)  All lucky fools sporting the MacBook Pro or jokers with linux on the desktop have no worries.  Come to think of it, I really need to make the jump.

Here's a list of rubber/capistrano commands (you run all of these in the root of the application):

* `rubber vulcanize complete_passenger_postgresql` - this sets up your app for rubber.  You only run this once.
* `cap rubber:create_staging` - you run this to create a 'staging' environment - it runs for a very long time, and at the end you have a single instance on EC2 with database, web server, monit, and a boatload of other supporting services, which is visible from the internet and dns resolved from your desktop because rubber added the hostname from config/rubber/rubber.yml to your /etc/hosts file.
* `cap rubber:destroy_all` - tear down all instances you stood up.  Do this every night unless you want to pay Amazon for instances running overnight doing nothing.  The beauty of this is you don't have to keep them up - the rubber configuration files fully define the application environment in every way - you can stand up or tear down instances any time you want.
* `cap rubber:create # db01 db:primary=true` - create the primary database required by the application (this is the database server only, not the app server rails is running on.)  Here's where it starts getting amazing.
* `ALIAS=db02 ROLES=db cap rubber:create` - create a second database which is setup as a replicated slave to db01 above.  That's right, I said replicated slave.  All the replicaton configuration is done for you.  It's amazing.
* `ALIAS=app01 ROLES=app cap rubber:create` - create the app server the rails app runs on.  It's called app01 because you could create a hundred of them if you want to.  Call it app001 if you want to create a thousand.  Seriously.  But beware of hosting fees, dude.
* `ALIAS=web01 ROLES=web cap rubber:create` - create the load balancer that will balance http sessions between the app servers built so far.  If you build 1 then the cluster is a cluster of 1.  But if you build 100 then this load balancer distributes traffic across all 100 app servers in a load-balanced fashion.  I think it uses round-robin. 
* `cap rubber:bootstrap` - prep all these boxes for deployment.  It puts them on EC2 instances and preps them for running.
* `cap deploy:cold` - reboot all the instances and start them up
* `cap rubber:tail_logs` - tail all the logs on all the servers you've setup on the EC2 instances.  This brings all the logs you need to be aware of into one space.
* `cap deploy` - ???
* `cap -T rubber` - shows all the commands available from capistrano/rubber.
* `cap rubber:describe` - one of the commands from above.  Shows all the EC2 instances running.  Easy peasy.
* `ALIAS=app02 ROLES=app cap rubber:create` - spin up another app server
* `ALIAS=db02 ROLES=db cap rubber:create` - spin up another database
* `ALIAS=tools ROLES=web_tools cap rubber:create` - build a webtools box as well.  web tools boxes have all sorts of wonderful monitoring tools accessible over https for your cluster.  You can access webtools through the url `https://tools.app_name.com:8443`.  The username and password are defined in `config/rubber/rubber.yml`.
