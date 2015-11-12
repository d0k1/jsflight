/*
https://github.com/eligrey/FileSaver.js

var blob = new Blob(["Hello, world!"], {type: "text/plain;charset=utf-8"});
saveAs(blob, "hello world.txt");

*/

/* FileSaver.js
 * A saveAs() FileSaver implementation.
 * 1.1.20151003
 *
 * By Eli Grey, http://eligrey.com
 * License: MIT
 *   See https://github.com/eligrey/FileSaver.js/blob/master/LICENSE.md
 */

/*global self */
/*jslint bitwise: true, indent: 4, laxbreak: true, laxcomma: true, smarttabs: true, plusplus: true */

/*! @source http://purl.eligrey.com/github/FileSaver.js/blob/master/FileSaver.js */

var saveAs = saveAs || (function(view) {
	"use strict";
	// IE <10 is explicitly unsupported
	if (typeof navigator !== "undefined" && /MSIE [1-9]\./.test(navigator.userAgent)) {
		return;
	}
	var
		  doc = view.document
		  // only get URL when necessary in case Blob.js hasn't overridden it
			// yet
		, get_URL = function() {
			return view.URL || view.webkitURL || view;
		}
		, save_link = doc.createElementNS("http://www.w3.org/1999/xhtml", "a")
		, can_use_save_link = "download" in save_link
		, click = function(node) {
			var event = new MouseEvent("click");
			node.dispatchEvent(event);
		}
		, is_safari = /Version\/[\d\.]+.*Safari/.test(navigator.userAgent)
		, webkit_req_fs = view.webkitRequestFileSystem
		, req_fs = view.requestFileSystem || webkit_req_fs || view.mozRequestFileSystem
		, throw_outside = function(ex) {
			(view.setImmediate || view.setTimeout)(function() {
				throw ex;
			}, 0);
		}
		, force_saveable_type = "application/octet-stream"
		, fs_min_size = 0
		// See https://code.google.com/p/chromium/issues/detail?id=375297#c7 and
		// https://github.com/eligrey/FileSaver.js/commit/485930a#commitcomment-8768047
		// for the reasoning behind the timeout and revocation flow
		, arbitrary_revoke_timeout = 500 // in ms
		, revoke = function(file) {
			var revoker = function() {
				if (typeof file === "string") { // file is an object URL
					get_URL().revokeObjectURL(file);
				} else { // file is a File
					file.remove();
				}
			};
			if (view.chrome) {
				revoker();
			} else {
				setTimeout(revoker, arbitrary_revoke_timeout);
			}
		}
		, dispatch = function(filesaver, event_types, event) {
			event_types = [].concat(event_types);
			var i = event_types.length;
			while (i--) {
				var listener = filesaver["on" + event_types[i]];
				if (typeof listener === "function") {
					try {
						listener.call(filesaver, event || filesaver);
					} catch (ex) {
						throw_outside(ex);
					}
				}
			}
		}
		, auto_bom = function(blob) {
			// prepend BOM for UTF-8 XML and text/* types (including HTML)
			if (/^\s*(?:text\/\S*|application\/xml|\S*\/\S*\+xml)\s*;.*charset\s*=\s*utf-8/i.test(blob.type)) {
				return new Blob(["\ufeff", blob], {type: blob.type});
			}
			return blob;
		}
		, FileSaver = function(blob, name, no_auto_bom) {
			if (!no_auto_bom) {
				blob = auto_bom(blob);
			}
			// First try a.download, then web filesystem, then object URLs
			var
				  filesaver = this
				, type = blob.type
				, blob_changed = false
				, object_url
				, target_view
				, dispatch_all = function() {
					dispatch(filesaver, "writestart progress write writeend".split(" "));
				}
				// on any filesys errors revert to saving with object URLs
				, fs_error = function() {
					if (target_view && is_safari && typeof FileReader !== "undefined") {
						// Safari doesn't allow downloading of blob urls
						var reader = new FileReader();
						reader.onloadend = function() {
							var base64Data = reader.result;
							target_view.location.href = "data:attachment/file" + base64Data.slice(base64Data.search(/[,;]/));
							filesaver.readyState = filesaver.DONE;
							dispatch_all();
						};
						reader.readAsDataURL(blob);
						filesaver.readyState = filesaver.INIT;
						return;
					}
					// don't create more object URLs than needed
					if (blob_changed || !object_url) {
						object_url = get_URL().createObjectURL(blob);
					}
					if (target_view) {
						target_view.location.href = object_url;
					} else {
						var new_tab = view.open(object_url, "_blank");
						if (new_tab == undefined && is_safari) {
							// Apple do not allow window.open, see
							// http://bit.ly/1kZffRI
							view.location.href = object_url
						}
					}
					filesaver.readyState = filesaver.DONE;
					dispatch_all();
					revoke(object_url);
				}
				, abortable = function(func) {
					return function() {
						if (filesaver.readyState !== filesaver.DONE) {
							return func.apply(this, arguments);
						}
					};
				}
				, create_if_not_found = {create: true, exclusive: false}
				, slice
			;
			filesaver.readyState = filesaver.INIT;
			if (!name) {
				name = "download";
			}
			if (can_use_save_link) {
				object_url = get_URL().createObjectURL(blob);
				save_link.href = object_url;
				save_link.download = name;
				setTimeout(function() {
					click(save_link);
					dispatch_all();
					revoke(object_url);
					filesaver.readyState = filesaver.DONE;
				});
				return;
			}
			// Object and web filesystem URLs have a problem saving in Google
			// Chrome when
			// viewed in a tab, so I force save with application/octet-stream
			// http://code.google.com/p/chromium/issues/detail?id=91158
			// Update: Google errantly closed 91158, I submitted it again:
			// https://code.google.com/p/chromium/issues/detail?id=389642
			if (view.chrome && type && type !== force_saveable_type) {
				slice = blob.slice || blob.webkitSlice;
				blob = slice.call(blob, 0, blob.size, force_saveable_type);
				blob_changed = true;
			}
			// Since I can't be sure that the guessed media type will trigger a
			// download
			// in WebKit, I append .download to the filename.
			// https://bugs.webkit.org/show_bug.cgi?id=65440
			if (webkit_req_fs && name !== "download") {
				name += ".download";
			}
			if (type === force_saveable_type || webkit_req_fs) {
				target_view = view;
			}
			if (!req_fs) {
				fs_error();
				return;
			}
			fs_min_size += blob.size;
			req_fs(view.TEMPORARY, fs_min_size, abortable(function(fs) {
				fs.root.getDirectory("saved", create_if_not_found, abortable(function(dir) {
					var save = function() {
						dir.getFile(name, create_if_not_found, abortable(function(file) {
							file.createWriter(abortable(function(writer) {
								writer.onwriteend = function(event) {
									target_view.location.href = file.toURL();
									filesaver.readyState = filesaver.DONE;
									dispatch(filesaver, "writeend", event);
									revoke(file);
								};
								writer.onerror = function() {
									var error = writer.error;
									if (error.code !== error.ABORT_ERR) {
										fs_error();
									}
								};
								"writestart progress write abort".split(" ").forEach(function(event) {
									writer["on" + event] = filesaver["on" + event];
								});
								writer.write(blob);
								filesaver.abort = function() {
									writer.abort();
									filesaver.readyState = filesaver.DONE;
								};
								filesaver.readyState = filesaver.WRITING;
							}), fs_error);
						}), fs_error);
					};
					dir.getFile(name, {create: false}, abortable(function(file) {
						// delete file if it already exists
						file.remove();
						save();
					}), abortable(function(ex) {
						if (ex.code === ex.NOT_FOUND_ERR) {
							save();
						} else {
							fs_error();
						}
					}));
				}), fs_error);
			}), fs_error);
		}
		, FS_proto = FileSaver.prototype
		, saveAs = function(blob, name, no_auto_bom) {
			return new FileSaver(blob, name, no_auto_bom);
		}
	;
	// IE 10+ (native saveAs)
	if (typeof navigator !== "undefined" && navigator.msSaveOrOpenBlob) {
		return function(blob, name, no_auto_bom) {
			if (!no_auto_bom) {
				blob = auto_bom(blob);
			}
			return navigator.msSaveOrOpenBlob(blob, name || "download");
		};
	}

	FS_proto.abort = function() {
		var filesaver = this;
		filesaver.readyState = filesaver.DONE;
		dispatch(filesaver, "abort");
	};
	FS_proto.readyState = FS_proto.INIT = 0;
	FS_proto.WRITING = 1;
	FS_proto.DONE = 2;

	FS_proto.error =
	FS_proto.onwritestart =
	FS_proto.onprogress =
	FS_proto.onwrite =
	FS_proto.onabort =
	FS_proto.onerror =
	FS_proto.onwriteend =
		null;

	return saveAs;
}(
	   typeof self !== "undefined" && self
	|| typeof window !== "undefined" && window
	|| this.content
));
// `self` is undefined in Firefox for Android content script context
// while `this` is nsIContentFrameMessageManager
// with an attribute `content` that corresponds to the window

