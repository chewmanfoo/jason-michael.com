---
layout: post
title: "Rails with Unfriendly Databases"
date: 2012-06-20 22:02
comments: true
categories: Ruby on Rails, programming, mysql, databases
---
We all know that Rails is "Opinionated" when it comes to the backend database schema.  Tables are named after the plural word describing what they contain ('posts', not 'Post' or 'post' or 'boatload_o_posts').  Tables have a primary key called 'id'.  In fact, the easiest way to get along with rails from the gitgo is to let rails build it's backend database for you, using migrations.  That's crucial to getting your Rails mojo kung-fu working.  But what hapens when you need to connect your Rails app to an unfriendly database?  What if you have to read data from a database designed by some one who, gasp!, doesn't do Rails?  What then???

I recently added a report to my corporate site survey Rails project which needed to pull data from a bunch of joined tables in a database which was the backend for Manage Engine's [supportcenter](http://www.manageengine.com/products/support-center/).  I really don't like supportcenter, and I _really_ don't like supportcenter's database.  Wow what a flaming sack of dogshit that thing is.

<!-- more -->

## Connecting to the unfriendly database

You can either add login/password to your model, or add them to database.yml.  I chose to add them to database.yml:

{% codeblock database.yml %}
supportcenter_production:
  adapter: mysql
  database: supportcenter
  username: uname
  password: secret
  host: 10.0.0.1
  port: 3306
{% endcodeblock %}

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
...
end
{% endcodeblock %}

## Queries and results

Now we can add queries to the Workorder model which pull from this unfriendly supportcenter database:

{% codeblock workorder.rb %}
...
  def self.get_one_week_of_major_critical_tickets
#    self.get_tickets_by_range(Time.now()-1, Time.now()-14)
    Workorder.find_by_sql("SELECT NOW() - interval 1 day as start, NOW() - interval 14 day as end, workorder.WORKORDERID AS REQUEST_ID, sladefinition.SLANAME AS PRIORITY, componenttype.COMPONENTTYPENAME AS PRODUCT_TYPE, componentdefinition.COMPONENTNAME AS PRODUCT, workorder_fields.UDF_CHAR2 AS DEGREE, workorder_fields.UDF_CHAR3 AS SVC_IMPACT, categorydefinition.CATEGORYNAME AS CATEGORY, statusdefinition.STATUSNAME AS STATUS, workorder_fields.UDF_CHAR5 AS CAUSE_CODE, workorder.CREATEDTIME AS CREATED_ON, aaauser.FIRST_NAME AS CREATED_BY, aaauser_1.FIRST_NAME AS ASSIGNED_TO, aaaorganization.NAME AS CUSTOMER, workorder.TITLE, workorder.RESOLVEDTIME AS RESOLVED_ON, aaauser_2.FIRST_NAME AS RESOLVED_BY, requestresolution.RESOLUTION, workorder.RESPONDEDTIME AS RESPONDED_ON, workorder.DUEBYTIME AS DUE_ON, workorder.LASTUPDATED AS LAST_UPDATED, workorder.COMPLETEDTIME AS COMPLETED_ON, TIMESPENTONREQ AS HOURS_SPENT FROM ((((((((((((workorder LEFT JOIN workorder_fields ON workorder.WORKORDERID = workorder_fields.WORKORDERID) LEFT JOIN requestresolution ON workorder.WORKORDERID = requestresolution.REQUESTID) LEFT JOIN workorderstates ON workorder.WORKORDERID = workorderstates.WORKORDERID) LEFT JOIN categorydefinition ON workorderstates.CATEGORYID = categorydefinition.CATEGORYID) LEFT JOIN aaauser AS aaauser_1 ON workorderstates.OWNERID = aaauser_1.USER_ID) LEFT JOIN aaauser ON workorder.CREATEDBYID = aaauser.USER_ID) LEFT JOIN sladefinition ON workorder.SLAID = sladefinition.SLAID) LEFT JOIN workorder_product ON workorder.WORKORDERID = workorder_product.WORKORDERID) LEFT JOIN (componentdefinition LEFT JOIN componenttype ON componentdefinition.COMPONENTTYPEID = componenttype.COMPONENTTYPEID) ON workorder_product.PRODUCT_ID = componentdefinition.COMPONENTID) LEFT JOIN statusdefinition ON workorderstates.STATUSID = statusdefinition.STATUSID) LEFT JOIN aaauser AS aaauser_2 ON workorder.RESOLVEDBY = aaauser_2.USER_ID) LEFT JOIN workorder_account ON workorder.WORKORDERID = workorder_account.WORKORDERID) LEFT JOIN aaaorganization ON workorder_account.ACCOUNTID = aaaorganization.ORG_ID where ((componenttype.COMPONENTTYPENAME)<>'Lab' and FROM_UNIXTIME(workorder.CREATEDTIME/1000) < (Now()- interval 1 day) And FROM_UNIXTIME(workorder.CREATEDTIME/1000) > (Now() - interval 7 day) and sladefinition.SLANAME is not null and (sladefinition.SLANAME = 'Critical SLA' or sladefinition.SLANAME = 'Major SLA'))")
...
  end
end
{% endcodeblock %}

Note the godawful table attributes in the supportcenter database.  If you're sitting on your couch late at night, and you hear a knock on the door, and you go to the door and open it and look around, and if you see the supportcenter database on your doorstep, do not stomp out the fire!!!

Therefore, we can slice and dice the result array from this query within the controller:

{% codeblock controller.rb %}
...
    @tickets = Workorder.get_one_week_of_tickets
    @previous_weeks_tickets = Workorder.get_previous_week_of_tickets
    @tickets_stats = Hash.new
    @tickets.group_by(&:CUSTOMER).each do |c,ts|
      @tickets_stats[c] = ts.size
    end
    @tickets_stats = @tickets_stats.sort_by {|c,count| count}.reverse
...
{% endcodeblock %}

It works like a charm.
