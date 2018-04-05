package pantry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import redis.clients.jedis.Jedis;

public class Products {
	private static final String productsIndex = "products:_id";
	private static final String productsTable = "products:";
	private static final String hostname = "localhost";

	/**
	 *  GET /product
	 * @return List of Product objects
	 */
    public static List<Product> getAll() {
    	System.out.println("Query all products");
        Jedis jedis = new Jedis(hostname);
        Set<String> keys = jedis.keys("products:[0-9]*");
        List<Product> res = new ArrayList<Product>();
    	Gson gson = new GsonBuilder().create();
    	
        if (keys != null && keys.size() > 0) {     
            for (String key : keys) {
            	String val = jedis.get(key);
            	if (val != null) {
            		Product product = gson.fromJson(val, Product.class);
            		res.add(product);            
            	}
            }
        }
        jedis.close();
        return res;
    }

    /**
     * GET /product/:id
     * @param String id
     * @return Product object containing product :id in DB
     */
    public static Product get(String id) {
    	System.out.println("Query product: " + id);
        Product product = null;
    	try {
        	Gson gson = new GsonBuilder().create();
        	Jedis jedis = new Jedis(hostname);
        	String res = jedis.get(productsTable + id);
        	if (res != null) {
        		product = gson.fromJson(res, Product.class);
        	}
        	jedis.close();
        } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println(Products.class.getSimpleName() + e.getMessage());
        }
    	
        return product;
    }

    /**
     * POST /product
     * @param productJson HTTP Request Body with new product information
     * @return Product object containing product with new ID in DB
     */
    public static Product create(String productJson) {
        System.out.println("Create product: " + productJson); 
        String outputJson = null;
        Product product = null;
        
        try {
        	Gson gson = new GsonBuilder().create();
        	product = gson.fromJson(productJson, Product.class);
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
        
        return product;
    }

    /**
     * PUT /product/:id
     * @param String id
     * @param productJson HTTP Request Body with new product information
     * @return Product, json object containing update product in DB
     */
    public static Product update(String id, String productJson) {
        System.out.println("Update product: " + id);
        String outputJson = null;
        Product product = null;        
        Jedis jedis = new Jedis(hostname);
        String res = jedis.get(productsTable + id);
        if (res != null) {
        	// update only if id exists
        	Gson gson = new GsonBuilder().create();
        	product = gson.fromJson(productJson, Product.class);
        	product.setProductId(Integer.parseInt(id));
        	outputJson = gson.toJson(product);
            jedis.set(productsTable + id, outputJson);
        }
        jedis.close();
        return product;
    }
    
    /**
     * DELETE /product/:id
     * @param String id
     * @return String, json object containing {"result": "Ok" or "Failed"}
     */
    public static String delete(String id) {
        System.out.println("Delete product: " + id);
        String res = null;
        Jedis jedis = new Jedis(hostname);
        Long cnt = jedis.del(productsTable + id);
        if (cnt == 1) {
        	// how to report result?
        	res = "{\"result\": \"Ok\"}";
        } else {
        	res = "{\"result\": \"Failed\"}";
        }
        jedis.close();
        return res;
    }
}
