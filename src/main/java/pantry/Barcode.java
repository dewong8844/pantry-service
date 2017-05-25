package pantry;


public class Barcode {
    private long id;  // set to 0 if id unknown
    private String type;
    private String value;
    private long productid;
 	private Product product;

    public Barcode(long barcodeId, String type, String value, Product product) {
        this.id = barcodeId;
        this.type = type;
        this.value = value;
        this.product = product;
    }

    public long getBarcodeId() {
        return id;
    }

    public void setBarcodeId(int barcodeId) {
        this.id = barcodeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getProductid() {
 		return productid;
 	}

 	public void setProductid(int productid) {
 		this.productid = productid;
 	}

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
