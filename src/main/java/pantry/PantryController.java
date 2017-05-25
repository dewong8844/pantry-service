package pantry;

import static spark.Spark.*;

public class PantryController {

	public PantryController() {}
	
	public void setAndRunRoutes() {
        get("/product", (req, res) -> {
        	res.header("Content-type","application/json");
        	return Products.getAll();
        }, JsonUtil.json());
        
        get("/product/:id", (req, res) -> 
            Products.get(req.params(":id")), JsonUtil.json());

        post("/product", (req, res) -> { 
            res.status(201);
            System.out.println("body: " + req.body());
            return Products.create(req.body());
        });

        put("/product/:id", (req, res) -> 
            Products.update(req.params(":id"), req.body()),
            JsonUtil.json());	

        
        get("/barcode", (req, res) ->{
        	res.header("Content-type","application/json");
        	return Barcodes.getAll();        	
        }, JsonUtil.json());
        
        get("/barcode/:id", (req, res) -> 
        	Barcodes.get(req.params(":id")), JsonUtil.json());
        
        get("/barcode/value/:value", (req, res) -> 
        	Barcodes.getByValue(req.params(":value")), JsonUtil.json());

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
	}
	
    public static void main(String[] args) {
    	PantryController controller = new PantryController();
    	controller.setAndRunRoutes();
    }
}
