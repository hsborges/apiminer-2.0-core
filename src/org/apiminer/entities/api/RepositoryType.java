package org.apiminer.entities.api;

public enum RepositoryType {

	COMPRESSED("Compressed"), GIT("Git"), LOCAL("Local"), MERCURIAL("Mercurial"), SUBVERSION("Subversion");

	private String value;

	RepositoryType(String type) {
		this.value = type;
	}

	public static RepositoryType parse(String value) {
		for (RepositoryType repositoryType : RepositoryType.values()) {
			if (repositoryType.value.equals(value)) {
				return repositoryType;
			}
		}
		return null;
	}

	@Override
	public final String toString() {
		return this.value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	};

}
