package android.example.customer.Classes;

public class Product {
    int price,quantity;
    String productnameeng, productnamehindi;
    int imageRid;
    boolean selected;
    String producttype;

    public Product() {
    }

    public Product(String productnameeng, String productnamehindi, int imageRid, int price, int quantity, boolean selected, String producttype) {
        this.price = price;
        this.quantity = quantity;
        this.productnameeng = productnameeng;
        this.productnamehindi = productnamehindi;
        this.imageRid = imageRid;
        this.selected = selected;
        this.producttype = producttype;
    }

    public Product(String productnameeng, String productnamehindi, int imageRid, boolean selected) {
        this.productnameeng = productnameeng;
        this.productnamehindi = productnamehindi;
        this.imageRid = imageRid;
        this.selected = selected;
    }

    public Product(String productnameeng, String productnamehindi, int imageRid, int price, int quantity) {
        this.productnameeng = productnameeng;
        this.productnamehindi=productnamehindi;
        this.price = price;
        this.quantity = quantity;
        this.imageRid = imageRid;
    }

    public Product(String productnameeng, String productnamehindi, int imageRid, int price, int quantity, boolean selected) {
        this.price = price;
        this.quantity = quantity;
        this.productnameeng = productnameeng;
        this.productnamehindi = productnamehindi;
        this.imageRid = imageRid;
        this.selected = selected;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductnameeng() {
        return productnameeng;
    }

    public void setProductnameeng(String productnameeng) {
        this.productnameeng = productnameeng;
    }

    public int getImageRid() {
        return imageRid;
    }

    public void setImageRid(int imageRid) {
        this.imageRid = imageRid;
    }

    public String getProductnamehindi() {
        return productnamehindi;
    }

    public void setProductnamehindi(String productnamehindi) {
        this.productnamehindi = productnamehindi;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getProducttype() {
        return producttype;
    }

    public void setProducttype(String producttype) {
        this.producttype = producttype;
    }
}
