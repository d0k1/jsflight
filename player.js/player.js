function Player() {
    var fs = require('fs');
    
    var records = [];
    
    var actionChain = [];
    
    var sort = function(){
        var events = [];
        
        for(var i=0;i<records.length;i++)
        {
            var recItem = records[i];
            for(var j=0;j<recItem.length;j++)
            {
                events.push(recItem[j]);
            }
        }
        records = events;
    };
    
    var page = null;
    
    this.defaultPageReadyCheck = function(page){
        return true;
    };
    
    this.defaultGetPageForEvent = function(event){
        if(page===null){
            page = createPage();
        }
        return page;
    };
    
    var createPage = function(onReady, pageReadyCheck){
        var webPage = require('webpage');
        var page = webPage.create();
        page.onResourceRequested = function(requestData, networkRequest) {
        //   console.log('---Request (#' + requestData.id + '): ');// + JSON.stringify(requestData));
        };

        page.onResourceReceived = function(response) {        
            // console.log('Response (#' + response.id + ', stage "' + response.stage + '"): ');// + JSON.stringify(response));
        };

        var timerHandler = null;
        
        function checkFunction(){
            var result = pageReadyCheck(page);
            if(result===true){
                clearInterval(timerHandler);
                onReady(page);
            }
        }
        
        page.onConsoleMessage = function(msg, lineNum, sourceId) {
            console.log('CONSOLE: ' + msg + ' (from line #' + lineNum + ' in "' + sourceId + '")');
        };
        
        return page;
    };
    
    var createAction = function(type, event, prevAction) {
        var action = null;
        if(type=='url') {
            action = {
                type: 'url',
                event: event
            };
        }
        
        if(type=='event') {
            action = {
                type: 'event',
                event: event
            };
        }
        
        if(action!==null && prevAction!==null){
            prevAction.next = action;
        }
        return action;
    };
    
    var evaluteAction = function(page, action, next){
        if(action.type=='url'){
            
        }
        if(action.type=='event'){
            
        }
    };
    
    var createChain = function(){
        var prevAction = null;
        
        for(var i=0;i<records.length;i++){
            var action = null;
            
            action = createAction('url', records[i], prevAction);

            if(action!==null){
                actionChain.push(action);
                prevAction = action;
            }
            
            action = createAction('event', records[i], prevAction);
            if(action!==null){
                actionChain.push(action);
                prevAction = action;
            }
        }
    };

    this.init = function(path){
        console.log("Loading " +path);

        var stream = fs.open(path, 'r');
        
        while(!stream.atEnd()) {
        
            var line = stream.readLine();
            var events = JSON.parse(line);    
            records.push(events);
        }
        stream.close();
        sort();
        createChain();
        console.log('Events: '+ records.length+' Actions: '+actionChain.length);
    };
    
    var currentStep = 0;
    
    var playStep = function(){
        
    };
    
    this.play = function(){
        
    };
}

function isSDPageLoaded(page){
    return page.evaluate(function() {   
        var state = '';
        
        if(document.getElementById('state.context')!==undefined){
            state = document.getElementById('state.context').getAttribute('value');                
        }
        if(state == 'ready'){
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