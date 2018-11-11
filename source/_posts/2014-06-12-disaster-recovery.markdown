---
layout: post
title: "Disaster Recovery"
date: 2014-06-12 14:44
comments: true
categories: planning, disaster, programming, engineering 
---
I've been thinking a lot about disaster recovery.  I've come to the conclusion that I am not very prepared for many types of disaster.  How prepared are you?
<!-- more -->
### Natural Disasters
In North Texas, we have a few common natural disasters to worry about: tornados, hail, lightning and flood. Tornado's can be devastating, so a careful examination warrants.  According to [The Weather Channel](http://www.weather.com/outlook/weather-news/severe-weather/articles/tornadoes-by-month-dallas_2010-03-25), the odds of a tornado occurring in Texas, by month are:

| Month | % Chance |
| ------- | -------- | 
| January | 0.0337%
| February | 0.0042%
| March | 0.0084%
| April | 0.0758%
| May | 0.0842%
| June | 0.0168%
| July | 0.0084%
| August | 0.0042%
| September | 0.0168%
| October | 0.0463%
| November | 0.0084%
| December | 0.0295%

Consequently, in June, 2014 the chances of a tornado are 1 in 1.7 million.  

Flooding occurs in Texas along low lying areas, near rivers and streams, and near the gulf.  I live in none of those areas.  I'm pretty safe. 

Hail occurs in Texas pretty frequently.  I manage hail risk by parking in a garage whenever I can (most of the time) and parking under a car port when I'm at home.  Hail can damage a rook in Texas, so we typically have a homeowners insurance policy which covers hail damage.  Or, buy a hail-proof roof, like a steel roof.

Lightning can cause fire, and fire can be devastating.  According to [NOAA](http://www.srh.noaa.gov/jetstream/lightning/lightning_faq.htm), the odds of a lightning strike occuring in the US are 1/1,000,000.  So, maybe all the worry is unjustified. In order to avoid the damage caused by lightning, lightning protection systems can be purchased, but it seems like overkill. 

### Unnatural Disasters

Data Loss scares the be-Jesus out of me, because, like many of my peers, I have all my data in spinning hard drives at home, or on the cloud.  Moving more of the data to the cloud, through using picasa or google drive, is my primary goal.  To this end I wil describe my near-term plans for data recovery.

My data storage and recover solution begins with a Synology DS1513+ (about $800) with 5 3TB WD RED drives (about $120 each - $600).  I own a drobo FS - it's great but very proprietary.  My experience with a RAID so far has been that the drives never fail, _per se_, but the RAID operating system can become corrupt, and you need tech support from the provider to restore it.  This is definitiely the case with the drobo - everything is in linux binaries, deliberately obfuscated so you're dependent on tech support.  And you can tell they make a significant amount of their revenue from support contracts.  

_I recently had a system failure which left my drobo inaccessable.  Tech support wouldn't help me because 1.) I did not have a current support contract and 2.) I could not purchase a support contract (they wouldn't sell me one). So, I googled around and found that a company in California will repair and restore my drobo for around $500.  It's a small price to pay for restoring pictures and videos of my daughter's first meals and first season playing soccer.  But it's also a ransom._  

So hopefully Synology is better.  But more importantly, establishing a method of backing up your data and restoring your raid *without assistance from tech support* is crucial.  Restoring your raid might mean restoring the system back to factory settings, thus losing your data.  So backing up your data is critical. This is why the Synology devices are so great.  They have a bunch of backups options, including Amazon Glacier.  

Amazon Glacier is an excellent backup solution because of their cost structure.  They do not charge you significantly for the upload or storage of your data.  Instead, they charge a premium for the download (restore) of your data in the event of an emergency.  Here's a fee schedule:

| GB Stored | Monthly Fee |
| --------- | --- |
| 1         | $0.01
| 10        | $0.10
| 100       | $1.00
| 1000      | $10.00
| 2000      | $20.00


| GB Retrieved | One-time Fee |
| ------------ | ------------ |
| 1            | free
| 10000        | $1200 
| 40000        | $3600

So, a 10 TB system can be fully restored with $1200.

