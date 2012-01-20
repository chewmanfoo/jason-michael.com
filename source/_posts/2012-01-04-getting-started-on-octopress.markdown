---
layout: post
title: "Getting started on octopress"
date: 2012-01-04 14:23
comments: true
categories:  helloworld
---
I haven't had a blog in a long time.  Earlier, I had a horrible PHP + CSS + MySQL + Contract with the Devil page that I moved around from website host to website host whenever my contract was up for renewal - I always found a reason to leave.  Now I'm on a lovely VPS on Linode and I love it.  

I tried Jekyll before but had a bear getting it running - now I'm trying Octopress, a wrapper for Jekyll.  It's much easier to use. I'm getting used to it.  Now, I get to use my favorite text editor (vim) to create my blog.  What a concept! 

*cool!* easy codesharing!

{% codeblock %}
$ sudo make me a sandwich
{% endcodeblock %}


{% codeblock This kind of thing can be done on ruby in 2 lines - main.java %}
BufferedWriter out = null;
try {
    out = new BufferedWriter(new FileWriter(”filename”, true));
    out.write(”aString”);
} catch (IOException e) {
    // error processing code
} finally {
    if (out != null) {
        out.close();
    }
}
{% endcodeblock %}
