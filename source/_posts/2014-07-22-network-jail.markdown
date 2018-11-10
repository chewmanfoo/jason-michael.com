---
layout: post
title: "network jail"
date: 2014-07-22 22:11
comments: true
categories: politics, networking, linux 
---
I am working on a project to build a continuous integration platform using docker and jenkins.  We may swap out jenkins at a later stage for another builder, who knows?  Anyway, I am trying to manage running docker in a tiered enterprise network which is run by a bunch of goose-stepping nazi's.
<!-- more -->
My VM has visibility to the internet, sometimes.  I have to use an authenticated http_proxy, which means I have to teach every tool at my disposal that an http_proxy exists, which is a pain.  Worse, when I ask for assistance from the networking engineers, I am told that I am not supposed to connect to the internet from my VM.  I *can* do it, but I'm not *supposed to* do it.  This is idiotic.  They say it is because internet access is dangerous.  Well, if it is dangerous, then why allow me to do it?  And if it is not dangerous, then why refuse to support me?  Ah - it is because you don't want to support my requests!  Well, welcome to IT.  Every day somebody wants something new.

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
