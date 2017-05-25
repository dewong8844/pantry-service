package pantry;

public class Product {
    private int    id;    // set to 0 if id unknown
    private String brand;
    private String name;
    private double amount;
    private String unit;
    private String ingredient;
    private String category;

    public Product() {}
    
    public Product(int pId, String brand, String name,
                   double amount, String unit,
                   String ingredient, String category) {
        setProductId(pId);
        setBrand(brand);
        setName(name);
        setAmount(amount);
        setUnit(unit);
        setIngredient(ingredient);
        setCategory(category);
    }

    public int getProductId() {
        return id;
    }

    public void setProductId(int mProductId) {
        this.id = mProductId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String mBrand) {
        this.brand = mBrand;
    }

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double mAmount) {
        this.amount = mAmount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String mUnit) {
        this.unit = mUnit;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String mIngredient) {
        this.ingredient = mIngredient;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String mCategory) {
        this.category = mCategory;
    }
    
    public String toString() {
    	return "Product [ id: " + id + ", brand: " + brand + ", name: " + name + " ]";
    }
}