if (typeof module !== "undefined" && module.exports) {
  module.exports.saveAs = saveAs;
} else if ((typeof define !== "undefined" && define !== null) && (define.amd != null)) {
  define([], function() {
    return saveAs;
  });
}

function createXPathFromElement(elm) { 
    var allNodes = document.getElementsByTagName('*'); 
    for (var segs = []; elm && elm.nodeType == 1; elm = elm.parentNode) 
    { 
        if (elm.hasAttribute('id')) { 
                var uniqueIdCount = 0; 
                for (var n=0;n < allNodes.length;n++) { 
                    if (allNodes[n].hasAttribute('id') && allNodes[n].id == elm.id) uniqueIdCount++; 
                    if (uniqueIdCount > 1) break; 
                }; 
                if ( uniqueIdCount == 1) { 
                    segs.unshift('id("' + elm.getAttribute('id') + '")'); 
                    return segs.join('/'); 
                } else { 
                    segs.unshift(elm.localName.toLowerCase() + '[@id="' + elm.getAttribute('id') + '"]'); 
                } 
        } else if (elm.hasAttribute('class')) { 
            segs.unshift(elm.localName.toLowerCase() + '[@class="' + elm.getAttribute('class') + '"]'); 
        } else { 
            for (i = 1, sib = elm.previousSibling; sib; sib = sib.previousSibling) { 
                if (sib.localName == elm.localName)  i++; }; 
                segs.unshift(elm.localName.toLowerCase() + '[' + i + ']'); 
        }; 
    }; 
    return segs.length ? '/' + segs.join('/') : null; 
}; 

