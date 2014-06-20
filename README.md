# SBT RSS

This is an SBT 0.13 plugin which uses AutoPlugin and the ROME libraries to 
query websites for RSS feeds and display the titles inside SBT.

## Usage

Assuming that you have built it, typing `rss` at an SBT prompt will show latest updates.

You can change the `rssList` settings to pick a different list of RSS URLs.  See `sbt-rss-tester` for examples.

## Building and testing 

```
sbt publish-local
```

or go to the `sbt-rss-tester` project and type `rss` at an SBT prompt.


