package acp.manager;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.beans.RankEntry;
import acp.store.RankDataStore;
import acp.ui.RankDebugUI;

/**
 * RankManager is the component that contains the ranking algorithm for learning of user behavior that 
 * produces a ranking model for ranking of suggestions and handles the store component that 
 * does the storing and retrieval of past user behavior.
 * 
 * @author Ng Chin Hui
 */
public class RankManager {

	final static Logger logger = LoggerFactory.getLogger(RankManager.class);
	private final static int K = 3;	

	private RankDataStore rds;
	private ArrayList<ArrayList<RankEntry>> lowlistList;
	private ArrayList<ArrayList<RankEntry>> highlistList;
	private ArrayList<Integer> turnList; 
	private ArrayList<Integer> destinationDocumentList;
	private int destinationDocument;
	private int rank;
	private int numOfClosedDestinationDoc;
	private RankDebugUI debugUI;
	
	/**
	 * Default Constructor.
	 * Initialize the variables and retrieve past user behavior.
	 */
	public RankManager(){
		rds = new RankDataStore();
		rds.retrieveRankData();
		
		highlistList = new ArrayList<ArrayList<RankEntry>>();
		lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		highlistList.add(new ArrayList<RankEntry>());
		lowlistList.add(new ArrayList<RankEntry>());
		
		destinationDocumentList = new ArrayList<Integer>();
		turnList = new ArrayList<Integer>();
		rank = 1;
		destinationDocument = -1;
		
		numOfClosedDestinationDoc = 0;
		
		debugUI = new RankDebugUI(highlistList, lowlistList, this);
	}
	
	/**
	 * To enable testing mode for Unit testing.
	 * It will use different file for storing and retrieving of user behavior.
	 */
	public void testing_mode(){
		rds.testing_mode();
	}

	/**
	 * Register the new source document into algorithm.
	 * It will check if there are existing data and initialize with past user behavior data.
	 * Then after it is inserted into the correct position.
	 * 
	 * @param sourceDoc		the name of the source document
	 */
	public void addNewEntry(String sourceDoc){
		logger.info("addNewEntry - " + sourceDoc);

		/* Initialize the new MLEntry */
		RankEntry tempE = new RankEntry(sourceDoc, rank);
		int index = rds.getDocumentList().indexOf(sourceDoc);
		if(index > -1){
			tempE.setScore(rds.getScoreList().get(index));
			rds.removeDocumentFromList(index);
		}
		rank++;

		/* insert the MLEntry into the appropriate position */
		for(int j=0; j<destinationDocumentList.size()+1; j++){
			
			ArrayList<RankEntry> highList = highlistList.get(j);
			ArrayList<RankEntry> lowList = lowlistList.get(j);
			
			ArrayList<RankEntry> temp = new ArrayList<RankEntry>();
			
			if(highList.size() == K){
				temp.add((RankEntry)tempE.clone());
				for(int i=0; i<highList.size(); i++){					
					if(i == highList.size()-1)
						lowList.add(highList.get(i));
					else
						temp.add(highList.get(i));
				}				
				highList = temp;
				Collections.sort(lowList, new ScoreComparator());
				
			}else if(highList.size() > 0){
				temp.add((RankEntry)tempE.clone());
				temp.addAll(highList);
				highList = temp;	
				
			}else{
				highList.add((RankEntry)tempE.clone());
			}
			
			highlistList.set(j, highList);
			lowlistList.set(j, lowList);
		}
		
		debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
	}
	
