/*
May it be usefull - download scenario just from browser

https://github.com/eligrey/FileSaver.js
var blob = new Blob(["Hello, world!"], {type: "text/plain;charset=utf-8"});
saveAs(blob, "hello world.txt");
 */

/*===================================================================================================================================*/
/** Global variables * */
// event id
var eventId = 0;

// browser window/tab uuid
var tabUuid = guid();

// is tracking mouse enabled
var trackMove = false;
/** Global variables * */

// base url to make links right
var baseUrl = '';

// recorder options
var options = {
		baseUrl:'',
		downloadPath:'/jsflight/recorder/download',
		statusPath:'/jsflight/recorder/status',
		trackMouse:false,
		trackHash:false,
		trackXhr:false,
		propertyProvider: function(prop){}
}
/*===================================================================================================================================*/

/**
 * Based on firebug method of getting xpath of dom element
 */
function getElementXPath(element) {
	if (element && element.id)
		return '// *[@id="' + element.id + '"]';
	else
		return getElementTreeXPath(element);
}

function getElementTreeXPath(element) {
	var paths = [];

	// Use nodeName (instead of localName) so namespace prefix is included (if
	// any).
	for (; element && element.nodeType == 1; element = element.parentNode) {
		var index = 0;
		for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {
			// Ignore document type declaration.
			if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)
				continue;

			if (sibling.nodeName == element.nodeName)
				++index;
		}

		var tagName = element.nodeName.toLowerCase();
		var pathIndex = (index ? "[" + (index + 1) + "]" : "");
		paths.splice(0, 0, tagName + pathIndex);
	}

	return paths.length ? "/" + paths.join("/") : null;
}

/**
 * Make json of mouse event.
 * 
 * @param mouseEvent
 * @returns {___anonymous1625_1630}
 */
function getEventInfo(mouseEvent) {
	if (mouseEvent === undefined)
		mouseEvent = window.event;

	var target = 'target' in mouseEvent ? mouseEvent.target
			: mouseEvent.srcElement;

	mouseEvent.target = target;

	var result = new Object;
	// To be really sure what target element was, it may be useful to hold the
	// whole path to target element
	// var path = mouseEvent.path;
	// var xpaths = [];
	// for(var i=0;i<path.length;i++){
	// if(path[i] && path[i]!=document && path[i]!=window){
	// xpaths.push(getElementXPath(path[i]))
	// }
	// }
	// result['path'] = xpaths;

	result['tabuuid'] = tabUuid;
	result['type'] = mouseEvent.type;
	result['url'] = window.location.href;
	result['charCode'] = mouseEvent.charCode;
	result['button'] = mouseEvent.button;

	result['target'] = getElementXPath(mouseEvent.target);
	result['timestamp'] = mouseEvent.timeStamp;

	result['screenX'] = mouseEvent.screenX;
	result['screenY'] = mouseEvent.screenY;

	result['pageX'] = mouseEvent.pageX;
	result['pageY'] = mouseEvent.pageY;

	result['screen.width'] = screen.width;
	result['screen.height'] = screen.height;

	result['window.width'] = window.outerWidth;
	result['window.height'] = window.outerHeight;

	result['page.width'] = window.innerWidth;
	result['page.height'] = window.innerHeight;

	return result;
}

/**
 * Store event to session storage
 * 
 * @param eventid
 * @param eventdata
 */
function saveToStorage(eventid, eventdata) {
	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return;
	}

	window.sessionStorage.setItem("recorder.max", eventid);
	window.sessionStorage.setItem('recorder.eventId.' + eventid, eventdata);
}

/**
 * Clear session storage
 */
function clearStorage() {
	eventId = 0;
	sessionStorage.clear();
}

/**
 * Generate browser window/tab uuid
 * 
 * @returns {String}
 */
function guid() {
	function s4() {
		return Math.floor((1 + Math.random()) * 0x10000).toString(16)
				.substring(1);
	}
	return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4()
			+ s4() + s4();
}

/**
 * Process mouse event
 */
var TrackMouse = function(mouseEvent) {
	if (mouseEvent.type == 'mousemove' && trackMove == false) {
		return;
	}
	if (mouseEvent.target && mouseEvent.target.id) {
		// ignoring self buttons
		if (mouseEvent.target.id.indexOf("no-track-flight-cp") === 0) {
			return;
		}
	}
	var data = JSON.stringify(getEventInfo(mouseEvent));
	saveToStorage(eventId, data);
	console.log("Event: " + data + "\n");
	eventId++;
};

/**
 * Process keyboard event
 */
var TrackKeyboard = function(keyboardEvent) {
	if (keyboardEvent.target && keyboardEvent.target.id) {
		// ignoring self buttons
		if (keyboardEvent.target.id.indexOf("no-track-flight-cp") === 0) {
			return;
		}
	}

	var data = JSON.stringify(getEventInfo(keyboardEvent));
	saveToStorage(eventId, data);
	console.log("Event: " + data + "\n");
	eventId++;
};

/**
 * Start recorder
 * 
 * @returns {Boolean}
 */
