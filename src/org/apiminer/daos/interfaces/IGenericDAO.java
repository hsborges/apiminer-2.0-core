package org.apiminer.daos.interfaces;

import org.apiminer.daos.DatabaseType;

public interface IGenericDAO <T extends IEntity> {
	
	void persist(final T object, final DatabaseType databaseType);
	void update(final T object, final DatabaseType databaseType);
	void delete(final long object, final DatabaseType databaseType);
	T find(final long id, final DatabaseType databaseType);

}
