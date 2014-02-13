package org.apiminer.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SuppressWarnings("serial")
@Entity
@Table(name = "Attachment")
public class Attachment implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "file_name", columnDefinition="text", nullable = false)
	private String fileName;
	
	@Column(name = "content", nullable = false, columnDefinition = "bytea")
	private byte[] content;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at", nullable = false)
	private Date addedAt;
	
	public Attachment() {
		super();
	}

	public Attachment(String fileName, byte[] content) {
		super();
		this.content = content;
		this.fileName = fileName;
		this.addedAt = new Date(System.currentTimeMillis());
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public byte[] getContent() {
		return content;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getId() {
		return id;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