function startRecorder() {
	if (document.addEventListener) {
		document.addEventListener('mousedown', TrackMouse);
		document.addEventListener('mousemove', TrackMouse);
		document.addEventListener('keypress', TrackKeyboard);
	} else {
		document.attachEvent('mousedown', TrackMouse);
		document.attachEvent('mousemove', TrackMouse);
		document.attachEvent('keypress', TrackKeyboard);
	}
	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return false;
	}

	window.sessionStorage.setItem('recorder.active', true);
	eventId = +window.sessionStorage.getItem('recorder.max') + 1;
	if (!eventId) {
		eventId = 0;
	}
}

/**
 * Stop recorder
 * 
 * @returns {Boolean}
 */
function stopRecorder() {
	if (document.removeEventListener) {
		document.removeEventListener('mousedown', TrackMouse);
		document.removeEventListener('mousemove', TrackMouse);
		document.removeEventListener('keypress', TrackKeyboard);
	} else {
		document.detachEvent('mousedown', TrackMouse);
		document.detachEvent('mousemove', TrackMouse);
		document.detachEvent('keypress', TrackKeyboard);
	}

	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return false;
	}

	window.sessionStorage.removeItem('recorder.active');
}

/**
 * Convert all event to json
 * 
 * @returns
 */
function getEventsAsString() {
	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return;
	}
	var storage = window.sessionStorage;

	var events = [];
	for ( var key in storage) {
		if (key.indexOf('recorder.eventId.') === 0) {
			events.push(storage.getItem(key));
		}
	}

	return JSON.stringify(events)
}

/**
 * method to show/hide control panel
 * 
 * @param event
 */
function controlHook(event) {
	if (event.ctrlKey && event.altKey && (event.which || event.keyCode) == 38) {
		var panel = document.getElementById("flight-cp");
		panel.style.display = 'block';
	}
	if (event.ctrlKey && event.altKey && (event.which || event.keyCode) == 40) {
		var panel = document.getElementById("flight-cp");
		panel.style.display = 'none';
	}
}

/**
 * Will recording be continued in case of page reload
 * 
 * @returns {Boolean}
 */
function shouldStartOnLoad() {
	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return false;
	}

	if (window.sessionStorage.getItem('recorder.active')) {
		return true;
	}

	return false
}

/**
 * Inject control panel specific js-code and markup
 */
function addControlHook() {
	tabUuid = guid();

	var script = document.createElement('script')
	script.type = 'text/javascript'
	script.charset = 'utf-8'
	script.text = ' \
		function flight_hide(){ \
	        var panel = document.getElementById("flight-cp"); \
	        panel.style.display="none"; \
		}\
		function flight_getEvents(){ \
        	document.getElementById("data").value = getEventsAsString();\
        	return true; \
	    } \
	    function flight_start(){ \
			flight_hide(); \
	    	startRecorder(); \
	    } \
	    function flight_stop(){ \
	        stopRecorder(); \
	    } \
	    function flight_clear(){ \
			stopRecorder(); \
	        clearStorage(); \
	    }';
	document.body.appendChild(script);

	var div = document.createElement("div");
	div.id = "flight-cp";
	div.style.display = 'none';

	div.innerHTML = '<h1>JSFlightRecorder</h1><h4><a href="https://github.com/d0k1/JSFlightRecorder">https://github.com/d0k1/JSFlightRecorder</a></h4><h2>Control panel</h2> \
	<div> \
	   <form action="'+baseUrl+'/jsflight/recorder/download" method="post" target="_blank"> \
	       <input id="data" type="hidden" value="secret" name="data"/> \
	       <input type="submit" value="Download recording" onclick="flight_getEvents()"/> \
	   </form>\
	</div>\
		<div> \
	        <button id="no-track-flight-cp1" onclick="flight_start()">Start</button> \
	        <button id="no-track-flight-cp2" onclick="flight_stop()">Stop</button> \
	        <button id="no-track-flight-cp3" onclick="flight_clear()">Clear</button> \
	        <button id="no-track-flight-cp4" onclick="flight_hide()">Hide</button> \
	        <button id="no-track-flight-cp5" onclick="window.open(\''+baseUrl+'/jsflight/recorder/status\', \'_blank\')">Status</button> \
		    <br/> \
			<br/> \
			<button id="no-track-flight-cp6" onclick="trackMove=true;">Track Move</button> \
			<button id="no-track-flight-cp7" onclick="trackMove=false">Dont Track Move</button> \
		</div> \
		';

	document.body.appendChild(div);

	if (document.addEventListener) {
		document.addEventListener('keyup', controlHook);
	} else {
		document.attachEvent('keyup', controlHook);
	}

	if (shouldStartOnLoad()) {
		startRecorder();
	}
}

/**
 * Clean up when tab is closing
 */
function removeControlHook() {
	if (document.removeEventListener) {
		document.removeEventListener('keyup', controlHook);
	} else {
		document.detachEvent('keyup', controlHook);
	}
}

/**
 * Init method. should be called on host page. uses onload and onbeforeunload
 * mechanics to add/remove event listeners
 * 
 * @param trackMouseMove
 */
function addJSFlightHooksOnDocumentLoad(url, trackMouseMove) {
	window.onload = function() {
		addControlHook();
	}
	window.onbeforeunload = function() {
		removeControlHook();
	};
	
	baseUrl = url;
	
	if (trackMouseMove === true) {
		trackMove = true;
	}
}