package com.stem.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.stem.app.Model.Customer;
import com.stem.app.Model.Driver;

import java.text.DecimalFormat;

public class DialogDriverDetails extends DialogFragment {
    //widgets;
    private TextView txt_Duration,txt_Arrive,txt_Phone,txt_Name,txt_Cost,txt_Co2,txt_distance;
    private double speedPerMetr;
    private LinearLayout linear_Order,linear_Cancel;
    private MaterialButton btn_Order,btn_Cancel;
    private Driver driver;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_driver_details, container, false);
        txt_Duration = view.findViewById(R.id.txt_Duration);
        txt_Arrive = view.findViewById(R.id.txt_Arrive);
        txt_Phone = view.findViewById(R.id.txt_Phone);
        txt_Name = view.findViewById(R.id.txt_Name);
        txt_Cost = view.findViewById(R.id.txt_Cost);
        txt_Co2 = view.findViewById(R.id.txt_Co2);
        txt_distance = view.findViewById(R.id.txt_distance);

        linear_Order =  view.findViewById(R.id.linear_Order);
        linear_Cancel =  view.findViewById(R.id.linear_Cancel);

        btn_Order =  view.findViewById(R.id.btn_Order);
        btn_Cancel =  view.findViewById(R.id.btn_Cancel);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(User.selectedDriver == null) dismiss();

        driver = User.selectedDriver;
        Customer customer = User.currentCustomer;

        speedPerMetr = ((driver.speed *1000)/ 3600);

        double meterDistance = distance(customer.cLatitude,customer.nLatitude,customer.cLongitude,customer.nLongitude);

        int toCustomerResult = (int) (distance(driver.latitude,customer.cLatitude,driver.longitude,customer.cLongitude ) / speedPerMetr / 10);
        int toNewLocationResult  =(int) ( meterDistance / speedPerMetr / 10);

        txt_Name.setText(driver.name);
        txt_Phone.setText(driver.phone);


        txt_Arrive.setText("~" + toCustomerResult +" min");
        txt_Duration.setText("~" + toNewLocationResult + " min");

        txt_Cost.setText(df2.format(GetCost(meterDistance,driver.cost)) + " LE");
        txt_Co2.setText(df2.format(GetCo2(meterDistance)) + " Kg");
        txt_distance.setText(df2.format(meterDistance / 1000) + " Km");

        IntialButtonActions(customer.key);
        SetVisiblity();
    }

    int distance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        double correctingdistance = (distance * 0.35);
        return (int) (distance + correctingdistance);
    }

    double GetCost (double distancePerMeter,double costPerKilo){
       double costPerMeter = costPerKilo / 1000;
       return costPerMeter * distancePerMeter;
    }

    double GetCo2(double distancePerMeter){
        double co2Weight= 44.01D;
        double distancePerKiloMeter = distancePerMeter / 1000;
        double carbonContentPerLiter = GetCarbonContent(driver.fuelType);

        // (DistancePerKilo / (Liter DistancePerKilo)) * CarbonContentPerLiter * Co2 weight (44)g
        return ((distancePerKiloMeter / driver.fuelConsumption) * carbonContentPerLiter * co2Weight)  / 1000;
    }

    private double GetCarbonContent(User.FuelType fuelType) {
        switch (fuelType) {
            case Gasoline_80:
                return 56;
            case Gasoline_92:
                return 53;
            case Gasoline_95:
                return 51;
            case Diesel:
                return 60;
            case Natural_Gas:
                return 38;
        }
        return 0;
    }


    private void IntialButtonActions(final String key) {
        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.UpdateCustomerAcceptState(false,key,"Canceled");
                dismiss();
            }
        });

        btn_Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.SendOrderRequest(key);
                dismiss();
            }
        });
    }

    public void SetVisiblity(){
        boolean isOrder = !User.currentCustomer.driverKey.equals("");
        boolean isSameDriver = User.selectedDriver.key.equals(User.currentCustomer.driverKey);

        if(isSameDriver && isOrder){
            linear_Order.setVisibility(View.GONE);
            linear_Cancel.setVisibility(View.GONE);
            return;
        }

        linear_Order.setVisibility(isOrder? View.GONE : View.VISIBLE);
        linear_Cancel.setVisibility(isSameDriver? View.VISIBLE : View.GONE);


    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
