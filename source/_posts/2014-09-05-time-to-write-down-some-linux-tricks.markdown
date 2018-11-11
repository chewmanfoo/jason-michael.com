---
layout: post
title: "Time to write down some linux tricks"
date: 2014-09-05 19:29
comments: true
categories: linux, tricks 
---
I've run across a lot of stuff using linux at the command line that you don't find easily when you need it.  This blog is an excellent place to store that
sort of stuff.
<!-- more -->

## Dev stuff
*If you're developing TCP services* you can spin up a listening port, with this:

```
nc -l 5000
```

It echo's the text coming in to the screen.

*If you need to use curl to access a web service* but you want to take curl's exit code as a valid signal of the http transaction (so that a 500 http_code
would result in a non-zero exit code):
```
curl -f http://your.url.com
```
Usually, curl exits with 0 when it runs, but `-f` runs curl normally, but the exit code matches the http_code.


*If you need to use an http proxy on your server*, you need a button or a script/function that sets up your proxy. I have a button like this:

```
read http_proxy
http://[uname]:[passwd]@www-proxyserver.com:80
read https_proxy
http://[uname]:[passwd]@www-proxyserver.com:80

``` 

So the password for your proxy is only encoded on your ssh client and not on the server anywhere.  (Anywhere, which means the **history** as well.)

*Using a web service at command line*, you need to pull a web service output down to stdout so you can work with it as a stream.  Try this:

```
wget -q -O - "$@" <url>
``` 

*grab url's from an html page*:

```
awk 'BEGIN{ RS="<a *href *= *\""} NR>2 {sub(/".*/,"");print; }' index.html
```

### Using vim?
*remove those pesky newline characters* the idiot Windows users leave behind:

```
:%s/\r$
```
## Files and Directories
*If the directories have spaces in them*, you can use `*/` to list them correctly, as in 

```
for x in */
```
You have to use the `*/` trick because just listing the files doesn't treat them like individual tokens, it breaks them by space.

*If you want to know what stuff is in a tar file*, use this:

```
tar -tvf tarfile.tar
```

*If you want to find old files*, you can use the `find` command:

```
find . -iname "*.tar" -mtime -30 -exec rm -f {} \;
```

This finds files older than 30 days and whacks them.
 
## Sysadmin stuff
*how is this application defined?* you need to know how the app your running is defined.  Try:

```
sg0218049@tvl-p-mgmt001:~/svn/STAGE2/conf$type -a cd
cd is a function
cd ()
{
    if builtin cd "$@"; then
        [[ -n "${rvm_current_rvmrc:-}" && "$*" == "." ]] && rvm_current_rvmrc="" || true;
        __rvm_cd_functions_set;
        return 0;
    else
        return $?;
    fi
}
cd is a shell builtin
```

*if you're tailing log files* you probably would benefit from highlighting specific words in ANSI colors so it jumps out at you.  Check this out:

```
tail -f tomcat.log | sed -e 's/.*\bERR.*/\x1b[7m&\x1b[0m/i' -e 's/.*\bWARN.*/\x1b[1m&\x1b[0m/i'
```

*pdsh bitches!* If you need to do something on a bunch of servers at the same time, use [ansible](http://www.ansible.com/home), you idiot! But if you
can't do that, use `pdsh`. Like this:

```
sudo pdsh -w $([servers]) -x [xclude_node] "rpm -e apache" | dshbak -c
```

Warning: this requires some setup.

*If you're using puppet* and you find the puppet agent won't run because it says it's already running, but you know it isn't, you can whack the lock
file and the agent will run again.

```
rm -f /var/lib/puppet/state/puppetdlock
```

*If you're using logrotate, and you need to test it now*, then try this:

```
logrotate -d -f /etc/logrotate.conf
```

*You need to datestamp a file name* - so you can see when it was created without using `ls -l`.  Why?  don't ask.

```
date +"%m-%d-%Y--%H:%M:%S"
```

### RPM stuff

*query and rpm* to see what it's scripts are (for install, uninstall etc.)

```
rpm -qp --scripts /users/sg0218049/fpm-repo/stratus-1.9.1-1.x86_64.rpm
```

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
