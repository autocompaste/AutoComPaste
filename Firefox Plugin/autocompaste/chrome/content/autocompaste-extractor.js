Components.utils.import("resource://gre/modules/NetUtil.jsm");
Components.utils.import("resource://gre/modules/FileUtils.jsm");

var AcpExtractor = {
    //function that is called each time the extension loads
    onLoad: function () {
        // initialization code
        this.initialized = true;
		myExtension.init();
		gBrowser.tabContainer.addEventListener('TabSelect', function() {myExtension.oldURL = gBrowser.selectedBrowser.currentURI.spec; AcpExtractor.onTabChange();}, true);
		gBrowser.tabContainer.addEventListener('TabClose', AcpExtractor.onTabClose, true);
		gBrowser.tabContainer.addEventListener("TabOpen", AcpExtractor.onTabOpen, false); 
		gBrowser.addEventListener("unload", AcpExtractor.onTabUnload, true);
		gBrowser.addEventListener("load", AcpExtractor.onTabLoad, true);
        //this gets the preferences which is used to store the 'count' persistently
        AcpExtractor.prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extensions.helloworld.");
		AcpExtractor.serverStatus == 'OFFLINE';
		AcpExtractor.extractionMode = AcpExtractor.prefs.getBoolPref("mode");
		setInterval("AcpExtractor.pollServer()",3000);
		//AcpTester.runDriver();
    },

	onTabOpen: function(event) {
		var browser = gBrowser.getBrowserForTab(event.target);
		/*if(browser.contentTitle!="")
  		{
			alert(browser.currentURI.spec + "is Opened!");
  		}*/
	},
	
	/*
	Description: Method called when a tab is loaded. Extract content always.
	Take Note: It does not consider the case when a tab is loading in the background.
	*/
    onTabLoad: function(event) {
    	var document = event.target;
  		if(document.title!="" && window.document.title == document.title+" - Mozilla Firefox")
  		{
			//alert('Tab Load');
			var plain = document.body.innerHTML;
			myExtension.oldURL = gBrowser.currentURI.spec;
			
			var clonedContent = document.cloneNode(true);
			var readableContent = AcpExtractManager.extractContent(clonedContent,myExtension.oldURL);
			//Application.console.log(AcpExtractManager.extractedPages);
			
			AcpExtractor.sendExtractedHTML("<html><head></head>"+readableContent+"</html>",document.title + " - Mozilla Firefox");
  		}
    },
	
	
	/*
	Description: Method called when a tab is closed. Remove content always.
	Take Note: What if two same tabs are open? (future implementation)
	*/
	onTabClose: function(event) {
		var doc = gBrowser.getBrowserForTab(event.target);
  		if(doc.contentTitle!="")
  		{
			//alert(doc.currentURI.spec + "is closed!");
			AcpExtractor.validateCloseRequest(doc.currentURI.spec);
			//AcpExtractManager.removeContent(doc.currentURI.spec);
			//AcpExtractor.sendCloseRequest(doc.currentURI.spec);
  		}
    },
	
	/*
	Description: Method called when the user navigates away from a page.
	Take Note: We need this for the case when (one window is open, and another window is completely closed) as we achieve the same by detecting change in url bar.
	*/
	onTabUnload: function(event) {
		var document = event.target;
  		if(document.title!="")
  		{
			//alert(document.documentURI);
			AcpExtractManager.removeContent(document.documentURI);
			AcpExtractor.sendCloseRequest(document.documentURI);
  		}
    },
	
	/*
	Description: Method called when the user changes the tab. We use this for lazy extraction (extract tab when it is viewed, and not previously extracted).
	Take Note: We use this method to overcome the case where background loading is not detected.
	*/
	onTabChange: function() {
		var browser =  gBrowser.selectedBrowser;
		if(!AcpExtractManager.hasBeenExtracted(browser.currentURI.spec) && browser.contentTitle != "")
		{
			//alert('Extracting Unextracted Tab');
			var clonedContent = browser.contentDocument.cloneNode(true);
			var readableContent = AcpExtractManager.extractContent(clonedContent ,browser.currentURI.spec);
			browser.contentDocument = readableContent;
			AcpExtractor.sendExtractedHTML("<html><head></head>"+readableContent+"</html>",browser.contentTitle + " - Mozilla Firefox");
		}
    },
	
	pollServer: function() {
		var request = new XMLHttpRequest();
		request.addEventListener("load", AcpExtractor.serverOnline, false);  
		request.addEventListener("error", AcpExtractor.serverOffline, false);  
		request.open('GET', 'http://127.0.0.1:5566', true); 
		request.send();
		
		//check for change in preference
		if( (AcpExtractor.extractionMode && AcpExtractor.prefs.getBoolPref("mode")) || (!AcpExtractor.extractionMode && !AcpExtractor.prefs.getBoolPref("mode")))
		{
			//here means preference has not changed since last check
		}
		else {
			AcpExtractor.extractionMode = AcpExtractor.prefs.getBoolPref("mode");
			var browserArray = gBrowser.browsers;
			for(i=0;i<browserArray.length;i++)
			{
				AcpExtractManager.removeContent(gBrowser.getBrowserAtIndex(i).currentURI.spec);
				AcpExtractor.sendCloseRequest(gBrowser.getBrowserAtIndex(i).currentURI.spec);
			}
			setTimeout("AcpExtractor.refreshExtraction()",2000);
		}
	},

	serverOnline: function() {
		if(AcpExtractor.serverStatus != 'ONLINE')
		{
			Components.classes['@mozilla.org/alerts-service;1'].getService(Components.interfaces.nsIAlertsService).showAlertNotification(null, 'AutoComPaste', 'Firefox addon is now active.', false, '', null);
			AcpExtractor.refreshExtraction();
			AcpExtractor.serverStatus = 'ONLINE';
		}
	},
	
	refreshExtraction: function() {
		var browserArray = gBrowser.browsers;
		for(i=0;i<browserArray.length;i++)
		{
			var document = gBrowser.getBrowserAtIndex(i).contentDocument;
			var url = gBrowser.getBrowserAtIndex(i).currentURI.spec;
			myExtension.oldURL = url;
			var clonedContent = document.cloneNode(true);
			var readableContent = AcpExtractManager.extractContent(clonedContent,url);
			AcpExtractor.sendExtractedHTML("<html><head></head>"+readableContent+"</html>",document.title + " - Mozilla Firefox");
		}
	},
	
	serverOffline: function() {
		if(AcpExtractor.serverStatus != 'OFFLINE')
		{
			Components.classes['@mozilla.org/alerts-service;1'].getService(Components.interfaces.nsIAlertsService).showAlertNotification(null, 'AutoComPaste', 'Firefox addon is now inactive.', false, '', null);
			AcpExtractor.serverStatus = 'OFFLINE';
		}
	},
	
	validateCloseRequest: function(url) {
		var browserArray = gBrowser.browsers;
		var count = 0;
		for(i=0;i<browserArray.length;i++)
		{
			if(gBrowser.getBrowserAtIndex(i).currentURI.spec == url)
			{
				count++;
			}
		}
		if(count <= 1) {
			AcpExtractManager.removeContent(url);
			AcpExtractor.sendCloseRequest(url);
		}
	},
	
	sendCloseRequest: function(url) {
		var reader = {
	        onInputStreamReady : function(input) {
	            var sin = Cc["@mozilla.org/scriptableinputstream;1"].createInstance(Ci.nsIScriptableInputStream);
	            sin.init(input);
	            sin.available();
	            var request = '';
	            while (sin.available()) {
	              request = request + sin.read(512);
	            }
	            alert('Received: ' + request);
	            output.write(data,data.length);
	            output.close();
	            alert(1);
	        }
	    }
		var transport = Components.classes["@mozilla.org/network/socket-transport-service;1"].getService(Components.interfaces.nsISocketTransportService).createTransport(null, 0, '127.0.0.1', 5566, null);
		var stream = transport.openOutputStream(0, 0, 0);
		//var input = transport.openInputStream(0,0,0).QueryInterface(Ci.nsIAsyncInputStream);
		var foo = {};
		foo.name = "closeSourceDocument";
		foo.parameters = new Array(url);
		var data = JSON.stringify(foo);//'{"name":"closeSourceDocument","parameters":["url"]}';
		try{
			stream.write(data, data.length);
			stream.close();
			//input.asyncWait(reader,0,0,null);
		}
		catch(e){
			alert(e);
		}
	},

    sendExtractedHTML: function(html,title) {
		
    	var reader = {
	        onInputStreamReady : function(input) {
	            var sin = Cc["@mozilla.org/scriptableinputstream;1"].createInstance(Ci.nsIScriptableInputStream);
	            sin.init(input);
	            sin.available();
	            var request = '';
	            while (sin.available()) {
	              request = request + sin.read(512);
	            }
	            alert('Received: ' + request);
	            output.write(data,data.length);
	            output.close();
	            alert(1);
	        }
	    }
		var transport = Components.classes["@mozilla.org/network/socket-transport-service;1"].getService(Components.interfaces.nsISocketTransportService).createTransport(null, 0, '127.0.0.1', 5566, null);
		var stream = transport.openOutputStream(0, 0, 0);
		//var input = transport.openInputStream(0,0,0).QueryInterface(Ci.nsIAsyncInputStream);
		var foo = {};
		foo.name = "processRawText";
		foo.parameters = new Array(html,title,myExtension.oldURL);
		var data = JSON.stringify(foo);//'{"name":"processRawText","parameters":["html",'sourceName','url']}';
	
		// First, get and initialize the converter
		var converter = Components.classes["@mozilla.org/intl/scriptableunicodeconverter"].createInstance(Components.interfaces.nsIScriptableUnicodeConverter);
		converter.charset = "UTF-8";
		var chunk = converter.ConvertFromUnicode(data);
		try{
			stream.write(chunk, chunk.length);
			stream.close();
			//input.asyncWait(reader,0,0,null);
		}
		catch(e){
			Application.console.log('No Server');
		}
    }
};

