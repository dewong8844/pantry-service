package pantry;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PantryController {

	public PantryController() {}
	
	public String getProductBarcodeJsonString(Product product, Barcode barcode) {
		return "{\"id\":\"" + product.getProductId() + 
		       "\", \"brand\":\"" + product.getBrand() + 
		       "\", \"name\":\"" + product.getName() + 
		       "\", \"amount\":\"" + product.getAmount() + 
		       "\", \"unit\":\"" + product.getUnit() +
		       "\", \"ingredient\":\"" + product.getIngredient() +
		       "\", \"category\":\"" + product.getCategory() +
		       "\", \"barcode\":\"" + barcode.getValue() +
		       "\", \"barcodetype\":\"" + barcode.getType() +
		       "\", \"barcodeid\":\"" + barcode.getBarcodeId() +
		       "\"}";
	}
	
	public void setAndRunRoutes() {
		
        get("/product", (req, res) -> {
          	res.header("Content-type","application/json");
          	StringBuilder sb = new StringBuilder();
        	sb.append("[");
        	
        	List<Product> products = Products.getAll();
        	products.forEach(product -> {
        		int id = product.getProductId();
        		Barcode barcode = Barcodes.getByProductid(Integer.toString(id));
                if (sb.length() > 1) {
                	sb.append(",");
                }
        		sb.append(this.getProductBarcodeJsonString(product, barcode));               
        	});
        	sb.append("]");
        	return sb.toString();
        }, JsonUtil.json());
        
		
        get("/product/:id", (req, res) -> {
            Product product = Products.get(req.params(":id"));
    		Barcode barcode = Barcodes.getByProductid(req.params(":id"));
    		return this.getProductBarcodeJsonString(product, barcode);
        }, JsonUtil.json());

        get("/product/barcode/:barcode", (req, res) -> {
        	Barcode barcode = Barcodes.getByValue(req.params(":barcode"));
        	Product product = Products.get(Long.toString(barcode.getProductid()));
        	return this.getProductBarcodeJsonString(product, barcode);
    	}, JsonUtil.json());
        
        post("/product", (req, res) -> { 
            res.status(201);
            System.out.println("body: " + req.body());
            // create product 
            Product product = Products.create(req.body());
            int id = product.getProductId();
            // create barcode
            JsonParser parser = new JsonParser(); 
            JsonObject json = (JsonObject) parser.parse(req.body());

            String barcodejson = "{productid:" + id + ", value:" + json.get("barcode") + ", type:" + json.get("barcodetype") + "}";
            Barcode barcode = Barcodes.create(barcodejson);
            return this.getProductBarcodeJsonString(product, barcode);         
        });

        put("/product/:id", (req, res) -> {
            Product product = Products.update(req.params(":id"), req.body());
            JsonParser parser = new JsonParser(); 
            JsonObject json = (JsonObject) parser.parse(req.body());
            Barcode barcode = Barcodes.getByProductid(req.params(":id"));
            String barcodejson = "{productid:" + product.getProductId() + ", value:" + json.get("barcode") + ", type:" + json.get("barcodetype") + "}";
            barcode = Barcodes.update(Long.toString(barcode.getBarcodeId()), barcodejson);
            return this.getProductBarcodeJsonString(product, barcode); 
        }, JsonUtil.json());
        
        delete("/product/:id", (req, res) -> {
        	Barcode barcode = Barcodes.getByProductid(req.params(":id"));
        	Products.delete(req.params(":id"));
        	String ret = Barcodes.delete(Long.toString(barcode.getBarcodeId()));
        	return ret;
        }, JsonUtil.json());

        
        get("/barcode", (req, res) -> {
        	res.header("Content-type","application/json");
        	return Barcodes.getAll();        	
        }, JsonUtil.json());
        
        get("/barcode/:id", (req, res) -> 
        	Barcodes.get(req.params(":id")), JsonUtil.json());
        
        get("/barcode/value/:value", (req, res) -> {
        	Gson gson = new GsonBuilder().create();
        	return gson.toJson(Barcodes.getByValue(req.params(":value")));
        }, JsonUtil.json());

        post("/barcode", (req, res) -> { 
            res.status(201);
            System.out.println("body: " + req.body());
            return Barcodes.create(req.body());
        });

        put("/barcode/:id", (req, res) -> 
        	Barcodes.update(req.params(":id"), req.body()), 
        	JsonUtil.json());
        
        put("/barcode/value/:value", (req, res) -> 
        	Barcodes.updateByValue(req.params(":value"), req.body()), 
        	JsonUtil.json());
        
        delete("/barcode/:id", (req, res) ->
    		Barcodes.delete(req.params(":id")),
    		JsonUtil.json());

	}
	
	public static void main(String[] args) {
    	PantryController controller = new PantryController();
    	controller.setAndRunRoutes();
    }
}
