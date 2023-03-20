package com.stem.app.Model;

import java.util.HashMap;

public class Customer {
    public String key;
    public String name;
    public String phone;
    public boolean isOnline;
    public Double cLatitude;
    public Double cLongitude;
    public Double nLatitude;
    public Double nLongitude;
    public boolean isAccepted;
    public String driverKey;
    public String cAddress;
    public String going;
    public String orderState;

    public Customer(String key, String name, String phone, boolean isOnline, Double cLatitude, Double cLongitude, Double nLatitude, Double nLongitude, boolean isAccepted, String driverKey) {
        this.key = key;
        this.name = name;
        this.phone = phone;
        this.isOnline = isOnline;
        this.cLatitude = cLatitude;
        this.cLongitude = cLongitude;
        this.nLatitude = nLatitude;
        this.nLongitude = nLongitude;
        this.isAccepted = isAccepted;
        this.driverKey = driverKey;
        this.orderState = "";
    }

    public  Customer(HashMap<String,Object> data){
        System.out.println("data =======>" +  data);

        this.key = data.get("key").toString();
        this.name = data.get("name").toString();
        this.phone = data.get("phone").toString();
        this.isOnline = data.get("isOnline").toString().equals("true");
        this.cLatitude = Double.parseDouble(data.get("cLatitude").toString());
        this.cLongitude = Double.parseDouble(data.get("cLongitude").toString());
        this.nLatitude =Double.parseDouble(data.get("nLatitude").toString());
        this.nLongitude =Double.parseDouble(data.get("nLongitude").toString());
        this.isAccepted = data.get("isAccepted").toString().equals("true");
        this.driverKey =data.get("driverKey").toString();
        this.orderState = data.get("orderState").toString();
    }
}
