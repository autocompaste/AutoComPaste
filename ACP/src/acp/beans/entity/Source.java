package acp.beans.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.DocumentId;


@Entity
@Table(name="Source")
/**
 * Source class is utilize as a mapping to database table for Hibernate using EJB 3.0 annotation standard. It is 
 * also used as a java bean to pass around the data in ACP.
 * 
 * @author Teo Kee Cheng
 */
public class Source {
	
	@Id
	@DocumentId
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id", nullable=false)
	private int sourceID;
	
	@Column(name="name", nullable=true)
	private String name;
	
	@Column(name="url", nullable=true)
	private String url;

	@Transient
	private int rank;
	
	/**
	 * Default constructor
	 */
	public Source(){}
	
	/**
	 * Constructor to initialize object for storing in database
	 */
	public Source(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}
	
	/**
	 * Get source ID of the Source object.   
	 *
	 * @return			source ID of the object.
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * Set source ID of the Source object.   
	 *
	 * @param sourceID			source ID of the object.
	 */
	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}
	
	/**
	 * Get name of the Source object.   
	 *
	 * @return			name of the object.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set name of the Source object.   
	 *
	 * @param name			name of the object.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Get url of the Source object.   
	 *
	 * @return			url of the object.
	 */
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Get ML source rank of the Source object.   
	 *
	 * @return			ML source rank of the object.
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * Set ML source rank of the Source object.   
	 *
	 * @param rank			ML source rank of the object.
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	/**
	 * Get the formatted object information through a string.   
	 *
	 * @return			string format of the object.
	 */
	public String toString() {
		return "Source [sourceID=" + sourceID + ", name=" + name + "]";
	}	
}
