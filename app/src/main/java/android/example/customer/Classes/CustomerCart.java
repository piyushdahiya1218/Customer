package android.example.customer.Classes;

import java.util.ArrayList;

public class CustomerCart {
    ArrayList<Product> products;
    int Tprice;
    int Titems;

    public CustomerCart() {
    }

    public CustomerCart(ArrayList<Product> products, int tprice, int titems) {
        this.products = products;
        Tprice = tprice;
        Titems = titems;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public int getTprice() {
        return Tprice;
    }

    public void setTprice(int tprice) {
        Tprice = tprice;
    }

    public int getTitems() {
        return Titems;
    }

    public void setTitems(int titems) {
        Titems = titems;
    }
}
