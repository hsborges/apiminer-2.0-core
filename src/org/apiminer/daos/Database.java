package org.apiminer.daos;

public final class Database {
	
	public static final String MYSQL_DATABASE = "MySQL";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";
	
	public static final String POSTGRESQL_DATABASE = "PostgreSQL";
	public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
	public static final String POSTGRESQL_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
	
	public static final String HSQL_DATABASE = "HSQL";
	public static final String HSQL_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String HSQL_DIALECT = "org.hibernate.dialect.HSQLDialect";
	
	private String name;
	
	private String driverClass;
	private String dialect;
	
	private String username;
	private String password;
	
	private String preProcessingSchema;
	private String examplesSchema;
	
	private String address;
	private Integer port;

	private boolean autoUpdate;
	
	public boolean isValid(){
		return this.username != null
				&& this.password != null
				&& this.address != null
				&& this.port != null
				&& (this.name.equals(MYSQL_DATABASE) || this.name.equals(POSTGRESQL_DATABASE) || this.name.equals(HSQL_DATABASE))
				&& this.driverClass != null
				&& this.dialect != null
				&& (this.preProcessingSchema != null && !this.preProcessingSchema.isEmpty())
				&& (this.examplesSchema != null && !this.examplesSchema.isEmpty());
	}
	
	public String getPreProcessingUrl() {
		if ( this.name.equals(MYSQL_DATABASE) ){
			return "jdbc:mysql://"+address+":"+port+"/"+preProcessingSchema;
		}else if ( this.name.equals(POSTGRESQL_DATABASE) ){
			return "jdbc:postgresql://"+address+":"+port+"/"+preProcessingSchema;
		}else if ( this.name.equals(HSQL_DATABASE) ){
			return String.format("jdbc:hsqldb:mem:%s", preProcessingSchema);
		}else{
			return null;
		}
	}
	
	public String getExamplesUrl() {
		if ( this.name.equals(MYSQL_DATABASE) ){
			return "jdbc:mysql://"+address+":"+port+"/"+examplesSchema;
		}else if ( this.name.equals(POSTGRESQL_DATABASE) ){
			return "jdbc:postgresql://"+address+":"+port+"/"+examplesSchema;
		}else if ( this.name.equals(HSQL_DATABASE) ){
			return String.format("jdbc:hsqldb:mem:%s", examplesSchema);
		}else{
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPreProcessingSchema() {
		return preProcessingSchema;
	}

	public void setPreProcessingSchema(String preProcessingSchema) {
		this.preProcessingSchema = preProcessingSchema;
	}

	public String getExamplesSchema() {
		return examplesSchema;
	}

	public void setExamplesSchema(String examplesSchema) {
		this.examplesSchema = examplesSchema;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + (autoUpdate ? 1231 : 1237);
		result = prime * result + ((dialect == null) ? 0 : dialect.hashCode());
		result = prime * result
				+ ((driverClass == null) ? 0 : driverClass.hashCode());
		result = prime * result
				+ ((examplesSchema == null) ? 0 : examplesSchema.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime
				* result
				+ ((preProcessingSchema == null) ? 0 : preProcessingSchema
						.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Database)) {
			return false;
		}
		Database other = (Database) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (autoUpdate != other.autoUpdate) {
			return false;
		}
		if (dialect == null) {
			if (other.dialect != null) {
				return false;
			}
		} else if (!dialect.equals(other.dialect)) {
			return false;
		}
		if (driverClass == null) {
			if (other.driverClass != null) {
				return false;
			}
		} else if (!driverClass.equals(other.driverClass)) {
			return false;
		}
		if (examplesSchema == null) {
			if (other.examplesSchema != null) {
				return false;
			}
		} else if (!examplesSchema.equals(other.examplesSchema)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		} else if (!port.equals(other.port)) {
			return false;
		}
		if (preProcessingSchema == null) {
			if (other.preProcessingSchema != null) {
				return false;
			}
		} else if (!preProcessingSchema.equals(other.preProcessingSchema)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}
	
}
