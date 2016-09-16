package com.example.bhavya.helpme;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;

/**q
 * Created by bhavya on 16/9/16.
 */
public class ContactsDefaultFragment extends Fragment{
    @BindView(R.id.contact_emergency_default_textview) TextView mEmergencyContactNo;
//    @BindView(R.id.contact_emergency_default_button) Button mEmergencyContactButton;
    Button mEmergencyContactButton;
    TextView mEmergencyContactTextView;
    public ContactsDefaultFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_default,container,false);
        Bundle args = getArguments();
        mEmergencyContactTextView = (TextView)view.findViewById(R.id.contact_emergency_default_textview);
        if(null != args){
            mEmergencyContactTextView.setText("Your selected contact is "+args.getSerializable("emergencyContact"));
        } else {
            mEmergencyContactTextView.setText("No contact specified");
        }

        mEmergencyContactButton = (Button)view.findViewById(R.id.contact_emergency_default_button);
            mEmergencyContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),"Sends sms to contact",Toast.LENGTH_SHORT).show();
                }
            });

        return view;
    }
}
