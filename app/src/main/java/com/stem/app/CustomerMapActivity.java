package com.stem.app;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.stem.app.Model.Customer;
import com.stem.app.Model.Driver;

import java.io.IOException;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Map<String,Marker> markers;
    Marker oldDirection =null;
    Geocoder geocoder;
    MaterialButton btn_AddDriver,btn_Cancel;
    ListenerRegistration query,query2;
    DialogDriverDetails dialog ;
    TextView txt_desc;
    LinearLayout linear_Cancel;

//                         android:text="your order is pending confirmation.."

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dialog = null;
        geocoder = new Geocoder(getApplicationContext());
        btn_AddDriver = findViewById(R.id.btn_AddDriver);
        btn_Cancel = findViewById(R.id.btn_Cancel);
        txt_desc = findViewById(R.id.txt_desc);
        linear_Cancel = findViewById(R.id.linear_Cancel);

        btn_AddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.CreateRandomDriver();
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.UpdateCustomerAcceptState(false,User.currentCustomer.key,"Canceled");
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);

        if(User.currentCustomer == null)
            startActivity(new Intent(CustomerMapActivity.this,CustomerRequst.class));

        LatLng latLng = new LatLng(User.currentCustomer.cLatitude,User.currentCustomer.cLongitude);

        mMap.addMarker(new MarkerOptions().position(latLng).title(GetAddress(latLng.latitude,latLng.longitude)));
        CameraPosition pos = new CameraPosition(latLng,14,0,0);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(!User.currentCustomer.driverKey.equals("")){
                    Toast.makeText(CustomerMapActivity.this, "Sorry, You cannot change new direction because you already in order", Toast.LENGTH_SHORT).show();
                    return;
                }
                User.UpdateNewLocation(latLng);
                if(oldDirection != null){
                    oldDirection.remove();
                }

                String address = GetAddress(latLng.latitude,latLng.longitude);
                Toast.makeText(CustomerMapActivity.this, "new direction Updated Successfully", Toast.LENGTH_SHORT).show();
                oldDirection =  mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.fromResource(R.drawable.markerblue)));
            }
        });

        StartListenToCustomer();
        LoadDrivers();
    }

    private void LoadDrivers(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        markers = new HashMap<>();

        query =  db.collection("drivers")
                .whereEqualTo("isOnline", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.err.println("Listen failed: " + e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            HashMap<String,Object> data = new HashMap(dc.getDocument().getData());

                            if(!data.containsKey("isOnline")) continue;
                            if(data.get("phone").toString().equals("01144778855")) continue;
                            System.out.println("dc.getDocument().getData() ==========> " + dc.getDocument().getData());
                            System.out.println("Drivers ==========> " + data);

                            Driver driver = new Driver(data);
                            boolean inMarkers = markers.containsKey(driver.key);

                            switch (dc.getType()) {
                                case ADDED:
                                    if(!inMarkers)
                                        ShowDriversOnMap(driver);
                                    break;
                                case MODIFIED:
                                    if(inMarkers) {
                                        markers.get(driver.key).setTag(driver);
                                    }  else
                                        ShowDriversOnMap(driver);
                                    break;
                                case REMOVED:
                                    if(inMarkers) {
                                        markers.get(driver.key).remove();
                                        markers.remove(driver.key);
                                    }
                                    break;
                            }
                        }
                    }
                });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTag() == null || TextUtils.isEmpty(marker.getTag().toString())) {
                    return false;
                }

                if(!User.currentCustomer.driverKey.equals(""))
                {
                    Toast.makeText(CustomerMapActivity.this, "you already have order , Please cancel it if u want change driver", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(User.currentCustomer.nLongitude == 0){
                    Toast.makeText(CustomerMapActivity.this, "Please long press on a location you want to go to.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                User.selectedDriver = (Driver)marker.getTag();
                OpenDialog();
                return false;
            }
        });
    }

    private  void ShowDriversOnMap(Driver driver){
        try {
            driver.address = GetAddress(driver.latitude,driver.longitude);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(driver.latitude,driver.longitude))
                    .title(driver.name)
                    .icon(BitmapDescriptorFactory.fromResource(GetCarIcon(driver.carType))));
            marker.setTag(driver);
            markers.put(driver.key,marker);
            System.out.println(markers.size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private int GetCarIcon(User.CarTypes type){
        switch (type){
            case Taxi:
                return  R.drawable.taxi;
            case Bus:
                return  R.drawable.bus;
            case Microbus:
                return  R.drawable.microbus;
        }
        return R.drawable.taxi;
    }
    private String GetAddress(Double latitude,Double longitude){
        String address = "";
        try {
            List<Address> addresesList = geocoder.getFromLocation(latitude,longitude,1);
            address =  addresesList.get(0).getFeatureName();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return address;
    }
    private void OpenDialog(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }

        dialog = new DialogDriverDetails();
        dialog.show(getSupportFragmentManager(),"Dialog");
    }

    private void StartListenToCustomer(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        query2 = db.collection("customers")
                .whereEqualTo("key", User.currentCustomer.key)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.err.println("Listen failed: " + e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            HashMap<String,Object> data = new HashMap(dc.getDocument().getData());

                            User.currentCustomer = new Customer(
                                    User.currentCustomer.key,
                                    User.currentCustomer.name,
                                    User. currentCustomer.phone,
                                    true,
                                    User.currentCustomer.cLatitude,
                                    User.currentCustomer.cLongitude,
                                    Double.parseDouble(data.get("nLatitude").toString()),
                                    Double.parseDouble(data.get("nLongitude").toString()),
                                    data.get("isAccepted").toString().equals(true),
                                    data.get("driverKey").toString()
                            );
                            User.currentCustomer.orderState =  data.get("orderState").toString();
                            UpdateState();
                        }
                    }
                });
    }

    void UpdateState(){
        String msg = "" ;
        linear_Cancel.setVisibility(View.GONE);

        if (User.currentCustomer.nLongitude == 0){
            msg = "Please long press on a location you want to go to.";
        }
        else if(User.currentCustomer.orderState.equals("")){
            msg = "Please select car you want to ride and start Order.";
        }
        else if(User.currentCustomer.orderState.equals("Accepted")){
            msg = "Your order is accepted";
            linear_Cancel.setVisibility(View.VISIBLE);
        }
        else if(User.currentCustomer.orderState.equals("Refused")){
            msg = "Your order is refused, Please select another car";
        }
        else if(User.currentCustomer.orderState.equals("Canceled")){
            msg = "Your order is Canceled, Please select another car";
        }
        else if(User.currentCustomer.orderState.equals("Pending")){
            msg = "Your order has been sent";
            linear_Cancel.setVisibility(View.VISIBLE);
        }
        else if(User.currentCustomer.orderState.equals("Rejected")){
            msg = "Your order is Rejected, Please select another car";
        }

        if(dialog != null) {
            dialog.SetVisiblity();
        }

        txt_desc.setText(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        markers = null;
        query.remove();
        query2.remove();
        mMap.clear();

        User.DeleteCustomer();
    }
}
