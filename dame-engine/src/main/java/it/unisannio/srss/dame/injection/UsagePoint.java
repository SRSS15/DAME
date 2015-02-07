package it.unisannio.srss.dame.injection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsagePoint {

	@JsonProperty("class")
	private String clazz;
	
	private String method;

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
}
