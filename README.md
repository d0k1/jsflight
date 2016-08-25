# JSFlight
Tools to record user's activity and replay it later.
The project's main goal is to store major user events in a browser (mouse clicks, key presses) and them reproduce them using Selenium WebDriver.
Besides it can store HttpRequests on server-side and convert them to a JMeter Scenario to replay it later.

##Recording concepts
###Browser recording
* You should add some JS to a host page to record browsers events 
* Injected code will add event some handlers (you can easily configure what events should be recorder and what not)
* Event handlers stores it's data in browser's session store
* Data from browser's session store is to be sent to the server with some period of time (Default period is 5 seconds)
* `javax.servlet.http.HttpServlet` on server side decided what to do with data it received

###Server-Side recording
* There is base class to be used to build a custom `javax.servlet.Filter`
* The filter will record any http request to urls it has been mapped to
* Of course you can add some special app's internal events to the recording. For example to monitor a production environment (or to get more data to replay user load more accurate)

[![Codeship Status for d0k1/JSFlightRecorder](https://codeship.com/projects/56dc64a0-6a0d-0133-3e69-6e257542035e/status?branch=master)](https://codeship.com/projects/114774)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/1f078e96ab984f2f92419c3c65bb7aa9)](https://www.codacy.com/app/denis-kirpichenkov/jsflight)
[![Code Climate](https://codeclimate.com/github/d0k1/jsflight/badges/gpa.svg)](https://codeclimate.com/github/d0k1/jsflight)
[![Issue Count](https://codeclimate.com/github/d0k1/jsflight/badges/issue_count.svg)](https://codeclimate.com/github/d0k1/jsflight)
