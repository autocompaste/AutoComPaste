package acp.beans.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Transient;


import acp.beans.Suggestion;

@Entity
@Table(name="Paragraph")
/**
 * Paragraph class is utilize as a mapping to database table for Hibernate using EJB 3.0 annotation standard. It is 
 * also used as a java bean to pass around the data in ACP.
 * 
 * @author Teo Kee Cheng
 */
public class Paragraph extends Suggestion{
	
	@Column(name="source_id", nullable=true)
	private int sourceID;
	
	@Column(name="parent_order", nullable=true)
	private int parentOrder;
	
	@Transient
	private List<Sentence> sentences;
	
	/**
	 * Default constructor
	 */
	public Paragraph(){}
	
	/**
	 * Constructor to initialize object for storing in database
	 */
	public Paragraph(int parentOrder) {
		super();
		this.parentOrder = parentOrder;
	}
	
	/**
	 * Get source ID of the Paragraph object.   
	 *
	 * @return			source ID of the object.
	 */
	public int getSourceID() {
		return sourceID;
	}
	
	/**
	 * Set source ID of the Paragraph object.   
	 *
	 * @param sourceID			source ID of the object.
	 */
	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}
	
	/**
	 * Get parent order of the Paragraph object.   
	 *
	 * @return			parent order of the object.
	 */
	public int getParentOrder() {
		return parentOrder;
	}
	
	/**
	 * Set parent order of the Paragraph object.   
	 *
	 * @param parentOrder			parent order of the object.
	 */
	public void setParentOrder(int parentOrder) {
		this.parentOrder = parentOrder;
	}
	
	/**
	 * Get sentences of the Paragraph object.   
	 *
	 * @return			sentences of the object.
	 */
	public List<Sentence> getSentences() {
		return sentences;
	}

	/**
	 * Set sentences of the Paragraph object.   
	 *
	 * @param sentences			sentences of the object.
	 */
	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}
	
	@Override
	/**
	 * Get content of the Paragraph object.   
	 *
	 * @return			content of the object.
	 */
	public String getContent() {
		StringBuffer content = new StringBuffer();
		for(int i = 0; i < sentences.size(); i++){
			content.append(sentences.get(i).getContent() + " ");
		}
		
		return content.toString();
	}

	@Override
	/**
	 * Get the formatted object information through a string.   
	 *
	 * @return			string format of the object.
	 */
	public String toString() {
		return "Paragraph [id=" + id + ",sourceID=" + sourceID + ", parentOrder="
				+ parentOrder + "]";
	}
	
}
