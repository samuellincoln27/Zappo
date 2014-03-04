import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ZappoTest {

	ArrayList<ArrayObject> locallist;  //to store a local copy of the JSON object
	int number;
	int price;

	public ZappoTest(int number, int price) {
		locallist = new ArrayList<ArrayObject>();
		this.number = number;
		this.price = price;
	}

	public static void main(String[] args) throws Exception {
		int number = 0;
		int price = 0;
		if(args.length!=2){
			System.err.print("Wrong number of arguments, please try again");
			return;
		}
		if(!args[1].toString().substring(0, 1).equals("$")){
			System.err.print("Wrong format of amount, please use $x");
			return;
		}
		number = Integer.parseInt(args[0].toString());
		price = Integer.parseInt(args[1].toString().substring(1));
		//initialising the constructor
		ZappoTest http = new ZappoTest(number, price);
		//Method to receive the JSON object from API
		http.sendGet(number, price);
		//Data is the object used to fill and print our results
		ArrayObject[] data = new ArrayObject[number];
		//Finds out all the combinations 
		http.combinations(data, 0, 0, 0);
	}

	synchronized void combinations(ArrayObject[] data, int sum, int index,
			int start) {
		// Current combination is ready to be printed
		if (index == number) {
			if (sum == price){
				System.out.print("[ ");
				for (int j = 0; j < number; j++) {
					System.out.print(data[j].GetKey().toString()+" ");
					if(j!=(number-1)){
						System.out.print(",");
					}
				}
				System.out.print("]");
				System.out.println();
				}
			return;
		}
		int sum1[] = new int[locallist.size()];
		for (int i = start; i < locallist.size(); i++) {
			data[index] = locallist.get(i);
			sum1[i] = sum + locallist.get(i).GetValue();
			//Pruning those portions which can never lead to our result
			if (sum1[i] <= price)
				combinations(data, sum1[i], index + 1, i + 1);
		}
	}

	// Getting the JSON Object
	private synchronized void sendGet(int number, int price)
			throws Exception {
		int totalResultCount = 0;
		int statusCode=0;
		int page = 1;
		int currentResultCount = 0;
		URL myUrl;
		URLConnection yc;
		JSONParser parser = new JSONParser();
		Object obj;
		JSONObject jsonObject;
		JSONArray msg;
		Iterator iterator;
		JSONObject temp;
		boolean flag = true;
		while (((currentResultCount * page) < totalResultCount || totalResultCount == 0)
				&& flag) {
			myUrl = new URL(
					"http://api.zappos.com/Search/term/%20?sort={\"price\":\"asc\"}&limit=100&page="
							+ page
							+ "&key=5b8384087156eb88dce1a1d321c945564f4d858e");
			try {
				yc = myUrl.openConnection();
				obj = parser.parse(new InputStreamReader(yc.getInputStream()));
				jsonObject = (JSONObject) obj;
				statusCode = Integer.parseInt(jsonObject.get(
						"statusCode").toString());
				if(statusCode!=200){
					System.err.print("Please use another key");
					System.exit(0); 
				}
				totalResultCount = Integer.parseInt(jsonObject.get(
						"totalResultCount").toString());
				
				if (totalResultCount == 0)
					flag = false;
				msg = (JSONArray) jsonObject.get("results");

				iterator = msg.iterator();

				while (iterator.hasNext()) {
					temp = (JSONObject) iterator.next();
					if (Float.parseFloat(temp.get("price").toString()
							.replace(",", "").substring(1)) <= price) {
						locallist.add(new ArrayObject(temp.get("productUrl")
								.toString(), Math.round(Float.parseFloat(temp
								.get("price").toString().replace(",", "")
								.substring(1)))));
					} else
						flag = false;

				}

				page++;

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	//Object that stores the URL along with the price 
	class ArrayObject {
		String key;
		int value;

		public ArrayObject(String Key, int Value) {
			this.key = Key;
			this.value = Value;
		}

		public String GetKey() {
			return key;
		}

		public int GetValue() {
			return value;
		}
	}
}