function lookupElementByXPath(path) { 
    var evaluator = new XPathEvaluator(); 
    var result = evaluator.evaluate(path, document.documentElement, null,XPathResult.FIRST_ORDERED_NODE_TYPE, null); 
    return  result.singleNodeValue; 
} 

function getElementXPath(element)
{
	if (element && element.id)
		return '// *[@id="' + element.id + '"]';
	else
		return getElementTreeXPath(element);
}

function getElementTreeXPath(element)
{
	var paths = [];

	// Use nodeName (instead of localName) so namespace prefix is included (if
	// any).
	for (; element && element.nodeType == 1; element = element.parentNode)
	{
		var index = 0;
		for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling)
		{
			// Ignore document type declaration.
			if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)
				continue;
			
			if (sibling.nodeName == element.nodeName)
				++index;
		}

		var tagName = element.nodeName.toLowerCase();
		var pathIndex = (index ? "[" + (index+1) + "]" : "");
		paths.splice(0, 0, tagName + pathIndex);
	}

	return paths.length ? "/" + paths.join("/") : null;
}

function getEventInfo(mouseEvent) {
    if (mouseEvent===undefined)
    	mouseEvent= window.event;                    
    
    var target= 'target' in mouseEvent? mouseEvent.target : mouseEvent.srcElement;

    mouseEvent.target = target;
    
	var path = mouseEvent.path;
	var xpaths = [];
	for(var i=0;i<path.length;i++){
		if(path[i] && path[i]!=document && path[i]!=window){
			xpaths.push(getElementXPath(path[i]))
		}
	}
	
	var result = new Object;

	result['url'] = window.location.href;
	result['charCode'] = mouseEvent.charCode;
	result['button'] = mouseEvent.button;

	result['target'] = getElementXPath(mouseEvent.target);
	result['path'] = xpaths;
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

function saveToStorage(eventid, eventdata){
	if(typeof(window.sessionStorage) == "undefined") {
	    console.log('No support of window.sessionStorage');
	    return;
	}
	
	window.sessionStorage.setItem("recorder.max", eventid);
	window.sessionStorage.setItem('recorder.eventId.'+eventid, eventdata);
}

function clearStorage(){
	eventId = 0;
	sessionStorage.clear();
}

var eventId = 0;

/* ============================================ */
var TrackMouse = function (mouseEvent) {
	if(mouseEvent.target && mouseEvent.target.id){
		// ignoring self buttons
		if(mouseEvent.target.id.indexOf("no-track-flight-cp")===0){
			return;
		}
	}
	var data = JSON.stringify(getEventInfo(mouseEvent));
	saveToStorage(eventId, data);
    console.log("Event: " + data + "\n");
	eventId++;
};

var TrackKeyboard = function (keyboardEvent) {
	if(keyboardEvent.target && keyboardEvent.target.id){
		// ignoring self buttons
		if(keyboardEvent.target.id.indexOf("no-track-flight-cp")===0){
			return;
		}
	}

	var data = JSON.stringify(getEventInfo(keyboardEvent));
	saveToStorage(eventId, data);
    console.log("Event: " + data + "\n");
	eventId++;
};
/* ============================================ */

function saveToFile(){
	window.alert('Woo-Hoo');
}

function addJSFlightHooksOnDocumentLoad(){
    window.onload = function() {
        addControlHook();
    }
}

function startRecorder(){
	if(document.addEventListener){
		document.addEventListener('click', TrackMouse);
		document.addEventListener('keypress', TrackKeyboard);
	} else {
		document.attachEvent('click', TrackMouse);
		document.attachEvent('keypress', TrackKeyboard);
	}
	if(typeof(window.sessionStorage) == "undefined") {
	    console.log('No support of window.sessionStorage');
	    return false;
	}
	
	window.sessionStorage.setItem('recorder.active', true);
	eventId = +window.sessionStorage.getItem('recorder.max')+1;
	if(!eventId){
		eventId=0;
	}
}

function stopRecorder(){
	if(document.removeEventListener){
		document.removeEventListener('click', TrackMouse);
		document.removeEventListener('keypress', TrackKeyboard);
	} else {
		document.detachEvent('click', TrackMouse);
		document.detachEvent('keypress', TrackKeyboard);
	}
	
	if(typeof(window.sessionStorage) == "undefined") {
	    console.log('No support of window.sessionStorage');
	    return false;
	}

	window.sessionStorage.removeItem('recorder.active');
}

function getEventsAsString(){
	if(typeof(window.sessionStorage) == "undefined") {
	    console.log('No support of window.sessionStorage');
	    return;
	}
	var storage = window.sessionStorage;
	
	var events = [];
	for (var key in storage){
		if(key.indexOf('recorder.eventId.')===0){
		   events.push(storage.getItem(key)); 
		}
	}
	
	return JSON.stringify(events)
}

function controlHook(event){
	if(event.ctrlKey && event.altKey && (event.which || event.keyCode)==38){
		var panel = document.getElementById("flight-cp");
		panel.style.display='block';
	}
}

function shouldStartOnLoad(){
	if(typeof(window.sessionStorage) == "undefined") {
	    console.log('No support of window.sessionStorage');
	    return false;
	}
	
	if(window.sessionStorage.getItem('recorder.active')){
		return true;
	}
	
	return false
}

function addControlHook(){
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
	div.id="flight-cp";
	div.style.display='none';
	
	div.innerHTML = '<h1>Control Panel</h1> \
	<div> \
	   <form action="jsflight/recorder/download" method="post"> \
	       <input id="data" type="hidden" value="secret" name="data"/> \
	       <input type="submit" value="download" onclick="flight_getEvents()"/> \
	   </form>\
	</div>\
		<div> \
	       <button id="no-track-flight-cp1" onclick="flight_start()">Start</button> \
	       <button id="no-track-flight-cp2" onclick="flight_stop()">Stop</button> \
	       <button id="no-track-flight-cp3" onclick="flight_clear()">Clear</button> \
	       <button id="no-track-flight-cp4" onclick="flight_hide()">Hide</button> \
		</div> \
		';

	document.body.appendChild(div);
	
	if(document.addEventListener){
		document.addEventListener('keyup', controlHook);
	} else {
		document.attachEvent('keyup', controlHook);
	}	
	
	if(shouldStartOnLoad()){
		startRecorder();
	}
}

function removeControlHook(){
	if(document.removeEventListener){
		document.removeEventListener('keyup', controlHook);		
	} else {
		document.detachEvent('keyup', controlHook);
	}	
}