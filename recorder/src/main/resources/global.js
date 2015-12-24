/**
 * Global variables
 */
// jsflight namespace 
var jsflight = jsflight || {}

/** Global variables * */
//periodically send tracked data timer variable
jsflight.send_timer = null;

//After track_duration seconds browser will stop tracking events. timer
//variable.
jsflight.stop_timer = null;

//event id
jsflight.eventId = 0;

//browser window/tab uuid
jsflight.tabUuid = '';

//id to compare xhr tacked data what was sent, what was received back
jsflight.xhrId = 0;

//variable to determine if tracked was started
jsflight.started = false;

