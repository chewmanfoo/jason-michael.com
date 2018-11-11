---
layout: post
title: "Mooching PhpMyAdmin Build"
date: 2014-09-03 16:17
comments: true
categories: rpm, build 
---
My next step in my ongoing "building RPM's" saga is examining and stealing all the good bits of the PhpMyAdmin RPM.
The PhpMyAdmin RPM is special in this case because it is a web application which runs inside a web server like apache
or nginx and has a database backend. So the RPM depends on a web server and a database, and it lays down app code in
a webapps directory and sets permissions correctly so that the app can start after install. The app even has post-install
steps to be performed by the user.  So there's a *lot* under the hood here. I found the PhpMyAdmin rpm spec here:
[link](https://github.com/repoforge/rpms/blob/master/specs/phpmyadmin/phpmyadmin.spec). There's about 70 lines of magic in that script. I'll try to evaluate
the important ones here. 
<!-- more -->

# The header

   * `Name: phpmyadmin` - the name of the package
   * `Version: 2.11.11.3` - the version of the package
   * `Release: 2%{?dist}` - the release of the package
   * `Group: Applications/Internet` - a group for groupinstall.  Very useful
   * `Requires: php-mysql >= 4.1.0` - this package will cause php-mysql to be installed if it is not present
   * `Requires: webserver` - requires a package to supply a 'webserver'.  For example, the package httpd has the line `Provides: webserver`
   * `Obsoletes: phpMyAdmin <= %{version}-%{release}` - causes phpMyAdmin to be uninstalled if it's present and it's version is less than this version
   * `Provides: phpMyAdmin = %{version}-%{release}` - a marker which can be used by another packages `Requires`.  In this case the package and version are given.

# More details

There's an interesting block in `%prep` that sets up the `phpmyadmin.conf` file:

```
%{__cat} <<EOF >phpmyadmin.conf
#
#  %{summary}
#

<Directory "%{_datadir}/phpmyadmin">
  Order Deny,Allow
  Deny from all
  Allow from 127.0.0.1
</Directory>

Alias /phpmyadmin %{_datadir}/%{name}
Alias /phpMyAdmin %{_datadir}/%{name}
Alias /mysqladmin %{_datadir}/%{name}
EOF
```

Interesting - apparently this means that this file `phpmyadmin.conf` is created on the target system and copied to it's final location with this line, found
later in the spec file:

```
%{__install} -Dp -m0644 phpmyadmin.conf %{buildroot}%{_sysconfdir}/httpd/conf.d/phpmyadmin.conf
```

## The %install

The `%install` section has some interesting points.  Here it is in full:

```
%install
%{__rm} -rf %{buildroot}
%{__mkdir} -p %{buildroot}/%{_datadir}/%{name}
%{__mkdir} -p %{buildroot}/%{_sysconfdir}/httpd/conf.d/
%{__mkdir} -p %{buildroot}/%{_sysconfdir}/%{name}

%{__install} -d -m0755 %{buildroot}%{_datadir}/%{name}/
%{__cp} -av *.{php,html,css,ico} %{buildroot}%{_datadir}/%{name}/
%{__cp} -av contrib/ js/ lang/ libraries/ pmd/ scripts/ themes/ %{buildroot}%{_datadir}/%{name}/

%{__install} -Dp -m0644 config.sample.inc.php %{buildroot}%{_datadir}/%{name}/config.inc.php
%{__install} -Dp -m0644 phpmyadmin.conf %{buildroot}%{_sysconfdir}/httpd/conf.d/phpmyadmin.conf

```

First, there's something called `%{buildroot}`, which is defined earlier:

```
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root
```

at the top of the spec file.  The *buildroot* is a space on the disc of the build server (not the target server where the package is being installed), where
the package can be staged and assembled.   

So how is this working? There's a line `%{__rm} -rf %{buildroot}` which seems to be clearing out the buildroot.  Then there are a few lines setting up
directories in buildroot which will later hold stuff.  Next there's `%{__install}` commands and `%{__cp}` commands.  

The `%{__install}` command, as in:

```
%{__install} -d -m0755 %{buildroot}%{_datadir}/%{name}/
```

is the same thing as `/usr/bin/install`.  In this case it has parameters -d (the thing we're installing is a directory) and -m0755 (same as chmod 0755 for
every directory and file.) 

The `%{__cp}` command, as in:

```
%{__cp} -av *.{php,html,css,ico} %{buildroot}%{_datadir}/%{name}/
```

is the same as `/usr/bin/cp`. In this case it has parameters -av, and copies every php, html, css and ico file from the current directory to buildroot.
Apparently, if it's in buildroot, it will be copied to the target server. This is a bit confusing to me. 

More to come.

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
