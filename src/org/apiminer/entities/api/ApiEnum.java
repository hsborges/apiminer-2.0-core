package org.apiminer.entities.api;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

@SuppressWarnings("serial")
@Entity
public class ApiEnum extends ApiClass {
	
	@ElementCollection
	@CollectionTable(name = "ApiEnum_Constants", joinColumns=@JoinColumn(name = "api_enum_id"))
	@OrderColumn(name = "constant_index")
	private List<String> constants;
	
	public ApiEnum(String name) {
		super(name);
		super.setEnum(true);
		this.constants = new ArrayList<String>();
	}

	public ApiEnum(){
		super.setEnum(true);
		this.constants = new ArrayList<String>();
	}

	public List<String> getConstants() {
		return constants;
	}
	
	public void setConstants(List<String> values) {
		this.constants = values;
	}

}
