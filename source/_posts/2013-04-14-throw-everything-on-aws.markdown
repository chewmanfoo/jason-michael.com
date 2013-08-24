---
layout: post
title: "Throw Everything on AWS"
date: 2013-04-14 18:19
comments: true
categories: aws, systems administration
---
<!-- more -->
I am paying $40 a month to linode for a 1Gb slice with no practical limits on transfers (there may in fact be limits, but I haven't experienced them.)  I'm exploring whether I can move the services I run on linode to AWS, free tier, so save some $$.

iRedMail

I run a wiki and a mail server.

I found this: http://www.inboxs.com/index.php/linux-os/ec2-aws/10-how-to-install-iredmail-on-ec2

The free tier allows Amazon linux as well as ubuntu.  The transfer limits may be an issue with a mail server.  I'm debating on imap mail servers available online. The upshot is that when I get turned into a forward for spam, I have to fight it on my linode server, but the imap provider would be responsible for it (time = money, so it's often a good deal).

Amazon Glacier

Backups are essential.  You don't value them until you need them, but when you need them, and you don't have them, it could be devastating.  You need a system which will bacup your critical files periodically, and perform the function whether you like it or not.  Set it and forget it.  And it always helps if somebody else is contractually obligated to manage the media, store the tapes offsite, restore upon demand.

Then somebody brings up the issue of cost.  There are several different options for online backup solutions out there, and they vary widely on cost.  Amazon Glacier's model is designed to get you up and running quickly with little upfront cost and a tiny monthly maintenance fee.  It only costs you if/when you need ot restore your stuff.  Costwise it's smart.  I recommend it.

In order to bring easy periodic backups to your linux instance using Amazon Glacier, I recommend this lovely gem: [link](https://github.com/uskudnik/amazon-glacier-cmd-interface) This works also, but it's more of a pain in the ass: [link](http://blog.epsilontik.de/?page_id=68) 