	/**
	 * Promote the rank of the source document being viewed by the user.
	 * Used by sensor module.
	 * 
	 * @param sourceDoc		the name of the source document
	 */
	public void changeRanks(String sourceDoc){
		
		if(highlistList.get(0).contains(new RankEntry(sourceDoc, -1)) || lowlistList.get(0).contains(new RankEntry(sourceDoc, -1))){
			
			for(int j=0; j<destinationDocumentList.size()+1; j++){
				
				ArrayList<RankEntry> highList = highlistList.get(j);
				ArrayList<RankEntry> lowList = lowlistList.get(j);
				
				boolean isInHighList = highList.contains(new RankEntry(sourceDoc, -1));
				boolean isInLowList = lowList.contains(new RankEntry(sourceDoc, -1));
				
				ArrayList<RankEntry> temp = new ArrayList<RankEntry>();
				int index = -1;
			
				if(isInHighList){
					
					/* if it is in high list change its rank */
					index = highList.indexOf(new RankEntry(sourceDoc, -1));
					
					if(index != 0){
						logger.info("changeRanks - entry in high list: " + sourceDoc);
						
						temp.add(highList.get(index));
						highList.remove(index);
						temp.addAll(highList);
						highList = temp;
						
						highlistList.set(j, highList);
						lowlistList.set(j, lowList);
						
						debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
					}
					
				}else if(isInLowList){
					logger.info("changeRanks - entry in low list: " + sourceDoc);
					
					/* if it is in low list 
					 * shift entry to highList, the lowest ranking in highList to LowList and sort the low list 
					 * */
					index = lowList.indexOf(new RankEntry(sourceDoc, -1));
					RankEntry tempE = lowList.get(index);
					
					lowList.remove(tempE);		
					
					temp.add(tempE);
					for(int i=0; i<highList.size(); i++){					
						if(i == highList.size()-1)
							lowList.add(highList.get(i));
						else
							temp.add(highList.get(i));
					}				
					highList = temp;
					Collections.sort(lowList, new ScoreComparator());
					
					highlistList.set(j, highList);
					lowlistList.set(j, lowList);
					
					debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
				}
			}
		}
	}

	/**
	 * Increases the score of the most recently pasted suggestion of a particular source document.
	 * Updates the score of all source documents according to the turn of last usage(pasting)
	 * Also, promote the rank of the source document if it is in high priority list 
	 * and keep track of the position of the suggestion in the source document.
	 * 
	 * @param lastUsedParagraph		the position of the paragraph in the source document
	 * @param lastUsedSentence		the position of the sentence in the paragraph
	 * @param sourceDoc				the name of the source document
	 */
	public void learnUserBehaviour(int lastUsedParagraph, int lastUsedSentence, String sourceDoc){
		if(destinationDocument == -1){
			logger.info("No word processor open");
		}else{
			logger.info("learnUserBehaviour - " + sourceDoc);
			
			int destinationDocIndex = destinationDocumentList.indexOf(destinationDocument);
			int turn = turnList.get(destinationDocIndex);
			
			ArrayList<RankEntry> highList = highlistList.get(destinationDocIndex + 1);
			ArrayList<RankEntry> lowList = lowlistList.get(destinationDocIndex + 1);
			
			boolean isInHighList = highList.contains(new RankEntry(sourceDoc, -1));
			boolean isInLowList = lowList.contains(new RankEntry(sourceDoc, -1));
			
			RankEntry e = null;
			int index = -1;
			ArrayList<RankEntry> temp = new ArrayList<RankEntry>();
			
			
			if(isInHighList){
				index  = highList.indexOf(new RankEntry(sourceDoc, -1));
				e = highList.get(index);
				
				/* increase score of the source document */
				double currentScore = e.getScore();
				e.setScore(currentScore + 100);
				e.setLastUsedParahraph(lastUsedParagraph);
				e.setLastUsedSentence(lastUsedSentence);
				e.setTurnOfLastUse(turn);
				
				/* promote the rank of the source document */
				temp.add(e);
				highList.remove(index);
				temp.addAll(highList);
				highList = temp;
				
			}else if(isInLowList){
				index  = lowList.indexOf(new RankEntry(sourceDoc, -1));
				e = lowList.get(index);
				
				/* increase score of the source document */
				double currentScore = e.getScore();
				e.setScore(currentScore + 100);
				e.setLastUsedParahraph(lastUsedParagraph);
				e.setLastUsedSentence(lastUsedSentence);
				e.setTurnOfLastUse(turn);
			}

			/* update all the scores of all instance in both lists */
			turn++;
			turnList.set(destinationDocIndex, turn);
			updateScores(highList, destinationDocIndex);
			updateScores(lowList, destinationDocIndex);	
			Collections.sort(lowList, new ScoreComparator());
			
			highlistList.set(destinationDocIndex + 1, highList);
			lowlistList.set(destinationDocIndex + 1, lowList);
			
			debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
		}
	}

