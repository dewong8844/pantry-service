package pantry;

import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import redis.clients.jedis.Jedis;

public class Barcodes {
	private static final String barcodesIndex = "barcodes:_id";
	private static final String barcodesLookupByValue = "barcodes:lookup:value";
	private static final String barcodesLookupByProductid = "barcodes:lookup:productid";
	private static final String barcodesTable = "barcodes:";
	private static final String hostname = "localhost";

	/**
	 *  GET /barcode
	 * @return String, json array containing all barcodes in DB
	 */
    public static String getAll() {
    	System.out.println("Query all barcodes");
        Jedis jedis = new Jedis(hostname);
        Set<String> keys = jedis.keys("barcodes:[0-9]*");
        String res = "[";
        int count = (keys != null)? keys.size() : 0;
        if (count > 0) {     
            for (String key : keys) {
            	String val = jedis.get(key);
            	if (val == null) {
            		// log error - key doesn't exist in redis
            		jedis.close();
            		return "";
            	}
            	count--;
            	res += val + (count > 0? ", " : "");
            }
        }
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
    public static Barcode getByValue(String value) {
    	System.out.println("Query barcode by value: " + value);
        Jedis jedis = new Jedis(hostname);
        Barcode barcode = null;
        
        try {
        	String res = null;
        	Gson gson = new GsonBuilder().create();
        	String id = jedis.hget(barcodesLookupByValue, value);
        	if (id != null) {
        		res = jedis.get(barcodesTable + id);
        	}
        	barcode = gson.fromJson(res, Barcode.class);
        	jedis.close();
        } catch (Exception e) {
    		e.printStackTrace();
    		System.err.println(Barcodes.class.getSimpleName() + e.getMessage());
    	}
        return barcode;
    }
    
    public static Barcode getByProductid(String productid) {
    	System.out.println("Query barcode by productid: " + productid);
        Jedis jedis = new Jedis(hostname);
        Barcode barcode = null;
        
        try {
        	String res = null;
        	Gson gson = new GsonBuilder().create();
        	String id = jedis.hget(barcodesLookupByProductid, productid);
        	if (id != null) {
        		res = jedis.get(barcodesTable + id);
        	}
        	barcode = gson.fromJson(res, Barcode.class);
        	jedis.close();
        } catch (Exception e) {
    		e.printStackTrace();
    		System.err.println(Barcodes.class.getSimpleName() + e.getMessage());
    	}
        return barcode;
    }

    /**
     * POST /barcode
     * @param barcodeJson HTTP Request Body with new barcode information
     * @return String, json object containing product with new ID in DB
     */
    public static Barcode create(String barcodeJson) {
        System.out.println("Create barcode: " + barcodeJson); 
        String outputJson = null;
        Barcode barcode = null;
        
        try {
        	Gson gson = new GsonBuilder().create();
        	barcode = gson.fromJson(barcodeJson, Barcode.class);
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
            jedis.hset(barcodesLookupByProductid, Long.toString(barcode.getProductid()), id);
            jedis.close();                       
        	 
       } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println(Barcodes.class.getSimpleName() + e.getMessage());
        }
        
        return barcode;
    }

    /**
     * PUT /barcode/:id
     * @param String id
     * @param productJson HTTP Request Body with new product information
     * @return Barcode, json object containing update product in DB
     */
    public static Barcode update(String id, String barcodeJson) {
        System.out.println("Update barcode: " + id);
        String outputJson = null;
        Barcode barcode = null;
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(barcodesTable + id);
        if (res != null) {
        	// update only if id exists
        	Gson gson = new GsonBuilder().create();      	
        	barcode = gson.fromJson(barcodeJson, Barcode.class);
        	barcode.setBarcodeId(Integer.parseInt(id));
        	outputJson = gson.toJson(barcode);
        	jedis.set(barcodesTable + id, outputJson);
        	Barcode oldbarcode = gson.fromJson(res, Barcode.class);
        	jedis.hdel(barcodesLookupByValue, oldbarcode.getValue());
        	jedis.hset(barcodesLookupByValue, barcode.getValue(), id);
        	jedis.hdel(barcodesLookupByProductid, Long.toString(oldbarcode.getProductid()));
        	jedis.hset(barcodesLookupByProductid, Long.toString(barcode.getProductid()), id);
        }
        jedis.close();
        return barcode;
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
       		barcode.setBarcodeId(Integer.parseInt(id));
       		outputJson = gson.toJson(barcode);
       		jedis.set(barcodesTable + id, outputJson);
       	}
        jedis.close();
        return outputJson;
    }
    
    /**
     * DELETE /barcode/:id
     * @param String id
     * @return String, json object containing {"result": "Ok" or "Failed"}
     */
    public static String delete(String id) {
    	System.out.println("Delete barcode: " + id);
        Jedis jedis = new Jedis(hostname);                
        String res = jedis.get(barcodesTable + id);
        if (res != null) {
        	Gson gson = new GsonBuilder().create();
        	Barcode barcode = gson.fromJson(res, Barcode.class);
        	jedis.hdel(barcodesLookupByValue, barcode.getValue());
        	jedis.hdel(barcodesLookupByProductid, Long.toString(barcode.getProductid()));
        	Long cnt = jedis.del(barcodesTable + id);
        	if (cnt == 1) {
        		// how to report result?
        		res = "{\"result\": \"Ok\"}";
        	} else {
        		res = "{\"result\": \"Failed\"}";
        	}
        }
        jedis.close();
        return res;
    }
}
