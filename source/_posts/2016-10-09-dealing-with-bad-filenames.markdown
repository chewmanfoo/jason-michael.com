---
layout: post
title: "Dealing with Bad Filenames"
date: 2016-10-09 23:27
comments: true
categories: linux 
---
I had a filename which had a newline in it. Not sure how it got created,
but it is there.

<!-- more -->
```bash
> ls
  a?file 
```

The best way to deal with this is just to use inodes.  There are a bunch of 
other options, but just grab the BFG and fix it now!

```bash
> ls -i
  10428179 a?file
> find . -inum 10428179 -exec mv {} afile \;
> ls
  afile
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
