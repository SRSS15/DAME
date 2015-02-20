/*
 * Copyright 2015 
 * 	Danilo Cianciulli 			<cianciullidanilo@gmail.com>
 * 	Emranno Francesco Sannini 	<esannini@gmail.com>
 * 	Roberto Falzarano 			<robertofalzarano@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
