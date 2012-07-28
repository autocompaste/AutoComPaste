package acp.beans;

/**
 * RankEntry is the base data structure used by the ranking algorithm.
 * Each source document is represented by one object.
 * 
 * @author Ng Chin Hui
 */
public class RankEntry implements Cloneable{
	private String source;
	private int rankOfAddition;
	private double score;
	private int lastUsedParahraph;
	private int lastUsedSentence;
	private int turnOfLastUse;
	
	/**
	 * Default constructor
	 * 
	 * @param source			the name of the source document
	 * @param rankOfAddition	the rank in which the source document is registered
	 */
	public RankEntry(String source, int rankOfAddition){
		this.source = source;
		this.rankOfAddition = rankOfAddition;
		this.score = 0.0;
		this.lastUsedParahraph = -1;
		this.lastUsedSentence = -1;
		this.turnOfLastUse = -1;
	}
	
	/**
	 * Get the name of the source document the object is referencing.
	 * 
	 * @return		the name of the source document
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Set the name of the source document the object is referencing.
	 * 
	 * @param source	the name of the source document
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	/**
	 * Get the rank in which the object/source document is registered
	 * 
	 * @return		the rank of registration of the object/source document
	 */
	public int getRankOfAddition() {
		return rankOfAddition;
	}
	
	/**
	 * Get the score of the object/source document.
	 * 
	 * @return		the score of the object/source document
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Set the score of the object/source document.
	 * 
	 * @param score		the score of the object/source document
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Get the last used paragraph id of the object/source document
	 * 
	 * @return		the last used paragraph id of the object/source document
	 */
	public int getLastUsedParahraph() {
		return lastUsedParahraph;
	}

	/**
	 * Set the last used paragraph id of the object/source document
	 * 
	 * @param lastUsedParahraph		the id of the last used paragraph
	 */
	public void setLastUsedParahraph(int lastUsedParahraph) {
		this.lastUsedParahraph = lastUsedParahraph;
	}

	/**
	 * Get the last used sentence id of the object/source document
	 * 
	 * @return		the id of the last used paragraph
	 */
	public int getLastUsedSentence() {
		return lastUsedSentence;
	}

	/**
	 * Set the last used sentence id of the object/source document
	 * 
	 * @param lastUsedSentence		the id of the last used sentence
	 */
	public void setLastUsedSentence(int lastUsedSentence) {
		this.lastUsedSentence = lastUsedSentence;
	}
	
	/**
	 * Get the turn of last used of the object/source document 
	 * 
	 * @return		the turn of last use
	 */
	public int getTurnOfLastUse() {
		return turnOfLastUse;
	}

	/**
	 * Set the turn of last used of the object/source document
	 * 
	 * @param turnOfLastUse		the turn of last use
	 */
	public void setTurnOfLastUse(int turnOfLastUse) {
		this.turnOfLastUse = turnOfLastUse;
	}

	@Override
	/**
	 * Returns a string representation of the object. (source document name)
	 * 
	 * @return		a string representation of the object. 
	 */
	public String toString(){
		return this.getSource();
	}
	
	@Override
	/**
	 * Creates and returns a copy of this object
	 * 
	 * @return		a exact copy of this object
	 */
	public Object clone(){         
		try{             
			return super.clone();         
		}catch(CloneNotSupportedException e){      
			return null;          
		}     
	}
	
	@Override
	/**
	 * Indicates whether some other object is "equal to" this one by comparing the source document name.
	 * true - both objects have the same source document name.
	 * false - does not have the same source document name.
	 * 
	 * @return		the result of this operation
	 */
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		/* object must be MLEntry at this point 
		 * using source variable as the 'key'
		 * */
		RankEntry entry = (RankEntry)obj;
		return (source == entry.source || (source != null && source.equals(entry.source)));
	}
}