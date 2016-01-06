/**
 * Methods for configuration
 */
// jsflight namespace 
var jsflight = jsflight || {};

/* =================================================================================================================================== */
// recorder options
jsflight.options = {
    // should record be started after page loaded
    autostart : false,
    // baseUrl to reach servlet. useful when logger is outside an web-app
    baseUrl : '',
    // url to find servlet to send data
    downloadPath : '/jsflight/recorder/storage',
    // url to find servlet to view status
    statusPath : '/jsflight/recorder/status',
    // save initial dom after document has loaded
    saveInitialDom : false,
    // save page shot when document loaded
    saveInitialScreenshot : false,
    // take screenshot on hash change
    saveShotOnHashChange : false,
    // take DOM snapshot when has changing
    saveDomOnHashChange : false,
    // track mouse movements
    trackMouse : false,
    // track url hash change
    trackHash : false,
    // track xhr request/response
    trackXhr : false,
    // time to track, milliseconds
    track_duration : -1,
    // time interval to send tracked data, milliseconds
    send_interval : -1,
    // control panel disabled by default
    cp_disabled : true,
    propertyProvider : function(prop) {
    }
};
/* =================================================================================================================================== */

jsflight.parseOptions = function(options) {
    if (options.saveInitialDom)
        jsflight.options.saveInitialDom = options.saveInitialDom;

    if (options.saveInitialScreenshot)
        jsflight.options.saveInitialScreenshot = options.saveInitialScreenshot;

    if (options.saveShotOnHashChange)
        jsflight.options.saveShotOnHashChange = options.saveShotOnHashChange;

    if (options.saveDomOnHashChange)
        jsflight.options.saveDomOnHashChange = options.saveDomOnHashChange;

    if (options.cp_disabled === false)
        jsflight.options.cp_disabled = false;

    if (options.autostart)
        jsflight.options.autostart = options.autostart;

    if (options.baseUrl)
        jsflight.options.baseUrl = options.baseUrl;

    if (options.downloadPath)
        jsflight.options.downloadPath = options.downloadPath;

    if (options.statusPath)
        jsflight.options.statusPath = options.statusPath;

    if (options.trackMouse)
        jsflight.options.trackMouse = options.trackMouse;

    if (options.trackHash)
        jsflight.options.trackHash = options.trackHash;

    if (options.trackXhr)
        jsflight.options.trackXhr = options.trackXhr;

    if (options.propertyProvider)
        jsflight.options.propertyProvider = options.propertyProvider;

    if (options.send_interval)
        jsflight.options.send_interval = options.send_interval;

    if (options.track_duration)
        jsflight.options.track_duration = options.track_duration;
};