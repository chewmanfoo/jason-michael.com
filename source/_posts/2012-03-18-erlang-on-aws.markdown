---
layout: post
title: "erlang on AWS"
date: 2012-03-18 22:06
comments: true
categories: 
---
AWS and Amazon Elastic Cloud could be the best playground for erlang projects.  I'm going to experiment with using EC2 to build a variety of erlang "Hello World" projects.

<!-- more -->

## Goals 
#### Virtual Machines

Figure out how to build AWS Amazon Linux (free tier) images the same way [vagrant](http://vagrantup.com/) does images with [Oracle Virtualbox](https://www.virtualbox.org/).

#### Networking

Figure out how to get various EC2 images to talk to each other securely (using [AWS VPC](http://aws.amazon.com/vpc/)).

Here's some links:

 * [Erlang packages for AWS](https://github.com/x6j8x/erlaws/tree)
 * [mochiweb](https://github.com/mochi/mochiweb)
 * [heroku-buildpack-erlang](https://github.com/archaelus/heroku-buildpack-erlang)
 * [AWS Micro Instances with Erlang](http://j2labs.tumblr.com/post/4679269154/instantiating-aws-micro-instances-with-erlang)
 * [Erlang chat server with Comet](http://chrismoos.com/2009/09/28/building-an-erlang-chat-server-with-comet-part-1/)
 * [socket.io-erlang](https://github.com/yrashk/socket.io-erlang)
 * [vagrant-aws gem](https://github.com/mlinderm/vagrant-aws)
