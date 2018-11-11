---
layout: post
title: "rails wrapped in docker"
date: 2015-03-13 14:45
comments: true
categories: rails, docker, programming, infrastructure 
---
I've been interested in docker containers for a long time.  I've been a rails developer since rails 1.0.  Up until today, I've use rails in a headless way,
on a VPS across ssh.  I don't own a mac and I've never been able to make [RailsInstaller](http://railsinstaller.org/en) to work in my workflows without giving
up control of my workspace and dealing with a lot of unnecessary abstractions. Mac guys have it good - a real unix under the desktop means if you run a webrat
test which spins up firefox for a test, it really spins up firefox on your Mac. Headless testing of web ui is a pain in the ass. So, I am really enjoying 
using docker to run rails.
<!-- more -->

Rails is a pain to install and support on a VPS. Most of the VPS's I use run a redhat linux variety, which comes with a ruby from the 1980's. Ain't nobody
got time for that. So, I often use rvm to manage my ruby installs, and gemsets to manage my gems. But occasionally I find myself running a crashing rails
environment because the gems are all screwed up. I'm not sure how it happens, and I'm not sure how to correct the problem. It's a real pain.

But if rails were running in a container, maybe there would be less issues with gems and compatibility. We use rvm to virtualize ruby, because we might need
to run different rubies for different projects (do we really need this?). But using a container means that you can have one container per ruby, and one gemset
per container. No confusion.

So I found this page explaining how to run rails on docker: [link](http://docs.docker.com/compose/rails/).  I made a simpler list with a few added items:

## new directory
Create a new dorectpry to hold your app.

## Dockerfile for ruby 
(`~/sources/dockerfiles/ruby_docker_compose`)

This dockerfile does the work of creating the ruby (rails) container.  In this case it's 2.2.0, but you might find another ruby version. 

```ruby
    FROM ruby:2.2.0
    RUN apt-get update -qq && apt-get install -y build-essential libpq-dev
    RUN mkdir /myapp
    WORKDIR /myapp
    ADD Gemfile /myapp/Gemfile
    RUN bundle install
    ADD . /myapp
```
## have base Gemfile for base rails 
(`~/sources/gemfiles/ruby_docker_compose_base_gemfile`)

Basic Gemfile for loading rails 4.2.0.  If you want a different version, just change it here.

```ruby
    source 'https://rubygems.org'
    gem 'rails', '4.2.0'
```
## have docker-compose.yml 
(`~/sources/dockerfiles/docker_compose_rails_postgresql_stack.yml`)

This docker compose file creates a stack of two containers - one with postgres, one which is the ruby container above.  It also sets up safe communication
between the db and the web containers, a command to execute when the web container starts (the rails server), and finally sets up a `volume` for the storage
of your rails code.  A `volume` is like a shared space between your host OS and the container.

```ruby
    db:
      image: postgres
      ports:
        - "5432"
    web:
      build: .
      command: bundle exec rails s -p 3000 -b '0.0.0.0'
      volumes:
        - .:/myapp
      ports:
        - "3000:3000"
      links:
        - db
```
## build project
This commands creates the rails app.

    `docker-compose run web rails new . --force --database=postgresql --skip-bundle`

## uncomment 'therubyracer' in Gemfile
You'll now notice all sorts of new files in your local directory (where you ran the `docker-compose` command.) In this there's a Gemfile - you can change
it to load gems as needed.

## build the docker containers
Build the containers.

    `docker-compose build`

## sudo chown -R jason.jason *
Not sure why (perhaps because docker-compose runs as root), but you'll notice that everything in your current directory is owned by root.root.  Fix that now.

## update database.yml
Simplify `config/database.yml`, and point the rails app to the db instance.  There's some kind of magic here - apparently dns resolves so that the ruby 
container connects to a host called 'db'.

```ruby    
    development: &default
      adapter: postgresql
      encoding: unicode
      database: postgres
      pool: 5
      username: postgres
      password:
      host: db

    test:
      <<: *default
      database: myapp_test
```

## start the containers
Now you can start the containers.  Note that the ruby container's log is echoed to the screen, like catting the log to stdout.  Pretty convenient.

    `docker-compose up`

## create the databases
This puts the rails databases onto the postgresql database server.

    `docker-compose run web rake db:create`

## load rails locally
This is where this recipe kinda goes off the rails a bit. In order to be able to use rails commands (generators) in the local directory, you need to load
rails locally as well. This brings to mind an issue - what keeps the rails which is local absolutely equal to the rails in the container? Well, there is an
easy method of keeping them in sync - you only load gems using the Gemfile given here. And you may need rvm to keep the local gems in separate sets. Oh well. 

## while the containers are running, create scaffolds
Now you can go ahead and build your rails app as you normally do. One caveat: this is not practicing TDD - this is writing code and running it immediately.
In order to use TDD, you could spin up another container linking back to this same /myapp volume which runs tests all the time. Maybe with guard...
 
    `rails g scaffold Test name address phone`

## profit!
 
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