	/**
	 * Updates the scores of the entries(source documents) in the list(high or low) 
	 * according to the change in turn.
	 * 
	 * This method is used when learning user behavior.
	 * 
	 * @param list						the data structure used to keep track of the ranks of each entry(source document)
	 * @param destinationDocIndex		the index of the destination document(e.g. MS Word)
	 */
	private void updateScores(ArrayList<RankEntry> list, int destinationDocIndex){
		for(RankEntry e : list){
			double currentScore = e.getScore();
			int turnOfLastUse = e.getTurnOfLastUse();
			if(turnOfLastUse != -1){
				int turnsAgo = turnList.get(destinationDocIndex) - turnOfLastUse;

				if(turnsAgo >= 91)	
					e.setScore(currentScore * 0.1);
				else if(turnsAgo <= 90 && turnsAgo >= 32)
					e.setScore(currentScore * 0.3);
				else if(turnsAgo <= 31 && turnsAgo >= 15)
					e.setScore(currentScore * 0.5);
				else if(turnsAgo <= 14 && turnsAgo >= 5)
					e.setScore(currentScore * 0.7);
			}
		}		
	}
	
	/**
	 * Removal of the source document entry from the algorithm after the source document is closed.
	 * 
	 * @param sourceDoc	the name of the source document
	 */
	public void removeRankData(String sourceDoc){
		logger.info("removeRankData - " + sourceDoc);

		double overallScore = 0.0;
		
		for(int j=0; j<highlistList.size(); j++){ 	// **highlistList and lowlistList have the same size
			
			ArrayList<RankEntry> highList = highlistList.get(j);
			ArrayList<RankEntry> lowList = lowlistList.get(j);
			
			boolean isInHighList = highList.contains(new RankEntry(sourceDoc, -1));
			boolean isInLowList = lowList.contains(new RankEntry(sourceDoc, -1));

			RankEntry e = null;
			int index = -1;
			
			if(isInHighList){
				
				index = highList.indexOf(new RankEntry(sourceDoc, -1));
				e = highList.get(index);
				if(j!=0 && this.destinationDocumentList.size() > 0){
					double score = e.getScore();
					overallScore += score;
				}
				highList.remove(index);
				
				/* shift the highest ranking entry in low list to high list last rank */
				if(lowList.size() > 0){
					e = lowList.remove(0);
					highList.add(K-1, e);
				}
				
			}else if(isInLowList){
				
				index = lowList.indexOf(new RankEntry(sourceDoc, -1));
				e = lowList.get(index);
				if(j!=0 && this.destinationDocumentList.size() > 0){
					double score = e.getScore();
					overallScore += score;
				}
				lowList.remove(e);
			}
			
			highlistList.set(j, highList);
			lowlistList.set(j, lowList);
		}
		
		/* safe keep the MLData */
		if(destinationDocumentList.size() > 0)
			overallScore = overallScore / destinationDocumentList.size();
		
		if(overallScore != 0)
			rds.updateDocumentList(sourceDoc, overallScore);

		debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
	}
	
