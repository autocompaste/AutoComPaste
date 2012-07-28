package acp.beans;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.search.annotations.DocumentId;

import acp.beans.entity.Source;


@MappedSuperclass
/**
 * Suggestion class is utilize as super class for all suggestion type classes (Entity, Sentence, Paragraph). 
 * 
 * @author Teo Kee Cheng
 */
public abstract class Suggestion {
	
	public static final int SENTENCE = 101;
	public static final int PARAGRAPH = 102;
	public static final int ENTITY = 103;
	
	@Id
	@DocumentId
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id", nullable=false)
	protected int id;
	
	@Transient
	protected int type;

	@Transient
	protected Source source;
	
	/**
	 * Get ID of the Suggestion object.   
	 *
	 * @return			ID of the object.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set ID of the Suggestion object.   
	 *
	 * @param id			ID of the object.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get suggestion type of the Suggestion object.   
	 *
	 * @return			suggestion type of the object.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Set suggestion type of the Suggestion object.   
	 *
	 * @param type			suggestion type of the object.
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * Get source object of the Suggestion object.   
	 *
	 * @return			source object of the object.
	 */
	public Source getSource() {
		return source;
	}
	
	/**
	 * Set source object of the Suggestion object.   
	 *
	 * @param source			source object of the object.
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * Get content of the Suggestion object. This method is implemented by all sub classes   
	 *
	 * @return			string format of the object.
	 */
	public abstract String getContent();

}
