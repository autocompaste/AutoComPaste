var AcpExtractManager = {
	extractedPages: [],
	
	extractContent: function(document,url) {
		AcpExtractManager.extractedPages.push(url);
		var extractedContent;
		if(!AcpExtractor.prefs.getBoolPref("mode")) {
			extractedContent = AcpExtractManager.runReadability(document);
		}
		else {
			extractedContent = document.body.innerHTML;
		}
		return extractedContent;
	},
	
	removeContent: function(url) {
		var index = AcpExtractManager.extractedPages.indexOf(url);
		AcpExtractManager.extractedPages.splice(index,1);
	},
	
	hasBeenExtracted: function(url) {
		var index = AcpExtractManager.extractedPages.indexOf(url);
		if(index == -1)
			return false;
		return true;
	},
	
	runReadability: function(document) {
		
		for(var nodeIndex = 0; (node = document.getElementsByTagName('*')[nodeIndex]); nodeIndex++)
			{
				/* Remove unlikely candidates */
				if (!false) {
					var unlikelyMatchString = node.className + node.id;
					if (unlikelyMatchString.search('/combx|comment|disqus|foot|header|menu|meta|nav|rss|shoutbox|sidebar|sponsor/i') !== -1 &&
						unlikelyMatchString.search('/and|article|body|column|main/i') == -1 &&
						node.tagName !== "BODY")
					{
						dbg("Removing unlikely candidate - " + unlikelyMatchString);
						node.parentNode.removeChild(node);
						nodeIndex--;
						continue;
					}				
				}
				/*Remove all superscript tags*/
				if(node.tagName == "SUP") {
					node.innerHTML = "";
				}
				
				/* Turn all divs that don't have children block level elements into p's */
				if (node.tagName === "DIV") {
					if (node.innerHTML.search(readability.regexps.divToPElementsRe) === -1)	{
						dbg("Altering div to p");
						var newNode = document.createElement('p');
						try {
							newNode.innerHTML = node.innerHTML;				
							node.parentNode.replaceChild(newNode, node);
							nodeIndex--;
						}
						catch(e)
						{
							dbg("Could not alter div to p, probably an IE restriction, reverting back to div.")
						}
					}
					else
					{
						/* EXPERIMENTAL */
						for(var i = 0, il = node.childNodes.length; i < il; i++) {
							var childNode = node.childNodes[i];
							if(childNode.nodeType == Node.TEXT_NODE) {
								dbg("replacing text node with a p tag with the same content.");
								var p = document.createElement('p');
								p.innerHTML = childNode.nodeValue;
								p.style.display = 'inline';
								p.className = 'readability-styled';
								childNode.parentNode.replaceChild(p, childNode);
							}
						}
					}
				} 
			}
			/**
			 * Loop through all paragraphs, and assign a score to them based on how content-y they look.
			 * Then add their score to their parent node.
			 *
			 * A score is determined by things like number of commas, class names, etc. Maybe eventually link density.
			**/
			var allParagraphs = document.getElementsByTagName("p");
			var candidates    = [];

			for (var j=0; j	< allParagraphs.length; j++) {
				var parentNode      = allParagraphs[j].parentNode;
				var grandParentNode = parentNode.parentNode;
				var innerText       = readability.getInnerText(allParagraphs[j]);

				/* If this paragraph is less than 25 characters, don't even count it. */
				if(innerText.length < 25)
					continue;

				/* Initialize readability data for the parent. */
				if(typeof parentNode.readability == 'undefined')
				{
					readability.initializeNode(parentNode);
					candidates.push(parentNode);
				}
				
				/* Initialize readability data for the grandparent. */
				if(typeof grandParentNode.readability == 'undefined')
				{
					readability.initializeNode(grandParentNode);
					candidates.push(grandParentNode);
				}

				var contentScore = 0;

				/* Add a point for the paragraph itself as a base. */
				contentScore++;

				/* Add points for any commas within this paragraph */
				contentScore += innerText.split(',').length;
				
				/* For every 100 characters in this paragraph, add another point. Up to 3 points. */
				contentScore += Math.min(Math.floor(innerText.length / 100), 3);
				
				/* Add the score to the parent. The grandparent gets half. */
				parentNode.readability.contentScore += contentScore;
				grandParentNode.readability.contentScore += contentScore/2;
			}
			/**
			 * After we've calculated scores, loop through all of the possible candidate nodes we found
			 * and find the one with the highest score.
			**/
			var topCandidate = null;
			for(var i=0, il=candidates.length; i < il; i++)
			{
				/**
				 * Scale the final candidates score based on link density. Good content should have a
				 * relatively small link density (5% or less) and be mostly unaffected by this operation.
				**/
				candidates[i].readability.contentScore = candidates[i].readability.contentScore * (1-readability.getLinkDensity(candidates[i]));

				dbg('Candidate: ' + candidates[i] + " (" + candidates[i].className + ":" + candidates[i].id + ") with score " + candidates[i].readability.contentScore);

				if(!topCandidate || candidates[i].readability.contentScore > topCandidate.readability.contentScore)
					topCandidate = candidates[i];
			}
			/**
			 * If we still have no top candidate, just use the body as a last resort.
			 * We also have to copy the body node so it is something we can modify.
			 **/
			if (topCandidate == null || topCandidate.tagName == "BODY")
			{
				topCandidate = document.createElement("DIV");
				topCandidate.innerHTML = document.body.innerHTML;
				document.body.innerHTML = "";
				document.body.appendChild(topCandidate);
				readability.initializeNode(topCandidate);
			}
			
			/**
			 * Now that we have the top candidate, look through its siblings for content that might also be related.
			 * Things like preambles, content split by ads that we removed, etc.
			**/
			var articleContent        = document.createElement("DIV");
				articleContent.id     = "readability-content";
			var siblingScoreThreshold = Math.max(10, topCandidate.readability.contentScore * 0.2);
			var siblingNodes          = topCandidate.parentNode.childNodes;
			for(var i=0, il=siblingNodes.length; i < il; i++)
			{
				var siblingNode = siblingNodes[i];
				var append      = false;

				dbg("Looking at sibling node: " + siblingNode + " (" + siblingNode.className + ":" + siblingNode.id + ")" + ((typeof siblingNode.readability != 'undefined') ? (" with score " + siblingNode.readability.contentScore) : ''));
				dbg("Sibling has score " + (siblingNode.readability ? siblingNode.readability.contentScore : 'Unknown'));

				if(siblingNode === topCandidate)
				{
					append = true;
				}
				
				if(typeof siblingNode.readability != 'undefined' && siblingNode.readability.contentScore >= siblingScoreThreshold)
				{
					append = true;
				}
				
				if(siblingNode.nodeName == "P") {
					var linkDensity = readability.getLinkDensity(siblingNode);
					var nodeContent = readability.getInnerText(siblingNode);
					var nodeLength  = nodeContent.length;
					
					if(nodeLength > 80 && linkDensity < 0.25)
					{
						append = true;
					}
					else if(nodeLength < 80 && linkDensity == 0 && nodeContent.search(/\.( |$)/) !== -1)
					{
						append = true;
					}
				}

				if(append)
				{
					dbg("Appending node: " + siblingNode)

					/* Append sibling and subtract from our list because it removes the node when you append to another node */
					articleContent.appendChild(siblingNode);
					i--;
					il--;
				}
			}  
		return articleContent.innerHTML;
	},
};    