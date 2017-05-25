package pantry;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Products {
	private static final String productsIndex = "products:_id";
	private static final String productsTable = "products:";
	private static final String hostname = "localhost";

	/**
	 *  GET /product
	 * @return String, json array containing all products in DB
	 */
    public static String getAll() {
    	System.out.println("Query all products");
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(productsIndex);
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
     * GET /product/:id
     * @param String id
     * @return String, json object containing product :id in DB
     */
    public static String get(String id) {
    	System.out.println("Query product: " + id);
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(productsTable + id);
        jedis.close();
        return res;
    }

    /**
     * POST /product
     * @param productJson HTTP Request Body with new product information
     * @return String, json object containing product with new ID in DB
     */
    public static String create(String productJson) {
        System.out.println("Create product: " + productJson); 
        String outputJson = null;
        
        try {
        	Gson gson = new GsonBuilder().create();
        	Product product = gson.fromJson(productJson, Product.class);
        	// connect to redis server
            Jedis jedis = new Jedis(hostname);
            if (jedis.get(productsIndex) == null ) {
            	// create new products:_id index
            	jedis.set(productsIndex, "1");
            } else {
            	jedis.incr(productsIndex);
            }
            String id = jedis.get(productsIndex);
            product.setProductId(Integer.parseInt(id));
            outputJson = gson.toJson(product);
            jedis.set(productsTable + id, outputJson);
            jedis.close();                       
        	 
       } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println(Products.class.getSimpleName() + e.getMessage());
        }
        
        return outputJson;
    }

    /**
     * PUT /product/:id
     * @param String id
     * @param productJson HTTP Request Body with new product information
     * @return String, json object containing update product in DB
     */
    public static String update(String id, String productJson) {
        System.out.println("Update product: " + id);
        String outputJson = null;
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(productsTable + id);
        if (res != null) {
        	// update only if id exists
        	Gson gson = new GsonBuilder().create();
        	Product product = gson.fromJson(productJson, Product.class);
        	outputJson = gson.toJson(product);
            jedis.set(productsTable + id, outputJson);
        }
        jedis.close();
        return outputJson;
    }
}