	/**
	 * Remove of all entries from the algorithm.
	 */
	public void removeAllRankData(){
		logger.info("removeAllRankData");
		
		/* combine all entries into one list */
		for(int i=0; i<highlistList.size(); i++){
			ArrayList<RankEntry> highList = highlistList.get(i);
			ArrayList<RankEntry> lowList = lowlistList.get(i);
			
			highList.addAll(lowList);
			
			highlistList.set(i, highList);
		}
		
		/* making sure that all the combined list has the same order */
		for(ArrayList<RankEntry> list : highlistList)
			Collections.sort(list, new LaunchSequenceComparator());
		
		/* The first element in highlist-List, is used as a reference */
		ArrayList<RankEntry> referenceList = highlistList.get(0);
		
		/* updating the scores */
		for(int i=0; i<referenceList.size(); i++){
			
			RankEntry e_ref = referenceList.get(i);
			
			//if(destinationDocumentList.size() != 0)
				//e_ref.setScore(0.0);
			
			for(int j=1; j<highlistList.size(); j++){
				ArrayList<RankEntry> list = highlistList.get(j);
				double currentScore = e_ref.getScore();
				RankEntry e = list.get(i);
				e_ref.setScore(currentScore + e.getScore());
			}
		}
		
		/* update to MLStore */
		for(RankEntry e : referenceList){
			double currentScore = e.getScore();
			
			//System.out.println(currentScore+" "+destinationDocumentList.size()+" "+numOfClosedDestinationDoc);
			
			if(destinationDocumentList.size() != 0)
				currentScore = currentScore / (destinationDocumentList.size() + numOfClosedDestinationDoc);
			if(currentScore != 0)
				rds.updateDocumentList(e.getSource(), currentScore);
		}
		
		/* clearing up the lists */
		for(ArrayList<RankEntry> list : highlistList)
			list.clear();
		for(ArrayList<RankEntry> list : lowlistList)
			list.clear();
		
		if(numOfClosedDestinationDoc != 0)
			numOfClosedDestinationDoc = 0;
		
		debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
	}
	
	/**
	 * Store the data collected from the user behavior by the algorithm.
	 */
	public void storeUserBehavior(){
		
		/* ensure that all ML data is recorded */
		for(int i=0; i<highlistList.size(); i++){
			ArrayList<RankEntry> list = highlistList.get(i);
			if(!list.isEmpty()){
				removeAllRankData();
			}
		}
		
		rds.storeUserBehaviour();
	}

	/**
	 * Method is used when require user behavior for sorting of suggestions.
	 * The method produces a ranking model for the sorting algorithm to use.
	 * 
	 * @return		a ranking model based on user behavior for sorting of suggestions
	 */
	public ArrayList<RankEntry> getRankingModel(){
		ArrayList<RankEntry> rankingModel = new ArrayList<RankEntry>();
		if(destinationDocument == -1){
			logger.info("No word processor open");
		}else{
			logger.info("getRankingModel");
			
			int destinationDocIndex = destinationDocumentList.indexOf(destinationDocument) + 1;
			ArrayList<RankEntry> highList = highlistList.get(destinationDocIndex);
			ArrayList<RankEntry> lowList = lowlistList.get(destinationDocIndex);
			
			for(RankEntry e : highList)
				rankingModel.add(e);
			for(RankEntry e : lowList)
				rankingModel.add(e);
		}	
		return rankingModel;
	}
	
	/**
	 * Keep tracks of the active destination document. If the destination document is new, 
	 * it will be registered.
	 * 
	 * @param destinationDoc		the ID of the destination document
	 */
	public void setDestinationDocument(int destinationDoc) {
		if(destinationDocument != destinationDoc){
			logger.info("Set Destination Document, ID: " + destinationDoc);
			
			this.destinationDocument = destinationDoc;
			addDestinationDocument(destinationDoc);
		}
	}
	/**
	 * Register the destination document.
	 * 
	 * @param destinationDoc	the ID of the destination document
	 */
	private void addDestinationDocument(int destinationDoc){
		if(!destinationDocumentList.contains(destinationDoc)){
			logger.info("Add Destination Document - ID: " + destinationDoc);
			
			/* get the updated score(avg) from existing source documents list*/
			updateReferenceList();
			
			/* keep track of the destination document */
			destinationDocumentList.add(destinationDoc);
			turnList.add(0);
			
			/* construct the high and low lists using first highList-List and lowList-List*/
			ArrayList<RankEntry> highList = new ArrayList<RankEntry>();
			ArrayList<RankEntry> lowList = new ArrayList<RankEntry>();
			for(RankEntry e : highlistList.get(0)){
				highList.add((RankEntry)e.clone());
				if(numOfClosedDestinationDoc == 0)
					e.setScore(0);
			}
			for(RankEntry e : lowlistList.get(0)){
				lowList.add((RankEntry)e.clone());	
				if(numOfClosedDestinationDoc == 0)
					e.setScore(0);
			}
				
			highlistList.add(highList);
			lowlistList.add(lowList);
			
			debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
		}
	}
	