var ACP_urlBarListener = {
  QueryInterface: function(aIID)
  {
   if (aIID.equals(Components.interfaces.nsIWebProgressListener) ||
       aIID.equals(Components.interfaces.nsISupportsWeakReference) ||
       aIID.equals(Components.interfaces.nsISupports))
     return this;
   throw Components.results.NS_NOINTERFACE;
  },

  onLocationChange: function(aProgress, aRequest, aURI)
  {
    myExtension.processNewURL(aURI);
  },

  onStateChange: function(a, b, c, d) {},
  onProgressChange: function(a, b, c, d, e, f) {},
  onStatusChange: function(a, b, c, d) {},
  onSecurityChange: function(a, b, c) {}
};

var myExtension = {
  oldURL: null,
  
  init: function() {
    // Listen for webpage loads
    gBrowser.addProgressListener(ACP_urlBarListener,
        Components.interfaces.nsIWebProgress.NOTIFY_LOCATION);
  },
  
  uninit: function() {
    gBrowser.removeProgressListener(ACP_urlBarListener);
  },

  processNewURL: function(aURI) {
    if (aURI.spec == this.oldURL)
      return;
    
	var browserArray = gBrowser.browsers;
	for(i=0;i<browserArray.length;i++)
	{
		//alert(this.oldURL + "<-->" + gBrowser.getBrowserAtIndex(i).currentURI.spec);
		if(this.oldURL == gBrowser.getBrowserAtIndex(i).currentURI.spec)
			return;
	}
    // now we know the url is new...
    //alert(this.oldURL + "- is being closed!");
	AcpExtractManager.removeContent(this.oldURL);
	AcpExtractor.sendCloseRequest(this.oldURL);
    this.oldURL = aURI.spec;
  }
};

window.addEventListener("load", AcpExtractor.onLoad, false);