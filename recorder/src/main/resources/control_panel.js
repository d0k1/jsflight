/**
 * JSFlight control panel
 */
// jsflight namespace 
var jsflight = jsflight || {};

/**
 * Convert all event to json
 * 
 * @returns
 */
jsflight.getEventsAsString = function() {
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

jsflight.addControlPanel = function() {
    var script = document.createElement('script')
    script.type = 'text/javascript';
    script.charset = 'utf-8';

    script.text = ' \
        function flight_hide(){ \
            var panel = document.getElementById("flight-cp"); \
            panel.style.display="none"; \
        }\
        function flight_getEvents(){ \
            document.getElementById("data").value = jsflight.getEventsAsString(); \
            return true; \
        } \
        function flight_store(){ \
            jsflight.sendEventData(false); \
        } \
        function flight_start(){ \
            flight_hide(); \
            jsflight.startRecorder(); \
        } \
        function flight_stop(){ \
            jsflight.sendEventData(true); \
            jsflight.stopRecorder(); \
        } \
        function flight_clear(){ \
            jsflight.stopRecorder(); \
            jsflight.clearStorage(); \
        }';
    document.body.appendChild(script);

    var css = document.createElement('style');
    css.type = 'text/css';

    var styles = '.modalDialog {position: fixed; font-family: Arial, Helvetica, sans-serif; top: 0; right: 0; bottom: 0; left: 0; background: rgba(0, 0, 0, 0.8); z-index: 99999; -webkit-transition: opacity 400ms ease-in; -moz-transition: opacity 400ms ease-in; transition: opacity 400ms ease-in; } .modalDialog:target {opacity:1; } .modalDialog > div {width: 400px; position: relative; margin: 10% auto; padding: 5px 20px 13px 20px; border-radius: 10px; background: #fff; background: -moz-linear-gradient(#fff, #999); background: -webkit-linear-gradient(#fff, #999); background: -o-linear-gradient(#fff, #999); } .close {background: #606061; color: #FFFFFF; line-height: 25px; position: absolute; right: -12px; text-align: center; top: -10px; width: 24px; text-decoration: none; font-weight: bold; -webkit-border-radius: 12px; -moz-border-radius: 12px; border-radius: 12px; -moz-box-shadow: 1px 1px 3px #000; -webkit-box-shadow: 1px 1px 3px #000; box-shadow: 1px 1px 3px #000; } .close:hover {background: #00d9ff; }';

    if (css.styleSheet)
        css.styleSheet.cssText = styles;
    else
        css.appendChild(document.createTextNode(styles));

    document.getElementsByTagName("head")[0].appendChild(css);

    var div = document.createElement("div");
    div.id = "flight-cp";
    div.className = "modalDialog";
    div.style.display = 'none';

    div.innerHTML = '<div style="color:black"> \
           <h1>JSFlight</h1><h2>Control panel</h2> \
           <form action="'
            + jsflight.options.baseUrl
            + jsflight.options.downloadPath
            + '?download" method="post" target="_blank"> \
               <input id="data" type="hidden" value="secret" name="data"/> \
               <input type="submit" value="Download recording" onclick="flight_getEvents()"/> \
           </form>\
            <button id="no-track-flight-cp1" onclick="flight_start()">Start</button> \
            <button id="no-track-flight-cp2" onclick="flight_stop()">Stop</button> \
            <button id="no-track-flight-cp0" onclick="flight_store()">Store</button> \
            <button id="no-track-flight-cp3" onclick="flight_clear()">Clear</button> \
            <button id="no-track-flight-cp4" onclick="flight_hide()">Hide</button> \
            <button id="no-track-flight-cp5" onclick="window.open(\''
            + jsflight.options.baseUrl
            + jsflight.options.statusPath
            + '\', \'_blank\')">Status</button> \
            <button id="no-track-flight-cp6" onclick="jsflight.options.trackMouse=true;">Track Move</button> \
            <button id="no-track-flight-cp7" onclick="jsflight.options.trackMouse=false">Dont Track Move</button> \
            <h6><a href="https://github.com/d0k1/jsflight">https://github.com/d0k1/jsflight</a></h6> \
            </div>';

    document.body.appendChild(div);

    if (document.addEventListener) {
        document.addEventListener('keyup', jsflight.controlHook);
    } else {
        document.attachEvent('keyup', jsflight.controlHook);
    }
};

/**
 * Inject control panel specific js-code and markup
 */
jsflight.addControlHook = function() {
    if (jsflight.options.cp_disabled === false) {
        jsflight.addControlPanel();
    }

    jsflight.tabUuid = jsflight.guid();

    if (jsflight.shouldStartOnLoad()) {
        jsflight.startRecorder();
    }
};

/**
 * Clean up when tab is closing
 */
jsflight.removeControlHook = function() {
    if (document.removeEventListener) {
        document.removeEventListener('keyup', jsflight.controlHook);
    } else {
        document.detachEvent('keyup', jsflight.controlHook);
    }
};


/**
 * method to show/hide control panel
 * 
 * @param event
 */
jsflight.controlHook = function(event) {
    if (event.ctrlKey && event.altKey && event.shiftKey && (event.which || event.keyCode) == 38) {
        var panel = document.getElementById("flight-cp");
        panel.style.display = 'block';
        panel.className = 'modalDialog';
    }
    if (event.ctrlKey && event.altKey && event.shiftKey && (event.which || event.keyCode) == 40) {
        var panel1 = document.getElementById("flight-cp");
        panel1.style.display = 'none';
    }
};
