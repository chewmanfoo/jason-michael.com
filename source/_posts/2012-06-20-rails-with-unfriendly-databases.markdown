layout: post
title: "Rails with Unfriendly Databases"
date: 2012-06-20 22:02
comments: true
categories: Ruby on Rails, programming, mysql, databases
---

We all know that Rails is "Opinionated" when it comes to the backend database schema.  Tables are named after the plural word describing what they contain ('posts', not 'Post' or 'post' or 'boatload_o_posts').  Tables have a primary key called 'id'.  In fact, the easiest way to get along with rails from the gitgo is to let rails build it's backend database for you, using migrations.  That's crucial to getting your Rails mojo kung-fu working.  But what hapens when you need to connect your Rails app to an unfriendly database?  What if you have to read data from a database designed by some one who, gasp!, doesn't do Rails?  What then???

I recently added a report to my corporate site survey Rails project which needed to pull data from a bunch of joined tables in a database which was the backend for Manage Engine's (supportcenter)[http://www.manageengine.com/products/support-center/].  I really don't like supportcenter, and I _really_ don't like supportcenter's database.  Wow what a flaming sack of dogshit that thing is.

<!-- more -->

## Connecting to the unfriendly database

You can either add login/password to your model, or add them to database.yml.  I chose to add them to database.yml:

Then, we'll use these credentials to connect to the database and run queries in our model.

## A new Rails model for the unfriendly database's table

Create a new model which inherits from ActiveRecord, like so:

{% codeblock workorder.rb %}
class Workorder < ActiveRecord::Base
#  establish_connection "supportcenter_#{RAILS_ENV}"
#  self.abstract = true
  establish_connection "supportcenter_production"
  set_table_name "workorder"
  set_primary_key "WORKORDERID"
{% endcodeblock %}

## Queries and results