	/**
	 * Updates the score first list of highlist-List and lowlist-List by taking 
	 * the average score of all source document entries of all destination document.
	 * 
	 * First list of highlist-List and lowlist-List is used for initializing for new destination document.
	 * 
	 * This method is used when registering a new destination document.
	 */
	private void updateReferenceList(){
		if(destinationDocumentList.size() != 0){
			ArrayList<RankEntry> highList = highlistList.get(0); 	//index = 0;
			ArrayList<RankEntry> lowList = lowlistList.get(0);	//index = 0;
			
			/* updating the scores of reference high list*/
			for(int i=0; i<highList.size(); i++){
				
				RankEntry e_ref = highList.get(i);
				double currentScore = 0.0;
				if(numOfClosedDestinationDoc != 0)
					currentScore = e_ref.getScore();
				
				for(int j=1; j<highlistList.size(); j++){
					ArrayList<RankEntry> list = highlistList.get(j);
					int index = list.indexOf(e_ref);
					if(index != -1)
						currentScore += list.get(index).getScore();
				}
				for(int j=1; j<lowlistList.size(); j++){
					ArrayList<RankEntry> list = lowlistList.get(j);
					int index = list.indexOf(e_ref);
					if(index != -1)
						currentScore += list.get(index).getScore();
				}
				
				e_ref.setScore(currentScore / (destinationDocumentList.size() + numOfClosedDestinationDoc));
			}
			
			/* updating the scores of reference low list*/
			for(int i=0; i<lowList.size(); i++){
				
				RankEntry e_ref = lowList.get(i);
				double currentScore = 0.0;
				if(numOfClosedDestinationDoc != 0)
					currentScore = e_ref.getScore();
				
				for(int j=1; j<highlistList.size(); j++){
					ArrayList<RankEntry> list = highlistList.get(j);
					int index = list.indexOf(e_ref);
					if(index != -1)
						currentScore += list.get(index).getScore();
				}
				for(int j=1; j<lowlistList.size(); j++){
					ArrayList<RankEntry> list = lowlistList.get(j);
					int index = list.indexOf(e_ref);
					if(index != -1)
						currentScore += list.get(index).getScore();
				}
				
				e_ref.setScore(currentScore / (destinationDocumentList.size() + numOfClosedDestinationDoc));
			}
			
			numOfClosedDestinationDoc = 0;
		}
	}
	
	/**
	 * Removes the destination document entry from the algorithm as well as removes it's high and low list
	 * from the highlist-List and lowlist-List.
	 * 
	 * Stores the data removed into reference list(i.e. 1st list in highlist-List and lowlist-List).
	 * 
	 * @param destinationDoc	the ID of the destination document
	 */
	public void removeDestinationDocument(int destinationDoc){
		
		if(destinationDocumentList.contains(destinationDoc)){
			
			logger.info("removeDestinationDocument - ID: " + destinationDoc);
			
			int destinationDocIndex = destinationDocumentList.indexOf(destinationDoc);
			
			ArrayList<RankEntry> highList_reference = highlistList.get(0);
			ArrayList<RankEntry> lowList_reference = lowlistList.get(0);
			
			ArrayList<RankEntry> highList = highlistList.get(destinationDocIndex+1);
			ArrayList<RankEntry> lowList = lowlistList.get(destinationDocIndex+1);
			
			/* updating the scores of reference high list*/
			for(int i=0; i<highList_reference.size(); i++){
				
				RankEntry e_ref = highList_reference.get(i);
				double currentScore = e_ref.getScore();
				
				int index = highList.indexOf(e_ref);
				if(index != -1){
					currentScore += highList.get(index).getScore();
				}else{
					index = lowList.indexOf(e_ref);
					currentScore += lowList.get(index).getScore();
				}
				
				e_ref.setScore(currentScore);
			}
			
			/* updating the scores of reference low list*/
			for(int i=0; i<lowList_reference.size(); i++){
				
				RankEntry e_ref = lowList_reference.get(i);
				double currentScore = e_ref.getScore();
				
				int index = highList.indexOf(e_ref);
				if(index != -1){
					currentScore += highList.get(index).getScore();
				}else{
					index = lowList.indexOf(e_ref);
					currentScore += lowList.get(index).getScore();
				}
				
				e_ref.setScore(currentScore);
			}
			
			numOfClosedDestinationDoc += 1;
			
			destinationDocumentList.remove(destinationDocIndex);
			highlistList.remove(destinationDocIndex+1);
			lowlistList.remove(destinationDocIndex+1);
			turnList.remove(destinationDocIndex);
			
			debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
		}
	}

