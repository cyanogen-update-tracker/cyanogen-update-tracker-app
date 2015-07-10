package com.arjanvlek.cyngnotainfo.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.R;

public class UpdateDescriptionActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_details);
        TextView updateDetailsField = (TextView) findViewById(R.id.textView_update_details);
        String updateDetails = null;
        try {
            updateDetails = getIntent().getExtras().getString("update-description");
        } catch (NullPointerException e) {
            updateDetailsField.setText(getString(R.string.update_details_not_available));
        }
        if (updateDetails != null && !updateDetails.equals("") && !updateDetails.equals("null")) {
            updateDetailsField.setText(updateDetails);
        } else {
            updateDetailsField.setText(getString(R.string.update_details_not_available));
        }
    }

    public void onCloseButtonClicked(View view) {
        finish();
    }
}
