package org.apiminer.entities.api;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * @author hudson
 */
@SuppressWarnings("serial")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "ApiElement")
public abstract class ApiElement implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	protected Long id;

	@Column(name = "is_private", columnDefinition = "boolean", nullable = false)
	protected boolean isPrivate;
	@Column(name = "is_protected", columnDefinition = "boolean", nullable = false)
	protected boolean isProtected;
	@Column(name = "is_public", columnDefinition = "boolean", nullable = false)
	protected boolean isPublic;
	@Column(name = "is_default", columnDefinition = "boolean", nullable = false)
	protected boolean isDefault;

	@Column(name = "is_final", columnDefinition = "boolean", nullable = false)
	protected boolean isFinal;
	@Column(name = "is_static", columnDefinition = "boolean", nullable = false)
	protected boolean isStatic;
	@Column(name = "is_abstract", columnDefinition = "boolean", nullable = false)
	protected boolean isAbstract;
	
	@Column(name = "has_javadoc", columnDefinition = "boolean", nullable = false)
	protected boolean hasJavadoc;
	@Column(name = "is_hidden", columnDefinition = "boolean", nullable = false)
	protected boolean isHidden;
	@Column(name = "is_deprecated", columnDefinition = "boolean", nullable = false)
	protected boolean isDeprecated;

	@Column(name = "name", columnDefinition = "text", nullable = false)
	protected String name;

	public ApiElement() {
		super();
	}

	public Long getId() {
		return id;
	}

	/**
	 * @return the isAbstract
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @return the isFinal
	 */
	public boolean isFinal() {
		return isFinal;
	}

	/**
	 * @return the isPrivate
	 */
	public boolean isPrivate() {
		return isPrivate;
	}

	/**
	 * @return the isProtected
	 */
	public boolean isProtected() {
		return isProtected;
	}

	/**
	 * @return the isPublic
	 */
	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @return the isStatic
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * @param isAbstract
	 *            the isAbstract to set
	 */
	public void setAbstract(boolean _abstract) {
		this.isAbstract = _abstract;
	}

	/**
	 * @param isDefault
	 *            the isDefault to set
	 */
	public void setDefault(boolean _default) {
		this.isDefault = _default;
	}

	/**
	 * @param isFinal
	 *            the isFinal to set
	 */
	public void setFinal(boolean _final) {
		this.isFinal = _final;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param isPrivate
	 *            the isPrivate to set
	 */
	public void setPrivate(boolean _private) {
		this.isPrivate = _private;
	}

	/**
	 * @param isProtected
	 *            the isProtected to set
	 */
	public void setProtected(boolean _protected) {
		this.isProtected = _protected;
	}

	public void setPublic(boolean _public) {
		this.isPublic = _public;
	}

	public void setStatic(boolean _static) {
		this.isStatic = _static;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ApiElement))
			return false;
		ApiElement other = (ApiElement) obj;
		if (isAbstract != other.isAbstract)
			return false;
		if (isDefault != other.isDefault)
			return false;
		if (isFinal != other.isFinal)
			return false;
		if (isPrivate != other.isPrivate)
			return false;
		if (isProtected != other.isProtected)
			return false;
		if (isPublic != other.isPublic)
			return false;
		if (isStatic != other.isStatic)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public boolean isHasJavadoc() {
		return hasJavadoc;
	}

	public void setHasJavadoc(boolean hasJavadoc) {
		this.hasJavadoc = hasJavadoc;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isDeprecated() {
		return isDeprecated;
	}

	public void setDeprecated(boolean isDeprecated) {
		this.isDeprecated = isDeprecated;
	}
	
}