	/**
	 * Get the list of high list.
	 * 
	 * @return the list of high list
	 */
	public ArrayList<ArrayList<RankEntry>> getLowlistList() {
		return lowlistList;
	}

	/**
	 * Get the list of low list.
	 * 
	 * @return the list of low list
	 */
	public ArrayList<ArrayList<RankEntry>> getHighlistList() {
		return highlistList;
	}
	
	/**
	 * Get the list of destination documents
	 * 
	 * @return the list of destination documents
	 */
	public ArrayList<Integer> getDestinationDocumentList(){
		return destinationDocumentList;
	}

	/**
	 * To enable the debugUI
	 */
	public void showDebugUI(){
		debugUI.showDebugUI();
	}
	
	/**
	 * To disable debugUI
	 */
	public void closeDebugUI(){
		debugUI.closeDebugUI();
	}
	
	/**
	 * For UI Testing purpose. To reset the data for new set of test scenario.
	 */
	public void resetData(){
		logger.info("Reset Ranking Data");
		
		for(ArrayList<RankEntry> list : highlistList){
			for(RankEntry e : list){
				e.setLastUsedParahraph(-1);
				e.setLastUsedSentence(-1);
				e.setTurnOfLastUse(-1);
				e.setScore(0.0);
			}
		}
		for(ArrayList<RankEntry> list : lowlistList){
			for(RankEntry e : list){
				e.setLastUsedParahraph(-1);
				e.setLastUsedSentence(-1);
				e.setTurnOfLastUse(-1);
				e.setScore(0.0);
			}
		}
		
		debugUI.refreshTreePane(highlistList, lowlistList, destinationDocumentList);
	}
	
}

/**
 * A comparator class to enable the use of built in sorting mechanism in java Collection class.
 * Compare first by score(higher score will be rank higher), turn of last use(more recent turn will be rank higher) 
 * and finally sequence in which the source document is launched(more recent will be rank higher).
 * 
 * @author Ng Chin Hui
 */
class ScoreComparator implements Comparator<RankEntry>{ 
	
	@Override
	public int compare(RankEntry arg0, RankEntry arg1) {	

		// rank by(first -> last): score -> turn of last use -> rank of addition
		int result;
		if(Double.compare(arg0.getScore(), arg1.getScore()) == 0){
			if(Double.compare(arg0.getTurnOfLastUse(), arg1.getTurnOfLastUse()) == 0){
				result = Integer.compare(-arg0.getRankOfAddition(), -arg1.getRankOfAddition());
			}else
				result = Integer.compare(-arg0.getTurnOfLastUse(), -arg1.getTurnOfLastUse());
		}else
			result = Double.compare(-arg0.getScore(), -arg1.getScore());		
		return result;		
	}
}

/**
 * A comparator class to enable the use of built in sorting mechanism in java Collection class.
 * Compare by the sequence which the source document is launched.
 * 
 * @author Ng Chin Hui
 */
class LaunchSequenceComparator implements Comparator<RankEntry>{ 
	
	@Override
	public int compare(RankEntry arg0, RankEntry arg1) {		
		return  Integer.compare(-arg0.getRankOfAddition(), -arg1.getRankOfAddition());		
	}
}
