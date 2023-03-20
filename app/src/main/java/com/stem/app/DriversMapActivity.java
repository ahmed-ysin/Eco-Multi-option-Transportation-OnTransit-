package com.stem.app;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Map<String,Marker> markers;
    Geocoder geocoder;
    Driver driver;
    MaterialButton btn_AddCustomer,btn_SignOut;
    ListenerRegistration query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btn_AddCustomer = findViewById(R.id.btn_AddCustomer);
        btn_SignOut = findViewById(R.id.btn_SignOut);

        geocoder = new Geocoder(getApplicationContext());
        driver = User.currentDriver;

        btn_AddCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btn_SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                User.SetDriverOnline(false,driver.key);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);

        mMap.addMarker(new MarkerOptions().position(new LatLng(driver.latitude,driver.longitude)).title(GetAddress(driver.latitude,driver.longitude)));
        CameraPosition pos = new CameraPosition(new LatLng(driver.latitude,driver.longitude),14,0,0);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

        LoadCustomers();
    }

    private void LoadCustomers(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
         markers = new HashMap<>();

         query = db.collection("customers")
                .whereEqualTo("driverKey", User.GetCurrentUser().getUid())
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
                            if(!data.containsKey("orderState")) continue;

                            Customer customer = new Customer(data);
                            boolean inMarkers = markers.containsKey(customer.key);

                            switch (dc.getType()) {
                                case ADDED:
                                    if(!inMarkers)
                                    {
                                        ShowCustomerOnMap(customer);
                                    }
                                    break;
                                case MODIFIED:
                                        if(inMarkers) {
                                            markers.get(customer.key).setIcon(BitmapDescriptorFactory.fromResource((customer.isAccepted ? R.drawable.human2 : R.drawable.human)));
                                            customer.cAddress = GetAddress(customer.cLatitude,customer.cLongitude);
                                            customer.going = GetAddress(customer.nLatitude,customer.nLongitude);

                                            markers.get(customer.key).setTag(customer);
                                        }  else
                                            ShowCustomerOnMap(customer);
                                    break;
                                case REMOVED:
                                    if(inMarkers)
                                    {
                                        markers.get(customer.key).remove();
                                        markers.remove(customer.key);
                                    }
                                    break;
                            }
                        }
                    }
                });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                User.CreateCustomer(latLng);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTag() == null || TextUtils.isEmpty(marker.getTag().toString())) {
                    return false;
                }

                User.selectedCustomer = (Customer)marker.getTag();
                (new DialogCustomerDetails()).show(getSupportFragmentManager(),"Dialog");
                return false;
            }
        });
    }

    private  void ShowCustomerOnMap(Customer customer){
        try {
            customer.cAddress = GetAddress(customer.cLatitude,customer.cLongitude);
            customer.going = GetAddress(customer.nLatitude,customer.nLongitude);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(customer.cLatitude,customer.cLongitude))
                    .title(customer.name)
                    .icon(BitmapDescriptorFactory.fromResource((customer.isAccepted ? R.drawable.human2 : R.drawable.human))));
            marker.setTag(customer);
            markers.put(customer.key,marker);

            if(!customer.isAccepted){
                //Show Dialog ;
                Toast.makeText(this, "you got a order request from " + customer.name, Toast.LENGTH_LONG).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        User.SetDriverOnline(false,driver.key);

        markers = null;
        query.remove();
        mMap.clear();
    }
}
