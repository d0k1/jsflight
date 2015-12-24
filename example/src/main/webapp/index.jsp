<!-- This is an example of injecting JSFlightRecorder --> 
<html>
<head>
    <!-- Add javascript modules -->
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=css.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=dom.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=xpath.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=xml.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=str.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=css-selector-generator.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=html2canvas.js" type="text/javascript"></script>
    
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=options.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=global.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=storage.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=tracking.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/jsflight/recorder?int=control_panel.js" type="text/javascript"></script>
    <!-- Add main javascript module -->
    <script src="<%=request.getContextPath()%>/jsflight/recorder" type="text/javascript"></script>
    <script>
    /*
        //Set desired options
        // below listed default options
		jsflight.options = {
		        baseUrl:'',
		        downloadPath:'/jsflight/recorder/download',
		        statusPath:'/jsflight/recorder/status',
		        trackMouse:false,
		        trackHash:false,
		        trackXhr:false,
		        propertyProvider: function(prop){}
		}
    */
    jsflight.addJSFlightHooksOnDocumentLoad({
    	baseUrl:'<%=request.getContextPath()%>',
    	trackHash:true,
    	trackXhr:true,
    	cp_disabled:false,
    	send_interval: -1,
    	saveInitialDom: true,
    	saveInitialScreenshot: true,
    	saveShotOnHashChange: true,
    	saveDomOnHashChange: true, 
    	propertyProvider:function(prop){
    		prop['customProp'] = (new Date()).getTime();
    	}
    	})
    </script>
    <script>
        // sample method to do xhr request
	    function loadXMLDoc(url) {
	    	  var xmlhttp;
	    	    
	    	   if (window.XMLHttpRequest) {
	    	    xmlhttp=new XMLHttpRequest();
	    	  }
	    	  else {
	    	    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	    	  }
	    	    
	    	  xmlhttp.onreadystatechange=function() {
	    	    if (xmlhttp.readyState==4 && xmlhttp.status==200) {
	    	        //document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
	    	    }
	    	  }
	    	  xmlhttp.open("GET", url,true);
	    	  xmlhttp.send("this is request body");
	    	}
        // sample method to do xhr request
	    function emptyRequestXMLDoc(url) {
	    	  var xmlhttp;
	    	    
	    	   if (window.XMLHttpRequest) {
	    	    xmlhttp=new XMLHttpRequest();
	    	  }
	    	  else {
	    	    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	    	  }
	    	    
	    	  xmlhttp.onreadystatechange=function() {
	    	    if (xmlhttp.readyState==4 && xmlhttp.status==200) {
	    	        //document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
	    	    }
	    	  }
	    	  xmlhttp.open("GET", url,true);
	    	  xmlhttp.send();
	    	}
    </script>
</head>
<body style="color: white;">
    <!-- example ui to track -->
    <form id="test">
        <input id="btn1" type="button" value="b0"/>
        <div id="d1">123</div>
        <input id="btn2" type="button" value="b1"/> 
        <button onclick="loadXMLDoc('<%=request.getContextPath()%>')">XHR ok</button>
        <button onclick="loadXMLDoc('<%=request.getContextPath()%>/12/2/12/3')">XHR 404</button>
        <button onclick="emptyRequestXMLDoc('<%=request.getContextPath()%>')">XHR with empty send</button>
        <br/>
        <textarea id='a1' rows="10" cols="80">this is a text to debug keypress</textarea>
    </form>
    <a href='#testHash'>testHash</a>
    <a href='#'>back from testHash</a>
</body>
</html>