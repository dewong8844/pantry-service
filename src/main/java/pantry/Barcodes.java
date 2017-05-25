package pantry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import redis.clients.jedis.Jedis;

public class Barcodes {
	private static final String barcodesIndex = "barcodes:_id";
	private static final String barcodesLookupByValue = "barcodes:lookup:value";
	private static final String barcodesTable = "barcodes:";
	private static final String hostname = "localhost";

	/**
	 *  GET /barcode
	 * @return String, json array containing all barcodes in DB
	 */
    public static String getAll() {
    	System.out.println("Query all barcodes");
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(barcodesIndex);
        if (res == null) {
        	// log error - key doesn't exist in redis
        	jedis.close();
        	return "";
        }
        int count = Integer.parseInt(res);
        res = "[";
        for (int i = 1; i < count; i++) {
           res += get(Integer.toString(i)) + ", ";
        }
        res += get(Integer.toString(count));
        jedis.close();
        return res + "]";
    }

    /**
     * GET /barcode/:id
     * @param String id
     * @return String, json object containing barcode :id in DB
     */
    public static String get(String id) {
    	System.out.println("Query barcode: " + id);
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(barcodesTable + id);
        jedis.close();
        return res;
    }

    /**
     * GET /barcode/value/:value
     * @param String id
     * @return String, json object containing barcode :id in DB
     */
    public static String getByValue(String value) {
    	System.out.println("Query barcode value: " + value);
        Jedis jedis = new Jedis(hostname);
        String res = null;
        
        String id = jedis.hget(barcodesLookupByValue, value);
        if (id != null) {
        	res = jedis.get(barcodesTable + id);
        }
        jedis.close();
        return res;
    }

    /**
     * POST /barcode
     * @param barcodeJson HTTP Request Body with new barcode information
     * @return String, json object containing product with new ID in DB
     */
    public static String create(String barcodeJson) {
        System.out.println("Create barcode: " + barcodeJson); 
        String outputJson = null;
        
        try {
        	Gson gson = new GsonBuilder().create();
        	Barcode barcode = gson.fromJson(barcodeJson, Barcode.class);
        	// connect to redis server
            Jedis jedis = new Jedis(hostname);
            if (jedis.get(barcodesIndex) == null ) {
            	// create new barcodes:_id index
            	jedis.set(barcodesIndex, "1");
            } else {
            	jedis.incr(barcodesIndex);
            }
            String id = jedis.get(barcodesIndex);
            barcode.setBarcodeId(Integer.parseInt(id));
            outputJson = gson.toJson(barcode);
            jedis.set(barcodesTable + id, outputJson);
            jedis.hset(barcodesLookupByValue, barcode.getValue(), id);
            jedis.close();                       
        	 
       } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println(Barcodes.class.getSimpleName() + e.getMessage());
        }
        
        return outputJson;
    }

    /**
     * PUT /barcode/:id
     * @param String id
     * @param productJson HTTP Request Body with new product information
     * @return String, json object containing update product in DB
     */
    public static String update(String id, String barcodeJson) {
        System.out.println("Update barcode: " + id);
        String outputJson = null;
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(barcodesTable + id);
        if (res != null) {
        	// update only if id exists
        	Gson gson = new GsonBuilder().create();
        	Barcode barcode = gson.fromJson(barcodeJson, Barcode.class);
        	outputJson = gson.toJson(barcode);
        	jedis.set(barcodesTable + id, outputJson);
        }
        jedis.close();
        return outputJson;
    }
    
    /**
     * PUT /barcode/value/:value
     * @param String id
     * @param productJson HTTP Request Body with new product information
     * @return String, json object containing update product in DB
     */
    public static String updateByValue(String value, String barcodeJson) {
        System.out.println("Update barcode: " + value);
        String outputJson = null;
        Jedis jedis = new Jedis(hostname);
       	String id = jedis.hget(barcodesLookupByValue, value);
       	if (id != null) {
       		// update only if id exists
       		Gson gson = new GsonBuilder().create();
       		Barcode barcode = gson.fromJson(barcodeJson, Barcode.class);
       		outputJson = gson.toJson(barcode);
       		jedis.set(barcodesTable + id, outputJson);
       	}
        jedis.close();
        return outputJson;
    }
}
