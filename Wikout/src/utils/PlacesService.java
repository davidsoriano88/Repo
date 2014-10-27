package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlacesService {
	private String API_KEY;
	Util util = new Util();

	public PlacesService(String apikey) {
		this.API_KEY = apikey;
	}

	public void setApiKey(String apikey) {
		this.API_KEY = apikey;
	}

	public void onActivityResult() {
	}

	public ArrayList<Place> findPlaces(double latitude, double longitude,
			String placeSpacification) {
		String urlString = makeUrl(latitude, longitude, placeSpacification);
		System.out.println("URL JSON: " + urlString);
		try {
			String json = getJSON(urlString);
			System.out.println(json);
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("results");
			ArrayList<Place> arrayList = new ArrayList<Place>();
			for (int i = 0; i < array.length(); i++) {
				try {
					Place place = Place.jsonToReferencePoint((JSONObject) array
							.get(i));
					util.log("" + place);
					System.out.println("PLACE DE JSON: " + place.getName());
					arrayList.add(place);
				} catch (Exception e) {
				}
			}
			return arrayList;
		} catch (JSONException ex) {
			Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return null;
	}

	// https://maps.googleapis.com/maps/api/place/search/json?location=41.645797,-0.8884767&radius=1000&types=cafe&sensor=false&key=AIzaSyCo0GZPsPX3hZvSi8q31AUlzufu6SUymXU
	private String makeUrl(double latitude, double longitude, String place) {
		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/search/json?");
		if (place.equals("")) {
			urlString.append("&location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			urlString.append("&radius=100");
			urlString.append("&rankBy=distance");
			urlString
					.append("&types=accounting|airport|amusement_park|aquarium|art_gallery|atm|bakery|bank|bar|beauty_salon|bicycle_store|book_store|bowling_alley|bus_station|cafe|campground|car_dealer|car_rental|car_repair|car_wash|casino|cemetery|church|city_hall|clothing_store|convenience_store|courthouse|dentist|department_store|doctor|electrician|electronics_store|embassy|establishment|finance|fire_station|florist|food|funeral_home|furniture_store|gas_station|general_contractor|grocery_or_supermarket|gym|hair_care|hardware_store|health|hindu_temple|home_goods_store|hospital|insurance_agency|jewelry_store|laundry|lawyer|library|liquor_store|local_government_office|locksmith|lodging|meal_delivery|meal_takeaway|mosque|movie_rental|movie_theater|moving_company|museum|night_club|painter|park|parking|pet_store|pharmacy|physiotherapist|place_of_worship|plumber|police|post_office|real_estate_agency|restaurant|roofing_contractor|rv_park|school|shoe_store|shopping_mall|spa|stadium|storage|store|subway_station|synagogue|taxi_stand|train_station|travel_agency|university|veterinary_care|zoo");

			// urlString.append("&types=null"bank|hospital|cafe|pharmacy|police|restaurant"
			// + place/);
			urlString.append("&sensor=false&key=" + API_KEY);
			util.log(urlString.toString());
			util.log("latitud: " + String.valueOf(latitude)
					+ String.valueOf(longitude));
		} else {
			urlString.append("&location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			urlString.append("&radius=100");
			urlString.append("&rankBy=distance");
			urlString
					.append("&types=accounting|airport|amusement_park|aquarium|art_gallery|atm|bakery|bank|bar|beauty_salon|bicycle_store|book_store|bowling_alley|bus_station|cafe|campground|car_dealer|car_rental|car_repair|car_wash|casino|cemetery|church|city_hall|clothing_store|convenience_store|courthouse|dentist|department_store|doctor|electrician|electronics_store|embassy|establishment|finance|fire_station|florist|food|funeral_home|furniture_store|gas_station|general_contractor|grocery_or_supermarket|gym|hair_care|hardware_store|health|hindu_temple|home_goods_store|hospital|insurance_agency|jewelry_store|laundry|lawyer|library|liquor_store|local_government_office|locksmith|lodging|meal_delivery|meal_takeaway|mosque|movie_rental|movie_theater|moving_company|museum|night_club|painter|park|parking|pet_store|pharmacy|physiotherapist|place_of_worship|plumber|police|post_office|real_estate_agency|restaurant|roofing_contractor|rv_park|school|shoe_store|shopping_mall|spa|stadium|storage|store|subway_station|synagogue|taxi_stand|train_station|travel_agency|university|veterinary_care|zoo"

					);
			urlString.append("&sensor=false&key=" + API_KEY);
			util.log("latitud: " + String.valueOf(latitude)
					+ String.valueOf(longitude));
		}
		return urlString.toString();
	}

	protected String getJSON(String url) {
		return getUrlContents(url);
	}

	private String getUrlContents(String theUrl) {
		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(theUrl);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream()), 8);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line + "\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}
}