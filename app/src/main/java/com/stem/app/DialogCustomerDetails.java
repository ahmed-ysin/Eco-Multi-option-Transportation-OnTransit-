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

public class DialogCustomerDetails extends DialogFragment {
    //widgets;
    private TextView txt_Duration,txt_Going,txt_Arrive,txt_cLocation,txt_Phone,txt_Name;
    private double speedPerMetr;
    private LinearLayout linear_Accepted,linear_Request;
    private MaterialButton btn_rejact,btn_accept,btn_refuse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_customer_details, container, false);
        txt_Duration = view.findViewById(R.id.txt_Duration);
        txt_Going = view.findViewById(R.id.txt_Going);
        txt_Arrive = view.findViewById(R.id.txt_Arrive);
        txt_cLocation = view.findViewById(R.id.txt_cLocation);
        txt_Phone = view.findViewById(R.id.txt_Phone);
        txt_Name = view.findViewById(R.id.txt_Name);
        linear_Accepted =  view.findViewById(R.id.linear_Accepted);
        linear_Request =  view.findViewById(R.id.linear_Request);

        btn_rejact =  view.findViewById(R.id.btn_rejact);
        btn_accept =  view.findViewById(R.id.btn_accept);
        btn_refuse =  view.findViewById(R.id.btn_refuse);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(User.selectedCustomer == null) dismiss();

        final Customer customer = User.selectedCustomer;
        Driver driver = User.currentDriver;
        speedPerMetr = ((driver.speed *1000)/ 3600);
        linear_Accepted.setVisibility(customer.isAccepted? View.VISIBLE : View.GONE);
        linear_Request.setVisibility((!customer.isAccepted)? View.VISIBLE : View.GONE);

        int toCustomerResult = distance(driver.latitude,customer.cLatitude,driver.longitude,customer.cLongitude ) ;
        int toNewLocationResult  = distance(customer.cLatitude,customer.nLatitude,customer.cLongitude,customer.nLongitude);

        txt_Name.setText(customer.name);
        txt_Phone.setText(customer.phone);
        txt_cLocation.setText(customer.cAddress);
        txt_Going.setText(customer.going);

        txt_Arrive.setText("~" + toCustomerResult + " min");
        txt_Duration.setText("~" + toNewLocationResult + " min");

        IntialButtonActions(customer.key);
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

        return (int)Math.max(((distance + (distance * 0.25) )/ speedPerMetr / 10),1);
    }


    private void IntialButtonActions(final String key) {

        btn_rejact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.UpdateCustomerAcceptState(false,key,"Rejected");
                dismiss();
            }
        });

        btn_refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.UpdateCustomerAcceptState(false,key,"Refused");
                dismiss();
            }
        });

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.UpdateCustomerAcceptState(true,key,"Accepted");
                dismiss();
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
