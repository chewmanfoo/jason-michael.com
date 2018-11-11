---
layout: post
title: "building an rpm builder"
date: 2014-08-28 19:41
comments: true
categories: packaging, building, scripting
---
I began to work on a packaging system this week - something that generates RPM's from "stuff".  I have a Ruby on Rails app that I created and maintain at
work, but I've never packaged it.  Embarrassed, certainly, but I simply svn clone it to install it.  It's nice that rails installs so easily, but I do need
to package it.  Also, it would be great to go ahead and programatically lay out the dependencies so that installs are as simple as `yum -y install stratus`.
<!-- more -->
But it got more complicated the more I dug into it.  Rails requires ruby, and ruby on Red Hat is 1.8.7.  But writing a rails app based on ruby 1.8.7 is all
sorts of bad.  So you want to load a newer ruby first before you load rails.  Now, [rvm](http://rvm.io/) and [rbenv](http://rbenv.org/) work great.  I 
recommend them. But lets be honest here - with respect to ruby, it would be *better* if we had a native RPM which we can store in a repo, track and manage.
Especially when you consider your Pilot and Production environments - you really want native ruby. And I'm not saying it's easy.

So I wanted to be able to package ruby for my system.  I need a system for pulling the latest stable ruby, and track the dependencies required to build the 
RPM on demand.  

Now that I have two packages to build, I need a menu system for driving the builds, and a system for filing away the dependencies. This is turning into a 
build system!

## Attempt #1
Here's the bash code I came up with:

first, a box building include script:
```bash A boxing system
#!/bin/bash

def_spc=2
def_title="Default Title"
tol="\e(0\x6c\e(B"
tor="\e(0\x6b\e(B"
hoz="\e(0\x71\e(B"
vrt="\e(0\x78\e(B"
bol="\e(0\x6d\e(B"
bor="\e(0\x6a\e(B"
tee="\e(0\x6e\e(B"
lte="\e(0\x74\e(B"
rte="\e(0\x75\e(B"
blueon=$'\e[1m'
blueoff=$'\e[0m'
boldon=$'\e[7m'
boldoff=$'\e[0m'

_Box () {
    str="$2"
    spc=${1:-$def_spc}
    len=$((${#str}+2))
    for i in $(seq $spc); do printf " "; done;
    printf "$tol"
    for i in $(seq $len); do printf "$hoz"; done;
    printf "$tor"
    printf "\n"
    for i in $(seq $spc); do printf " "; done;
    printf "$vrt $str $vrt\n";
    for i in $(seq $spc); do printf " "; done;
    printf "$bol"
    for i in $(seq $len); do printf "$hoz"; done;
    printf "$bor"
    printf "\n"
}

_MLBox () {
                                                    # a multi-line box
    spc=${1:-$def_spc}
    strings=( "$@" )
    numargs="$#"
    numlines=$((numargs-1))
    maxlen=1
    lenbound=80
                                                    # note always ignore ${strings[0]} (the spacing param)
    for i in $(seq $numlines); do
      str=${strings[$i]}
      ilen=$((${#str}+2))
      if [ "$ilen" -gt "$maxlen" ]; then
        maxlen=$ilen
      fi
    done

    for i in $(seq $spc); do printf " "; done;
    printf "$tol"
    for i in $(seq $maxlen); do printf "$hoz"; done;
    printf "$tor"
    printf "\n"

    for i in $(seq $numlines); do
      str=${strings[$i]}
      for i in $(seq $spc); do printf " "; done;
      printf "$vrt "
      printf "%-*s" $((maxlen - 2)) "$str"
      printf " $vrt\n";
    done

    for i in $(seq $spc); do printf " "; done;
    printf "$bol"
    for i in $(seq $maxlen); do printf "$hoz"; done;
    printf "$bor"
    printf "\n"
}

_MLTitledBox () {
                                                    # a multi-line box with title bar
    spc=${2:-$def_spc}
    title=${1:-$def_title}
    title=$(echo "${title^^}")
    strings=( "$@" )
    numargs="$#"
    numlines=$((numargs-1))
    maxlen=1
    lenbound=80
                                                    # note always ignore ${strings[0]} (the title)
                                                    #                and ${strings[1]} (the spacing param)
    for i in $(seq $numlines); do
      str=${strings[$i]}
      ilen=$((${#str}+2))
      if [ "$ilen" -gt "$maxlen" ]; then
        maxlen=$ilen
      fi
    done

    for i in $(seq $spc); do printf " "; done;
    printf "$tol"
    for i in $(seq $maxlen); do printf "$hoz"; done;
    printf "$tor\n"
    for i in $(seq $spc); do printf " "; done;
    printf "$vrt "
    printf "$blueon"
    printf "%-*s" $((maxlen - 2)) "$title"
    printf "$blueoff"
    printf " $vrt\n"
    for i in $(seq $spc); do printf " "; done;
    printf "$lte"
    for i in $(seq $maxlen); do printf "$hoz"; done;
    printf "$rte"
    printf "\n"

    for i in $(seq $numlines); do
      if [ $i -eq 1 ]; then
        skip=1
      else
        str=${strings[$i]}
        for i in $(seq $spc); do printf " "; done;
        printf "$vrt "
        printf "%-*s" $((maxlen - 2)) "$str"
        printf " $vrt\n";
      fi
    done

    for i in $(seq $spc); do printf " "; done;
    printf "$bol"
    for i in $(seq $maxlen); do printf "$hoz"; done;
    printf "$bor"
    printf "\n"
}
```

and the build system:
```bash An RPM building system
#!/bin/bash

def_spc=2
def_title="Default Title"
FPM_PATH="/users/sg0218049/fpm"
FPM_REPO_PATH="/users/sg0218049/fpm-repo"

. boxer

cleanup() {
  rm -rf $FPM_PATH/workspace
  rm -rf $FPM_TMP_PATH/*
}

build_stratus() {
  _Box 4 "building stratus RPM..."
  cd $FPM_PATH/stratus/include/opt/stratus/
  svn up
  cd $FPM_PATH
  BUILD_CMD="fpm -s dir -t rpm -n stratus -v $(cat $FPM_PATH/stratus/include/opt/stratus/version.txt) -C stratus/include/ --before-install $FPM_PATH/stratus/include/opt/stratus/install.sh --after-remove $FPM_PATH/stratus/include/opt/stratus/uninstall.sh -p $FPM_REPO_PATH ."
  $BUILD_CMD || exit 2

  exit 0
}

verify_ruby_prereqs() {
  retval=0
  _Box 4 "verifying ruby package prerequisites..."
  for x in $(curl -Ssl https://raw.githubusercontent.com/postmodern/ruby-install/master/share/ruby-install/ruby/dependencies.txt | grep "^yum" | sed 's/[^ ]* //') gcc-c++ readline tcl-devel tk-devel bison
  do
    rpm -q $x
    if [ $? -ne 0 ]; then
      echo "no $x found"
      retval=3
    fi
  done

echo "retval: $retval"
  if [ $retval -eq "0" ]; then
    _MLBox 4 "All ruby prerequisite packages were found" "Way to go!"
  else
    _MLBox 4 "Some ruby prerequisite packages were missing" "Read above and correct as needed."
  fi

  return $retval
}

build_ruby() {
  _MLBox 4 "building latest ruby RPM..." "... get a coffee, this'll take a while."
  verify_ruby_prereqs || exit $?
  BUILD_CMD="fpm -s dir -t rpm -n ruby -v $(/tmp/fpminstall/ruby/usr/local/bin/ruby -v|cut -d' ' -f2) -C /tmp/fpminstall/ruby/ -p $FPM_REPO_PATH ."
  cd $FPM_PATH/ruby/
  rm -rf stable-snapshot
  rm -rf /tmp/fpminstall/ruby
  wget http://cache.ruby-lang.org/pub/ruby/stable-snapshot.tar.gz
  tar xvfz stable-snapshot.tar.gz
  cd stable-snapshot
  ./configure
  make
  make install DESTDIR=/tmp/fpminstall/ruby
  $BUILD_CMD || exit 2
  exit 0
}

build_exp() {
  _MLBox 4 "you're entering a experimental area."
  exit 0
}

_MLTitledBox "build an RPM" 4 "Select an option below:" " " "[1] build stratus RPM" "[2] build latest ruby RPM" "[3] verify ruby rereqs"

echo -n "    Make a selection:"

read sel

case $sel in
1)
  build_stratus
  ;;
2)
  build_ruby
  ;;
3)
  verify_ruby_prereqs
  ;;
*)
  echo "Um, no" && exit 1
  ;;
esac
```
## What's Next?
So, what's missing?  A few things come to mind:

  * There are packaging dependencies for ruby which are stated on the ruby website and known by insiders, but which are not listed within the source package itself.  So here, I pull down a list from an insider site, add in a few of my own.  There's gotta be a better way.
  * The "verify prereqs" and "build whatever" functions are specific to whatever I'm building.  This is not at all necessary - I ought to be able to genericize those functions and make the code a lot tighter and more elegant.
  * There's a lot of opportunity to DRY it up.
  * for my personal package (stratus), I need to continue to explore the insides of that RPM.  The phpmyadmin RPM is a good example for me - it depends on php and apache and mysql, it loads the source code into an appropriate place for apache, and it loads config files into apache which allow it to run my default at `http://example.com/phpmyadmin`.  You can change all of that after the fact, but it's really good enough, and the total install steps thanks to this script are `yum -y install phpmyadmin`.  Fantastic.

## Dependencies and Duplication of Effort
Considering dependencies when building RPM's brings up another issue I've been wondering about lately.  I use a lot of puppet at work,
and I express dependencies in puppet for packages right there in the puppet manifests.  That way, I can explicitly state all the packages
which are installed on my server right there in the puppet code.  It seems like a *good thing* to have puppet be the
authority for what gets installed on the box.  i.e. If you want to know "what are all the packages installed on this box?"
you can ask puppet and you can be sure it will tell you the truth.  But not so fast.  If I add phpmyadmin as a package in puppet, and
I don't add apache and mysql, yum (rpm) will install mysql and apache for me within the puppet agent refresh because phpmyadmin requires them, and I'm not
sure there's any way for puppet to know what other packages yum installed when it installed phpmyadmin.  So in actuality,
yum is the authority on what packages are installed on my RHEL system, as it should be.  So there seems to be some kind of
tug of war going on here. I'm not sure about this yet - I'll probably blog about it later when I figure it out.

<!-- see https://github.com/Shopify/liquid/wiki/Liquid-for-Designers for stuff 
# H1
## H2
[I'm an inline-style link](https://www.google.com)
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png 'Logo Title Text 1')
```javascript
var s = 'JavaScript syntax highlighting';
alert(s);
```
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
-->
