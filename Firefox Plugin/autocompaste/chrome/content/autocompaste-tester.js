var AcpTester = {

	cases: [],
    //Test cases to unit test the extension.
    onLoad: function () {
        // initialization code
    },
	
	runDriver: function() {
		//setup driver
		this.id = 0;
		this.results = "";
		var flag = false;
		
		//case 1, extract empty page.
		setTimeout( function() { AcpTester.openTab("about:blank",AcpTester.testEmptyPage);}, 1000);
		
		//case 2, extract simple page.
		setTimeout(function() {AcpTester.openTab("file:///C:/case1.htm",AcpTester.testSimpleExtraction);}, 2000);
		
		//case 3, extraction when tab opens in background.
		setTimeout(function() {AcpTester.openTabInBackground("file:///C:/case2.htm",AcpTester.testExtractionForBackgroundTab);}, 3000);
		
		//case 4, content removed when current tab is closed.
		setTimeout(function() {AcpTester.testClosingOfCurrentTab();}, 4000);
		
		//case 5, content removed when background tab is closed.
		setTimeout(function() {AcpTester.testClosingOfBackgroundTab();}, 5000);
		
		//case 6, content removed and new content loaded when open link in same tab.
		setTimeout(function() {AcpTester.openInCurrentTab("file:///C:/case2.htm",AcpTester.testChangeOfUrl);}, 6000);
	
		//setTimeout to show results
		setTimeout(function() { AcpTester.showResult();}, 12000);
	},
	
	/* ---- Helper methods ---- */
	
	//open a tab with a given url and callback after finish loading
	openTab: function(url, callback) {
		// Add tab, then make active
		gBrowser.selectedTab = gBrowser.addTab(url);
		AcpTester.callback = callback;
		// BETTER WAY
		var newTabBrowser = gBrowser.getBrowserForTab(gBrowser.selectedTab);
		newTabBrowser.addEventListener("load", function () {
			AcpTester.callback(newTabBrowser);
		}, true);
	},
	
	openTabInBackground: function(url, callback) {
		var newTabBrowser = gBrowser.getBrowserForTab(gBrowser.addTab(url));
		newTabBrowser.addEventListener("load", function () {
			callback(newTabBrowser);
		}, true);
	},
	
	openInCurrentTab: function(url, callback) {
		var newTabBrowser = gBrowser.getBrowserForTab(gBrowser.selectedTab);
		AcpTester.callback = callback;
		openUILinkIn("file:///C:/case2.htm","current");
	},
	
	newTestCase: function(description) {
		this.results = this.results + "<tr style='background-color:#d7d7d7'; line-height:2;'><td colspan='3'><strong>"+description+"</strong></td></tr>";
	},
	
	addResult: function(testCase, result) {
		var color = "#f00";
		var status = "Failed";
		this.id = this.id + 1;
		if(result)
		{
			color = "#0f0";
			status = "Passed";
		}
		this.results = this.results + "<tr style='background-color:"+ color +"'; line-height:'2';><td>"+ this.id +"</td><td>"+ testCase +"</td><td>"+ status+"</td></tr>";
	},
	
	showResult: function() {
		gBrowser.selectedTab = gBrowser.addTab('about:blank');
		var newTabBrowser = gBrowser.getBrowserForTab(gBrowser.selectedTab);
		newTabBrowser.addEventListener("load", function () {
			newTabBrowser.contentDocument.body.innerHTML = "<html xmlns='http://www.w3.org/1999/xhtml'><head><title>Test Results</title></head><body><h1>ACP Firefox Test Results</h1><table width='100%'>" + AcpTester.results + "</table></body></html>";
		}, true);
	},
	
	assertResult: function(outcome, description){
		this.addResult(description,outcome);
	},
	
	htmlToString: function(str) {
		return str.replace(/<(?:.|\n)*?>/gm, '');
	},
	
	/* ---- Test Cases ---- */
	
	//test new tab (empty page does no extraction)
	testEmptyPage: function(browser) {
		AcpTester.newTestCase("Test Case 1: Testing extraction of a blank page.");	
	
		var clonedContent = browser.contentDocument.cloneNode(true);
		AcpTester.assertResult(myExtension.oldURL == "about:blank", "Test Case 1: Source retrieved is correct.");
		var readableContent = AcpExtractManager.extractContent(clonedContent,myExtension.oldURL);
		AcpTester.assertResult(AcpTester.htmlToString(readableContent) == "", "Test Case 1: Text Extracted is empty.");
	},
	
	//test simple html page extraction
	testSimpleExtraction: function(browser) {
		AcpTester.newTestCase("Test Case 2: Testing extraction of a simple HTML page.");
	
		var clonedContent = browser.contentDocument.cloneNode(true);
		AcpTester.assertResult(myExtension.oldURL == "file:///C:/case1.htm", "Test Case 2: Source retrieved is correct.");
		AcpTester.assertResult(AcpExtractManager.hasBeenExtracted("file:///C:/case1.htm"),"Test Case 2: Page added to extracted list.");
	},
	
	//test extraction works when a tab opened in backgraound is switched to
	testExtractionForBackgroundTab: function(browser) {
		AcpTester.newTestCase("Test Case 3: Testing extraction works only when a tab is opened in the background and gets viewed.");
		
		
		//has been extracted? expecting FALSE
		AcpTester.assertResult(!AcpExtractManager.hasBeenExtracted("file:///C:/case2.htm"),"Test Case 3: Page not extracted till tab switch.");
	
		//switch tab
		gBrowser.tabContainer.advanceSelectedTab(1, true);
		browser = gBrowser.getBrowserForTab(gBrowser.selectedTab);
		
		var clonedContent = browser.contentDocument.cloneNode(true);
		AcpTester.assertResult(myExtension.oldURL == "file:///C:/case2.htm", "Test Case 3: Source retrieved is correct.");
		
		//has been extracted? expecting TRUE
		AcpTester.assertResult(AcpExtractManager.hasBeenExtracted("file:///C:/case2.htm"),"Test Case 3: Page added to extracted list.");
	},
	
	//test closing of currently viewed tab sends closing request
	testClosingOfCurrentTab: function() {
		AcpTester.newTestCase("Test Case 4: Testing closing of currently viewed tab removes document.");
		
		gBrowser.removeCurrentTab();
		
		//sets has been extracted to false? expecting FALSE
		AcpTester.assertResult(!AcpExtractManager.hasBeenExtracted("file:///C:/case2.htm"),"Test Case 4: Page removed when closing current tab.");
	},
	
	//test closing of background tab sends closing request
	testClosingOfBackgroundTab: function() {
		AcpTester.newTestCase("Test Case 5: Testing closing of background tab removes document.");
		
		gBrowser.removeTab(gBrowser.tabContainer.getItemAtIndex(0));
		
		//sets has been extracted to false? expecting FALSE
		AcpTester.assertResult(!AcpExtractManager.hasBeenExtracted("about:home"),"Test Case 5: Page removed when closing background tab.");
	},
	
	//test change of url on the current tab
	testChangeOfUrl: function(browser) {
		AcpTester.newTestCase("Test Case 6: Testing loading of new page on the current tab.");
		
		// expecting FALSE
		AcpTester.assertResult(!AcpExtractManager.hasBeenExtracted("file:///C:/case1.htm"),"Test Case 6: Page removed when loading another url.");
		
		//current url is correct
		AcpTester.assertResult(myExtension.oldURL == "file:///C:/case2.htm", "Test Case 6: The new url is correct.");

		
		//has been extracted? expecting TRUE
		AcpTester.assertResult(AcpExtractManager.hasBeenExtracted("file:///C:/case2.htm"),"Test Case 6: New page added to extracted list.");
	},
}