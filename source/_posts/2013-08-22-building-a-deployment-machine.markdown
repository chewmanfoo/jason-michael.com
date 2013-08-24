---
layout: post
title: "Building a Deployment Machine"
date: 2013-08-22 22:50
comments: true
categories: 
---
Over the past year, I have worked to build a self-service deployment machine for a Fortune 500 Online Travel Agency.  
The product would integrate with puppet and activemq, and provide a secure, fault-tolerant self-service system for users (developers) who were not specifically trained to do puppet deployments to production servers.  Since the developers are prone to make bonehead mistakes, and since the cloud environment was inherently a bit chaotic and non-deterministic (more on this later), the machine had to be able to filter out noise from the user end, and handle static on the server end with grace.  It had to be designed from inception with these constraints in mind.  It was a challenge and I'm happy with the results.
<!-- more -->

### A Word on Puppet and ActiveMQ

Normally, people configure their instances to have puppet agents that kick off at regular intervals, enforcing configurations and policies like sentries marching in a circle around their post.  The benefit of this kind of system is that it makes management feel warm and fuzzy - it's a set it and forget it strategy.  At any point in time, you can ask "what does this configuration look like on that bank of instances there?" and the answer is always, "Lets look at the manifests."  And if there is any question about a particular instance, that it might be somehow showing symptoms of differing from the policy, you can either wait for the next puppet refresh or trigger the refresh now.   Further, puppet logs can give you a good view into the operation of the servers.  You can add the puppet log to your syslog/graylog server and have a nice story to watch unfold in front of you.  
However, if you don't like the idea of a poke in the eye at regular intervals (especially in production), you might want some control on when the puppet agent runs.  Having the puppet agent triggering every 15 minutes works if and only if your process is well-tested and well-known and if your puppet manifests are spot on.  If any non-determinism lives comfortably in your process, you might want to consider turning that puppet agent daemon off. 

Confession time: we had less testing that we were comfortable with, and we had more non-determinism that we would like to admit.  And we had a huge, monolithic application to deploy with a lot of ghetto code.  So we wrapped the puppet agent refresh in an activemq listener so we could trigger refreshes to one or many instances with pretty good control when we wanted.  We then added in-service/out-of-service control to the scripts by integrating with the F5's SOAP interface.  We had the whole thing scripted and it worked great.  But the only people who could work it were engineers who knew how it worked.  In the end, for us, it was all work work work.

### Enter: Rails and Sidekiq

I know how to build Rails apps.  I can spin one up in 15 minutes.  I can roll a rails app into an RPM and deploy it anywhere in the snap of a finger.  I thought I'd try to build a rails interface into this puppet/activemq system, so we could allow developers and managers who didn't have our puppet training to deploy software - even to production.  It ought to "just work", it ought to protect the company's services and the client experience, but be able to take action when we wanted.   

In order to model the deployment of a package to our environments, I had to map out the state machine we had created.  I used the Workflow gem for this.  In order to enable fault-tolerance, retry's and run time customization, I added the Sidekiq gem and service.  Sidekiq uses Redis on the backend, and allows you to run ruby scripts in a multi-threaded non-blocking fashion.  It all runs pretty well.

### Further Improvements

As we ran the system for a while (174 thousand deployments to date), we started running continuous integration with some applications.  This involved triggering builds with Jenkins which generated an RPM, then detecting if new RPM's were available, and if so, building a deployment for them and starting it automatically.  We can now deploy from certification, through a variety of Unit tests, UI testing using selenium, sanity checks etc. all the way to production in 11 minutes.  It beats all the work we had to accomplish once upon a time just to put a package in orbit.    
