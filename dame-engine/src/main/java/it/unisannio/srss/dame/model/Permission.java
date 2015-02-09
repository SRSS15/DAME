package it.unisannio.srss.dame.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Permission {

	private String type;

	@JsonProperty("usage-points")
	private List<UsagePoint> usagePoints;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<UsagePoint> getUsagePoints() {
		return usagePoints;
	}

	public void setUsagePoints(List<UsagePoint> usagePoints) {
		this.usagePoints = usagePoints;
	}
	
}
