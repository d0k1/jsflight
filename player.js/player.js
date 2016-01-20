function Player() {
	var fs = require('fs');

	var records = [];

	var actionChain = [];

	var self = this;

	var sort = function () {
		var events = [];

		for (var i = 0; i < records.length; i++) {
			var recItem = records[i];
			for (var j = 0; j < recItem.length; j++) {
				events.push(recItem[j]);
			}
		}
		records = events;
	};

	var lastEventId = null;
	var page = null;

	var getKeyEvent =  function(keyCode, character) {
	  var evt = document.createEvent("KeyboardEvent");
	  (evt.initKeyEvent || evt.initKeyboardEvent)("keypress", true, true, window,
	                    0, 0, 0, 0,
	                    keyCode, character.charCodeAt(0)) 
	  return evt;
	}

	var getMouseEvent = function(type, sx, sy, cx, cy) {
	  var evt;
	  var e = {
	    bubbles: true,
	    cancelable: (type != "mousemove"),
	    view: window,
	    detail: 0,
	    screenX: sx, 
	    screenY: sy,
	    clientX: cx, 
	    clientY: cy,
	    ctrlKey: false,
	    altKey: false,
	    shiftKey: false,
	    metaKey: false,
	    button: 0,
	    relatedTarget: undefined
	  };
	  if (typeof( document.createEvent ) == "function") {
	    evt = document.createEvent("MouseEvents");
	    evt.initMouseEvent(type, 
	      e.bubbles, e.cancelable, e.view, e.detail,
	      e.screenX, e.screenY, e.clientX, e.clientY,
	      e.ctrlKey, e.altKey, e.shiftKey, e.metaKey,
	      e.button, document.body.parentNode);
	  } else if (document.createEventObject) {
	    evt = document.createEventObject();
	    for (prop in e) {
	    evt[prop] = e[prop];
	  }
	    evt.button = { 0:1, 1:4, 2:2 }[evt.button] || evt.button;
	  }
	  return evt;
	}

	var dispatchSimulatedEvent = function(el, evt){
	  if (el.dispatchEvent) {
	    el.dispatchEvent(evt);
	  } else if (el.fireEvent) {
	    el.fireEvent('on' + type, evt);
	  }
	  return evt;
	}

	var createAction = function (type, event, prevAction) {
		var action = null;
		if (type == 'url') {
			action = {
				type: 'url',
				event: event
			};
		}

		if (type == 'event') {
			action = {
				type: 'event',
				event: event
			};
		}

		if (action !== null && prevAction !== null) {
			prevAction.next = action;
		}
		action.next = null;
		return action;
	};

	var evalAction = function (page, action, next) {
		if (action.type == 'url') {
			console.log('Type: '+action.type+'; event: '+action.event.url);
			var page = self.getPage(action.event, function(){
				console.log("page for simulaition opened");
			});
		}
		if (action.type == 'event') {
			console.log('Type: '+action.type+'; event: '+action.event.type);
			var page = self.getPage(action.event, function(){
				console.log("ready to simulate an event!");
			});
		}
		if(action.next!==null) {
			next(action);
		}
	};

	var createChain = function () {
		var prevAction = null;

		for (var i = 0; i < records.length; i++) {
			var action = null;

			action = createAction('url', records[i], prevAction);

			if (action !== null) {
				actionChain.push(action);
				prevAction = action;
			}

			action = createAction('event', records[i], prevAction);
			if (action !== null) {
				actionChain.push(action);
				prevAction = action;
			}
		}
	};


	var playStep = function (action, next) {
		evalAction(null, action, next);
	};

	var playNext = function(action1){
		playStep(action1.next, playNext);
	};

	/* Public interface */
	this.getPage = null;
	this.isPageReady = null;


	this.defaultPageReadyCheck = function (page) {
		return true;
	};

	this.defaultGetPageForEvent = function (event, onReady) {
		if (page === null) {
			page = this.createPage();
		}
		if(lastEventId===null || lastEventId!=event.id) {
			page = this.openPageUrl(event, page, event.url, onReady, this.isPageReady);
			lastEventId = event.id;
		}
		return page;
	};

	this.createPage = function () {
		var webPage = require('webpage');
		var page = webPage.create();
		page.onResourceRequested = function (requestData, networkRequest) {
			//   console.log('---Request (#' + requestData.id + '): ');// + JSON.stringify(requestData));
		};

		page.onResourceReceived = function (response) {
			// console.log('Response (#' + response.id + ', stage "' + response.stage + '"): ');// + JSON.stringify(response));
		};

		return page;
	};

	this.openPageUrl = function(event, page, url, onReady, pageReadyCheck){
		var timerHandler = null;
		var triy = 0;
		var maxTries = 10;

		function checkFunction() {
			console.log('EvnetId '+event.eventId+' try '+triy+' check ready url '+url);
			triy++;
			var result = false;
			if(triy<=maxTries) {
				result = pageReadyCheck(page);
			}
			var notReadyWaitLimitExceeded = (result===false && triy>maxTries);
			if (result === true || notReadyWaitLimitExceeded) {
				clearInterval(timerHandler);
				if(notReadyWaitLimitExceeded!==true) {
					onReady(page);
				} else {
					console.log('page was not become ready after ' + maxTries + ' tries');
				}
			}
		}

		page.onConsoleMessage = function (msg, lineNum, sourceId) {
			console.log('CONSOLE: ' + msg + ' (from line #' + lineNum + ' in "' + sourceId + '")');
		};

		page.open(url, function(status) {
			console.log('Page load status: ' + status + ' for eventId '+event.eventId);

			// Do other things here...

			timerHandler = setInterval(checkFunction, 500);
		});
		return page;
	}

	this.init = function (path) {
		console.log("Loading " + path);

		var stream = fs.open(path, 'r');

		while (!stream.atEnd()) {
			var line = stream.readLine();
			var events = JSON.parse(line);
			records.push(events);
		}
		stream.close();
		sort();
		createChain();
		console.log('Events: ' + records.length + ' Actions: ' + actionChain.length);
		this.getPage = this.defaultGetPageForEvent;
		this.isPageReady = this.defaultPageReadyCheck;
	};

	this.playScenario = function(){
		playStep(actionChain[0], playNext);
	}
}

function isSDPageLoaded(page) {
	return page.evaluate(function () {
		var state = '';

		if (document.getElementById('state.context') !== undefined) {
			state = document.getElementById('state.context').getAttribute('value');
		}
		if (state == 'ready') {
			return true;
		} else {
			return false;
		}
		return false;
	});
}

/**
 * Main part
 */
var f = new Player();
f.init("/tmp/3.json");
f.isPageReady = isSDPageLoaded;
f.playScenario();