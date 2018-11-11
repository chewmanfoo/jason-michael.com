---
layout: post
title: "time to write down some rails tricks"
date: 2015-06-05 19:15
comments: true
categories: rails, tips
---
Just a quick post for saving some scripts I may need in the future.  I may come back later and make it prettier.
<!-- more -->

## Views

### For Splitting Long Words
I worked on a project which rendered a page of SVN repos in a table.  Since the table columns were of fixed width, the length of
various attributes was important.  In this case, the name of the repo could be upwards of 150 characters.  Rails provides a few
nice text handling helpers, but one of them depends on word breaks, and the other chops off a word after a fixed length, and adds
'...' etc.  For a file name, it seems like a bad idea to hide the full word from the user.  So I needed a way to break a word at
a fixed width, but just keep going with the word on the next line, and beyond until the word is displayed.

This solution works great:

```ruby
def split_str(str, len = 10)
  fragment = /.{#{len}}/
  str.split(/(\s+)/).map! { |word|
    (/\s/ === word) ? word : word.gsub(fragment, '\0<wbr />')
  }.join
end
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
