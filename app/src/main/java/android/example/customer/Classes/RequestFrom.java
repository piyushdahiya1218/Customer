package android.example.customer.Classes;

public class RequestFrom {
    String requestfrom,accepted;

    public RequestFrom() {
    }

    public RequestFrom(String requestfrom, String accepted) {
        this.requestfrom = requestfrom;
        this.accepted = accepted;
    }

    public String getRequestfrom() {
        return requestfrom;
    }

    public void setRequestfrom(String requestfrom) {
        this.requestfrom = requestfrom;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }
}
