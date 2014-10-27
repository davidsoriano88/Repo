package utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class Place {
	private String id;
	private String icon;
	private String name;
	private String vicinity;
	private Double latitude;
	private Double longitude;
	private String placeid;
	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getPlaceID() {
		return placeid;
	}

	public void setPlaceID(String placeid) {
		this.placeid = placeid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVicinity() {
		return vicinity;
	}

	public void setVicinity(String type) {
		this.vicinity = vicinity;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	

	public static Place jsonToReferencePoint(JSONObject referencePoint) {
		try {
			Place result = new Place();
			JSONObject geometry = (JSONObject) referencePoint.get("geometry");
			JSONObject location = (JSONObject) geometry.get("location");
			result.setLatitude((Double) location.get("lat"));
			result.setLongitude((Double) location.get("lng"));
			result.setPlaceID(referencePoint.getString("place_id"));
			result.setIcon(referencePoint.getString("icon"));
			result.setName(referencePoint.getString("name"));
			result.setVicinity(referencePoint.getString("vicinity"));
			result.setId(referencePoint.getString("id"));
			result.setType(referencePoint.getJSONArray("types").get(0).toString());
			return result;
		} catch (JSONException ex) {
			Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public String toString() {
		return "Place{" + "id=" + id + ", place_id=" + placeid + ", icon="
				+ icon + ", name=" + name + ", latitude=" + latitude
				+ ", longitude=" + longitude +", types="+ type+'}';
	}
}