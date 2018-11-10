---
layout: post
title: "A Rails App for AWS Orchestration"
date: 2015-01-08 21:48
comments: true
categories: rails, aws, orchestration, cloudformation
---
I need an application which provides a simple user interface for creating CloudFormation templates and provisioning AWS instances based on those templates.
It needs to keep track of running instances, provide billing monitoring, and be able to decommission instances based on an agreement made with the user at
the point of creation (i.e. "build a web server to run for 7 days").  It needs multi-tenancy, with users and groups within each tenant, with common and
separate AWS access keys.
<!-- more -->
I'm investigating using an existing app. [Foreman](http://blog.theforeman.org/2012/05/ec2-provisioning-using-foreman.html) seems to provide some of this
functionality, but as usual it's a swiss army knife, and I only need the spoon and the toothpick. There's also [Heat](https://wiki.openstack.org/wiki/Heat)
which looks like it's just an API which works as an adapter for AWS API's (why does one need that?) Looks like I've got a lot of reading to do.

I have had some luck creating rails stacks using the aws cli on linux. A few points have emerged:

   * once the stack build begins, it's not easy to track it from the cli (`aws cloudformation describe-stack-events --stack-name myStack`)
   * a stack build failed because of a resource constraint but it wasn't clear in the CLI why it failed (but on the webapp it was clear why it failed)
   * one stack simply hung at "CREATE_IN_PROGRESS" for hours - I had to kill it
   * stack build fails if users permissions don't allow the creation of a resource - how to check for permissions prior to the build? Policy Simulator from the CLI?
   * billing impact (cost of services included in the stack) ought to be viewable before the build. Cost Estimator from the CLI?
   * a complex stack (a Rails multi-AZ template from [here](https://s3-us-west-2.amazonaws.com/cloudformation-templates-us-west-2/Rails_Multi_AZ.template) fails to build due to a resource constraint.  Traces of the failure (like a stack in ROLLBACK_COMPLETE) did not show up in the UI, but a simple stack (one that created a simple S3 bucket) and did not fail to complete, showed up in the UI.  

## Getting Started

I built a rails 4 app with a mysql backend (I would use Postgresql but my postgres skills are quite rusty now.) I wrapped the view in Foundation - normally I
use bootstrap, but I thought I'd try foundation out this time. I've decided to store Cloudformation Templates in a source control of some kind, or allow
the user to provide a source control URL.  The cloudformation_template will be a tool for viewing and editing CF templates stored in source control. 
You can wrap the editor in UAC, and have some kind of validation, up to full document parsing if you want to invest the time. I'll provide a controller 
(a model also ?) called deployment, which involves a priviledged user selecting a Cloudformation Template and giving a start datetime (do it now, or do it 
at 5:00PM next Tuesday). The deployment has a state machine and sidekiq for back end processing to move the deployment through the state machine. 
An example state might be "Pre-Flight Check", which would verify that the CF Template selected was loaded into S3.
If the template is not loaded into S3, it would either loop forever until it is loaded, or try to load it and fill in the S3_url field. The deployment would 
provide a view into the state machine, statistics about ETA etc.  

I would really like to create a widget with css/images/javascript for showing the state of the deployment. I found this [guide](http://www.ibm.com/developerworks/library/wa-finitemach1/). So, work to do. One thing I need this app to display is a really accurate view of the current state of all the state machines
Like maybe using [Faye](http://railscasts.com/episodes/260-messaging-with-faye).

 
