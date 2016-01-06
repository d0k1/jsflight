/**
 * Event storage method
 * Event send method
 */
// jsflight namespace 
var jsflight = jsflight || {};

jsflight.getEventInfo = function(mouseEvent) {
    if (mouseEvent === undefined)
        mouseEvent = window.event;

    var target = 'target' in mouseEvent ? mouseEvent.target
            : mouseEvent.srcElement;

    mouseEvent.target = target;

    var result = {};

    result.tabuuid = jsflight.tabUuid;
    result.type = mouseEvent.type;
    result.url = window.location.href;
    result.charCode = (mouseEvent.which || mouseEvent.keyCode || mouseEvent.charCode);

    if (mouseEvent.type === 'keyup') {
        if (!event.shiftKey) {
            result.charCode = String.fromCharCode(result.charCode)
                    .toLowerCase().charCodeAt(0);
        }
    }
    result.altKey = mouseEvent.altKey;
    result.ctrlKey = mouseEvent.ctrlKey;
    result.shiftKey = mouseEvent.shiftKey;
    result.metaKey = mouseEvent.metaKey;

    result.button = mouseEvent.button;
    result.hash = window.location.hash;

    result.target = jsflight.getElementXPath(mouseEvent.target);
    result.target1 = jsflight.getTargetId(mouseEvent);

    result.timestamp = mouseEvent.timeStamp;

    result.screenX = mouseEvent.screenX;
    result.screenY = mouseEvent.screenY;

    result.pageX = mouseEvent.pageX;
    result.pageY = mouseEvent.pageY;

    result.screen.width = screen.width;
    result.screen.height = screen.height;

    result.window.width = window.outerWidth;
    result.window.height = window.outerHeight;

    result.page.width = window.innerWidth;
    result.page.height = window.innerHeight;

    result.agent = navigator.userAgent;
    result.image = mouseEvent.image;
    result.dom = mouseEvent.dom;
    result.eventId = mouseEvent.eventId;

    if (jsflight.options.propertyProvider) {
        jsflight.options.propertyProvider(result);
    }

    return result;
};

/**
 * Store event to session storage
 * 
 * @param eventid
 * @param eventdata
 */
jsflight.saveToStorage = function(eventid, eventdata) {
    if (typeof (window.sessionStorage) == "undefined") {
        console.log('No support of window.sessionStorage');
        return;
    }

    window.sessionStorage.setItem("recorder.max", eventid);
    window.sessionStorage.setItem('recorder.eventId.' + eventid, eventdata);
};

/**
 * Clear session storage
 */
jsflight.clearStorage = function() {
    jsflight.eventId = 0;
    sessionStorage.clear();
};

/**
 * send current batch of stored events and clear session storage
 */
jsflight.sendEventData = function(sendStop) {

    if (jsflight.started === false)
        return;

    if (typeof (window.sessionStorage) == "undefined") {
        console.log('No support of window.sessionStorage');
        return;
    }
    var storage = window.sessionStorage;

    var events = [];
    var keys = [];
    for ( var key in storage) {
        if (key.indexOf('recorder.eventId.') === 0) {
            events.push(storage.getItem(key));
            keys.push(key);
        }
    }

    // it is pity to send an empty array. No tracked data no xhr post
    if (events.length === 0 && sendStop === false)
        return;

    var data = JSON.stringify(events);
    var uri = jsflight.options.baseUrl + jsflight.options.downloadPath;
    if (sendStop) {
        uri += "?stop";
    }
    var xhr = new XMLHttpRequest();
    xhr.open('POST', uri, true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.onload = function() {
        if (xhr.status == 200) {
            for (var i = 0; i < keys.length; i++) {
                storage.removeItem(keys[i]);
            }
        } else {
            console.log("error storing data. status " + xhr.status);
        }
    };
    xhr.send('data=' + encodeURIComponent(data));
};
