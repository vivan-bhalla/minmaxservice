package com.baraka.minmaxservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;


@SpringBootApplication
@RestController
public class MinmaxserviceApplication {
	// We have a hashmap which maps all the stock symbols to an arraylist of json objects
	public static HashMap<String, ArrayList<JSONObject>> minMaxPricePerMin = new HashMap<>();

	/**
	 * This particular service accesses the Baraka websocket and gets the stock prices
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(MinmaxserviceApplication.class, args);

		try {
			// Access the websocket endpoint
			final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://b-mocks.dev.app.getbaraka.com:9989"));

			// Add listener to listen to the events
			clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
				public void handleMessage(String message) {
					try {
						// We have to intercept the data and then we can manipulate it
						ObjectMapper mapper = new ObjectMapper();
						//Get the data node
						JsonNode node= mapper.readTree(message).get("data");
						calculateMinMaxPrice(node);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}

				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * End point when the home page is accessed
	 */
	@RequestMapping("/")
	public String homepage() {
		return "Welcome! Please use loaclhost:8080/getprice/{stock-name} to get the price of the stock of your choice!";
	}

	/**
	 * End point to be accessed to get the stock price for a current stock
	 * @param stock
	 * @return
	 */
	@RequestMapping( "/getprice/{stock}")
	public ArrayList<JSONObject> getStockPrice(@PathVariable("stock") String stock) {
		JSONObject jsonObject = new JSONObject();
		ArrayList<JSONObject> arrayList  = new ArrayList<>();
		// Get the current time when the endpoint is called and find the index in which the current time falls
		long currentTime =  System.currentTimeMillis();
		int index = findInterval(currentTime, stock);

		// Iterate through the minMaxSerivce hashmap and get the intervals for the particular stock
		for(int i = 0; i<= index; i++) {
			ArrayList<JSONObject> arrayList1 = minMaxPricePerMin.get(stock);
			arrayList.add(arrayList1.get(i));
		}

		return arrayList;
	}

	/**
	 * Helper function to add the relevant data upon receiving a message
	 * @param dataNode
	 */
	private static void calculateMinMaxPrice(JsonNode dataNode) {
		// Get the useful data from the data node
		String stock = dataNode.findValue("s").asText();
		Double stockPrice = Double.parseDouble(dataNode.findValue("p").asText());
		Long intervalStart = Long.parseLong(dataNode.findValue("t").asText());
		// We will round it off to the start of the mintue
		Long remainder = intervalStart % (60 * 1000);
		intervalStart -= remainder;
		// Find the next minute of the interval (It will be interval start + 59 sec)
		Long intervalEnd = Instant.ofEpochMilli(intervalStart).plusSeconds(59).toEpochMilli();


		// Create the JSON object to store all this data
		JSONObject json = new JSONObject();
		json.put("intervalStart", intervalStart);
		json.put("intervalEnd", intervalEnd);
		json.put("stock", stock);

		// Check whether the stock is present in the HashMap or not.
		// If not then add the stock in the hashmap with the current interval
		if(!minMaxPricePerMin.containsKey(stock)) {
			// If this is the first time we are seeing the stock's price in a day, then we assume that it is the only
			// price we have seen and we make it the min and the max price
			json.put("maxPrice", stockPrice);
			json.put("minPrice", stockPrice);
			// Create an arraylist and add this json object there. Eventually add it to the hashmap
			ArrayList<JSONObject> arrayList = new ArrayList<>();
			arrayList.add(json);
			minMaxPricePerMin.put(stock, arrayList);
		} else {
			// This means that the stock is already present in the hashmap.
			//
			ArrayList<JSONObject> arrayList = minMaxPricePerMin.get(stock);
			long lastInterval = Long.parseLong(arrayList.get(arrayList.size()-1).get("intervalEnd").toString());

			if(intervalStart < lastInterval) {
				// This means that the current time is in one of the interval already present in the hashmap
				// Get the min and max stock price form the particular interval
				JSONObject interval = minMaxPricePerMin.get(stock).get(arrayList.size()-1);

				// Get the min and max price of the interval
				Double minPriceInerval = Double.parseDouble(interval.get("minPrice").toString());
				Double maxPriceInerval = Double.parseDouble(interval.get("maxPrice").toString());

				// Compare
				if(stockPrice < minPriceInerval) {
					minMaxPricePerMin.get(stock).get(arrayList.size()-1).put("minPrice", stockPrice);
				}

				if(stockPrice > maxPriceInerval) {
					minMaxPricePerMin.get(stock).get(arrayList.size()-1).put("maxPrice", stockPrice);
				}
			} else {
				// This is a new time interval
				json.put("minPrice", stockPrice);
				json.put("maxPrice", stockPrice);
				minMaxPricePerMin.get(stock).add(json);
			}
		}
	}

	/**
	 * Helper function to find the index at which a particular interval second lies in the hashmap
	 * @param second
	 * @return
	 */
	private static int findInterval(Long second, String stockName) {
		// Get the arraylist of objects from the hashmap
		ArrayList<JSONObject> intervals = minMaxPricePerMin.get(stockName);
		// Iterate through the objects and find the index at which the interval will be stored
		// Note: We will do a linear search since there will be a max of 3600 entries (as well keep this data only for 1 day)
		for (int i=0; i< intervals.size(); i++) {
			JSONObject json = intervals.get(i);
			// Get the start and end time of the interval
			Long intervalStartTime = Long.parseLong(json.get("intervalStart").toString());
			Long intervalEndTime = Long.parseLong(json.get("intervalEnd").toString());
			if(second >= intervalStartTime && second <= intervalEndTime)
				return i;
		}

		return -1;
	}

}
