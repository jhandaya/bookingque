package com.nirwanastudio.bookingque;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookingFragment extends Fragment {


    public BookingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_booking, container, false);
        TextView tvPhone = (TextView) v.findViewById(R.id.tvPhone);
        EditText etPhone = (EditText)v.findViewById(R.id.etPhone);
        Button btnOK = (Button) v.findViewById(R.id.btnOK);

        tvPhone.setText("Phone Number");
        etPhone.setText("Input a Phone Number");
        btnOK.setText("Confirm");
        return v;
    }

}
