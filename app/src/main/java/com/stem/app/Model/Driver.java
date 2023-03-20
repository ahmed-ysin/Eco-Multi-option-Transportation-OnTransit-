package com.stem.app.Model;

import com.stem.app.User;

import java.util.HashMap;

public class Driver {
    public String key;
    public String name;
    public String phone;
    public boolean isOnline;
    public Double latitude;
    public Double longitude;
    public Double cost;
    public Double fuelConsumption;
    public Double speed = 50D;
    public String address;
    public String carNumber;
    public User.CarTypes carType;
    public User.FuelType fuelType;

    public Driver(){
    }


    public Driver(HashMap<String, Object> data) {
        this.key = data.get("key").toString();
        this.name = data.get("name").toString();
        this.phone = data.get("phone").toString();
        this.carNumber = data.get("phone").toString();
        this.isOnline = true;
        this.latitude = Double.parseDouble(data.get("latitude").toString());
        this.longitude = Double.parseDouble(data.get("longitude").toString());
        this.carType = User.CarTypes.valueOf(data.get("carType").toString());
        this.fuelType = User.FuelType.valueOf(data.get("fuelType").toString());
        this.speed = Double.parseDouble(data.get("speed").toString());
        this.cost = Double.parseDouble(data.get("cost").toString());
        this.fuelConsumption = Double.parseDouble(data.get("fuelConsumption").toString());

    }
}
