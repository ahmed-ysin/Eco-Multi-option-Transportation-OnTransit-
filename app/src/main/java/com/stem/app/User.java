package com.stem.app;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.stem.app.Model.Customer;
import com.stem.app.Model.Driver;

import java.util.HashMap;
import java.util.Random;

public class User {
    private static FirebaseUser user;
    static Customer selectedCustomer;
    static Driver selectedDriver;
    static Driver currentDriver;
    static Customer currentCustomer;
    public enum CarTypes {Taxi,Bus,Microbus}
    public enum FuelType {Gasoline_80,Gasoline_92,Gasoline_95,Diesel,Natural_Gas}

    private static final String customerCollection = "customers";
    static final String driversColliction = "drivers";

    static FirebaseUser GetCurrentUser(){
        return user;
    }
    static void SetCurrentUser(FirebaseUser newUser){
        user = newUser;
    }

    static Customer CreateCustomer(LatLng latLng){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Long uniqueID = System.currentTimeMillis()/1000;

        Customer customer = new Customer(
                uniqueID.toString(),
                getRandomName(),
                getRandomPhone(),
                true,
                latLng.latitude,
                latLng.longitude,
                getRandomLatitude(0.004D),
                getRandomLongitude( 0.02D),
                false,
                User.GetCurrentUser().getUid()
                );

        // Add a new document with a generated ID
        db.collection("customers").document(customer.key).set(customer);

        return customer;
    }
    static void CreateCustomer(String name,String phone){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Long uniqueID = System.currentTimeMillis()/1000;

        Customer customer = new Customer(
                uniqueID.toString(),
                name,
                phone,
                true,
                getRandomLatitude(0D),
                getRandomLongitude(0D),
                0D,
                0D,
                false,
               ""
        );

        // Add a new document with a generated ID
        db.collection("customers").document(customer.key).set(customer);
        currentCustomer = customer;
    }



    static void UpdateNewLocation(LatLng newLatlng){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> customer = new HashMap<>();
        customer.put("nLatitude",newLatlng.latitude);
        customer.put("nLongitude",newLatlng.longitude);

        db.collection(customerCollection).document(currentCustomer.key).update(customer);
    }

    static Driver CreateDriver(Driver driver){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        driver.latitude = getRandomLatitude(0D);
        driver.longitude  =  getRandomLongitude(0D);

        // Add a new document with a generated ID
        db.collection(driversColliction).document(driver.key).set(driver);
        return driver;
    }
    static Driver CreateRandomDriver(){
        Long uniqueID = System.currentTimeMillis()/1000;

        Driver driver = new Driver();
        driver.key =uniqueID.toString();
        driver.name = getRandomDriverName();
        driver.phone = getRandomPhone();
        driver.carNumber = getRandomCarNumber();
        driver.carType = getRandomCarType();
        driver.cost = getRandomCost();
        driver.isOnline = true;
        driver.fuelType = getRandomFuelType();
        driver.fuelConsumption = 5D;
        driver.speed = getRandomSpeed();
        return CreateDriver(driver);
    }

    static void SetDriverOnline(boolean isOnline,String key){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> driver = new HashMap<>();
        driver.put("isOnline",isOnline);

        db.collection(driversColliction).document(key).update(driver);
        if(!isOnline) {
            SignOut();
        }
    }

    static void UpdateCustomerAcceptState(boolean isAccepted, String customerKey,String state){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> customer = new HashMap<>();

        if(currentCustomer != null) currentCustomer.isAccepted = isAccepted;
        if(selectedCustomer != null) selectedCustomer.isAccepted =isAccepted;
        customer.put("orderState",state);

        if(isAccepted){
            customer.put("isAccepted","true");
            db.collection("customers").document(customerKey).update(customer);
        }
        else{
            customer.put("isAccepted","false");
            customer.put("driverKey","");

            if(currentCustomer != null) currentCustomer.driverKey ="";
            if(selectedCustomer != null) selectedCustomer.driverKey ="";

            db.collection(customerCollection).document(customerKey).update(customer);
        }
    }
    static void SendOrderRequest(String customerKey){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> customer = new HashMap<>();

        customer.put("isAccepted","false");
        customer.put("driverKey",selectedDriver.key);
        customer.put("orderState","Pending");

        currentCustomer.driverKey =selectedDriver.key;

        db.collection(customerCollection).document(customerKey).update(customer);
    }
    static void DeleteCustomer(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(customerCollection).document(currentCustomer.key).delete();
        SignOut();
    }


    static void SignOut(){
        currentDriver = null;
        currentCustomer = null;
        selectedCustomer = null;
        user = null;
    }
    private static double getRandomLatitude(Double space){
        Double defaultNumber = 31D;
        Double min = .030D;
        Double max = .04600D;

        Random r = new Random();
        return  (min + (max - min) * r.nextDouble()) + defaultNumber + space;
    }
    private static double getRandomLongitude(Double space){
        Double defaultNumber = 31D;
        Double min = .35D;
        Double max = .37D;

        Random r = new Random();
        return  (min + (max - min) * r.nextDouble()) + defaultNumber + space;
    }

    private static String getRandomName(){
        String[] firstNames = {"Asmaa","Marwa","Ahmed","Mohamed","Mahmoud","Shrouk","Reda"};
        String[] secNames = {"Ahmed","Mohamed","Reda","Mahmoud","M Saad","Abd Elhamed"};

        int rnd = new Random().nextInt(firstNames.length);
        int rnd2 = new Random().nextInt(secNames.length);
        return firstNames[rnd] + " " + secNames[rnd2] ;
    }
    private static String getRandomDriverName(){
        String[] Names = {"Ahmed","Mohamed","Reda","Mahmoud","M Saad","Abd Elhamed"};

        int rnd = new Random().nextInt(Names.length);
        int rnd2 = new Random().nextInt(Names.length);
        return Names[rnd] + " " + Names[rnd2] ;
    }
    private static String getRandomPhone(){
        String[] phones = {"01000000000","01164778855","01255228899"};
        int rnd = new Random().nextInt(phones.length);
        return phones[rnd] ;
    }
    private static String getRandomCarNumber(){
        String[] phones = {"123-LRA","561-EGY","856-HGA"};
        int rnd = new Random().nextInt(phones.length);
        return phones[rnd] ;
    }
    private static Double getRandomCost(){
        Double[] costs = {1.5D,2D,3.5D};
        int rnd = new Random().nextInt(costs.length);
        return costs[rnd] ;
    }
    private static Double getRandomSpeed(){
        Double[] speeds = {50D,45D,30.5D,33D};
        int rnd = new Random().nextInt(speeds.length);
        return speeds[rnd] ;
    }
    private static CarTypes getRandomCarType(){
        int rnd = new Random().nextInt(CarTypes.values().length);
        return CarTypes.values()[rnd];
    }
    private static FuelType getRandomFuelType(){
        int rnd = new Random().nextInt(FuelType.values().length);
        return FuelType.values()[rnd];
    }
}
