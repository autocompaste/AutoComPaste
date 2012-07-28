@echo off
echo Launching the webpages

start firefox.exe http://email.about.com/od/famousemailaddresses/f/What_is_Steve_Jobs_s_Email_Address.htm

REM wait for 20 seconds
ping -n 20 127.0.0.1 > nul

start firefox.exe http://www.telegraph.co.uk/technology/steve-jobs/8811345/Steve-Jobs-adopted-child-who-never-met-his-biological-father.html

REM wait for 20 seconds
ping -n 20 127.0.0.1 > nul

start firefox.exe http://en.wikipedia.org/wiki/Steve_Jobs

REM wait for 30 seconds
ping -n 30 127.0.0.1 > nul

start firefox.exe http://www.bbc.co.uk/news/magazine-15194365

REM wait for 20 seconds
ping -n 20 127.0.0.1 > nul

start firefox.exe http://news.yahoo.com/blogs/technology-blog/8-things-didn-t-know-life-steve-jobs-172130955.html