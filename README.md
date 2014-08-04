Crawler4j
=========

Crawler4j is an open source Java crawler which provides a simple interface for crawling the Web. You can setup a multi-threaded web crawler in 5 minutes!


How to use?
-----------
Basicly it is very simple.
Crawler4j exposes an API with only two methods which you need to implement:

**boolean shouldVisit(WebUrl webUrl)** - called BEFORE fetching of the page, so you can decide if you want to allow the fetching of the page by analyzing the URL (example: returning false on all non html pages, or returning true only when the url is of specific domains etc)

**void visit(Page page)** - called after the page was fetched and lets you decide what to do about it.


Detailed examples can be found on this site's [wiki pages] (https://github.com/Chaiavi/Crawler4j/wiki)


Code Examples
-------------
**[Basic crawler](https://github.com/Chaiavi/Crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/basic)**: Basic example usecase of Crawler4j.
**[Image crawler](https://github.com/Chaiavi/Crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/imagecrawler)**: a simple image crawler that downloads image content from the crawling domain and stores them in a folder. This example demonstrates how binary content can be fetched using crawler4j.
**[Collecting data from threads](https://github.com/Chaiavi/Crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/localdata)**: this example demonstrates how the controller can collect data/statistics from crawling threads.
**[Multiple crawlers](https://github.com/Chaiavi/Crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/multiple)**: this is a sample that shows how two distinct crawlers can run concurrently. For example, you might want to split your crawling into different domains and then take different crawling policies for each group. Each crawling controller can have its own configurations.
**[Shutdown crawling](https://github.com/Chaiavi/Crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/shutdown)**: this example shows have crawling can be terminated gracefully by sending the 'shutdown' command to the controller.



CLARIFICATION
-------------
This project aims to be a continuation of the [Original Project] (https://code.google.com/p/crawler4j/) which is the best [open source java web crawler] (http://myblog.chaiware.org/2014/07/crawling-site-with-java.html).

This project began with the latest release of crawler4j (v3.5), and will add patches to the code where each patch will take care of **one** **objective** issue.

No custom code for specific needs here, this projects aims for the good of all, thus will only accept code which makes crawler4j better for everyone's needs and not for any company's specific needs.

I hope that the original author will recognize the value of my patches and will accept all of my changes into the original code repository.


RELEASE
-------
For latest stable release, download v3.5 from the maven repository or from the [original site] (https://code.google.com/p/crawler4j/downloads/list)

For v3.5 with several bug fixes and good features (will be listed in a following paragraph) clone my project ad build.

If many bugfixes will accumulate, I will build a jar for the benefit of all (maybe even put it on maven's repo?).


Can I participate in this project?
----------------------------------
**YES**
But, I don't know you yet, so in order to know that you are a worthy committer, please follow the following guidelines

*Want to add your code?*
1. Make sure it is good for everyone and not only solves your specific needs.
2. Open an issue detailing the need and your suggested solution
3. If this is a bug, then please detail the scenario
4. Fork my project and make the fix
5. **Make sure you didn't add any other code except this issue's specific code** No code styling, no adding something else etc - only **ATOMIC** changes correlated to this specific issue.
6. Send me a pull request

That's it, I will see the issue and probably reply so a conversation might arise in the issues list.
I will see the code and if it is good I will apply it to my own.
If it is not suitable I might request you to fix it accordingly.
After I see that you are fit, I will request you to become a committer on this project - welcome!


Changelog
---------
1. Updated all libraries to their latest version (August 2014) (issue #21)
2. Removed log4j implementation and switched it with slf4j (issue #1)
3. Added more logs
4. Many more to come, feel free to look into the [issues list] (https://github.com/Chaiavi/Crawler4j/issues)


What about the original issues ?
--------------------------------
I went over all of the [original issues] (https://code.google.com/p/crawler4j/issues/list) and copied only the relevant ones (about 15 issues), by relevant I mean those issues which were actual bugs and had a definitive scenario, or those feature requests which were for the good of all and not for specific needs.


What about the other forks of Crawler4j
---------------------------------------
I searched github and found 41 forks! of crawler4j.
I went over all of them twice, and found only two forks worth mentioning (all of the rest, are either old or aren't adding anything), but both forks are for private companies and cover their own custom requirements.

So I will keep monitoring them and take whatever helps my users most.
