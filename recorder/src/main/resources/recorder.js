/**
 * Core methods.
 * Init
 * Start Stop
 * Timers
 * 
 */
// jsflight namespace 
var jsflight = jsflight || {}


/**
 * Start recorder
 * 
 * @returns {Boolean}
 */
jsflight.startRecorder = function() {
	// do not forget about timers. first stop them, next start them if required
	jsflight.stopTimers();
	jsflight.startTimers();

	if (document.addEventListener) {
		document.addEventListener('mousedown', jsflight.TrackMouse);
		document.addEventListener('mousemove', jsflight.TrackMouse);
		document.addEventListener('keypress', jsflight.TrackKeyboard);
		document.addEventListener('keyup', jsflight.TrackKeyboard);
		window.addEventListener('hashchange', jsflight.TrackHash);
	} else {
		document.attachEvent('mousedown', jsflight.TrackMouse);
		document.attachEvent('mousemove', jsflight.TrackMouse);
		document.attachEvent('keypress', jsflight.TrackKeyboard);
		document.attachEvent('keyup', jsflight.TrackKeyboard);
		window.attachEvent('hashchange', jsflight.TrackHash);
	}
	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return false;
	}

	window.sessionStorage.setItem('recorder.active', 'true');
	jsflight.eventId = +window.sessionStorage.getItem('recorder.max') + 1;
	if (!jsflight.eventId) {
		jsflight.eventId = 0;
	}

	if (jsflight.options.trackXhr) {
		jsflight.initXhrTracking();
	}

	jsflight.started = true;
}

/**
 * Stop recorder
 * 
 * @returns {Boolean}
 */
jsflight.stopRecorder = function() {
	jsflight.stopTimers();
	if (document.removeEventListener) {
		document.removeEventListener('mousedown', jsflight.TrackMouse);
		document.removeEventListener('mousemove', jsflight.TrackMouse);
		document.removeEventListener('keypress', jsflight.TrackKeyboard);
		document.removeEventListener('keyup', jsflight.TrackKeyboard);
		window.removeEventListener('hashchange', jsflight.TrackHash);
	} else {
		document.detachEvent('mousedown', jsflight.TrackMouse);
		document.detachEvent('mousemove', jsflight.TrackMouse);
		document.detachEvent('keypress', jsflight.TrackKeyboard);
		document.detachEvent('keyup', jsflight.TrackKeyboard);
		window.detachEvent('hashchange', jsflight.TrackHash);
	}

	if (typeof (window.sessionStorage) == "undefined") {
		console.log('No support of window.sessionStorage');
		return false;
	}

	window.sessionStorage.removeItem('recorder.active');
	window.sessionStorage.removeItem('recorder.max');

	if (jsflight.options.trackXhr) {
		jsflight.stopXhrTracking();
	}

	jsflight.started = false;
}

/**
 * Will recording be continued in case of page reload
 * 
 * @returns {Boolean}
 */
jsflight.shouldStartOnLoad = function() {

	if (jsflight.options.autostart == true) {
		return true;
	}

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
 * Init method. should be called on host page. uses onload and onbeforeunload
 * mechanics to add/remove event listeners
 * 
 * @param trackMouseMove
 */
jsflight.addJSFlightHooksOnDocumentLoad = function(options) {

	jsflight.parseOptions(options)
	

	function initialDump() {
		if (jsflight.options.saveInitialScreenshot) {
			jsflight.take_a_screenshot();
		}

		if (jsflight.options.saveInitialDom) {
			jsflight.take_dom_snapshot();
		}
	}

	// when document is rendered
	window.onload = function() {

		if(jsflight.options.saveInitialScreenshot || jsflight.options.saveInitialDom){
			window.setTimeout(200, initialDump());			
			window.setTimeout(2500, initialDump());
			window.setTimeout(5500, initialDump());
			window.setTimeout(9000, initialDump());
		}

		jsflight.addControlHook();
	}

	// when tab is above to close
	window.onbeforeunload = function() {
		jsflight.stopTimers();
		// disable all event handlers
		jsflight.removeControlHook();
		// send captured events
		jsflight.sendEventData(true);
	};
}

jsflight.startTimers = function() {
	if (jsflight.options.track_duration > 0)
		jsflight.stop_timer = window.setInterval(jsflight.stop_recording_timer,
				jsflight.options.track_duration);

	if (jsflight.options.send_interval > 0)
		jsflight.send_timer = window.setInterval(jsflight.send_data_timer,
				jsflight.options.send_interval);
}

jsflight.stopTimers = function() {
	if (jsflight.stop_timer != null) {
		window.clearInterval(jsflight.stop_timer);
		jsflight.stop_timer = null;
	}

	if (jsflight.send_timer != null) {
		window.clearInterval(jsflight.send_timer);
		jsflight.send_timer = null;
	}
}

jsflight.take_a_screenshot = function() {
	var event = {};
	html2canvas(document.body, {
		onrendered : function(canvas) {
			try {
				var myImage = canvas.toDataURL("image/png");
				event.image = myImage;
				event.type = 'screenshot';
				event.timeStamp = Date.now();
				var data = JSON.stringify(jsflight.getEventInfo(event));
				jsflight.saveToStorage(jsflight.eventId, data);
			} catch (e) {
				console.log(e);
			} finally {
				jsflight.eventId++;
			}
		}
	});
}

jsflight.take_dom_snapshot = function() {
	try {
		var tree = Xml.getElementHTML(document.body);
		var event = {};
		event.type = 'snapshot';
		event.timeStamp = Date.now();
		event.dom = tree;
		var data = JSON.stringify(jsflight.getEventInfo(event));
		jsflight.saveToStorage(jsflight.eventId, data);
	} catch (e) {
		console.log(e);
	} finally {
		jsflight.eventId++;
	}
}

jsflight.stop_recording_timer = function() {
	jsflight.sendEventData(true);
	jsflight.stopRecorder();
}

jsflight.send_data_timer = function() {
	jsflight.sendEventData(false);
}