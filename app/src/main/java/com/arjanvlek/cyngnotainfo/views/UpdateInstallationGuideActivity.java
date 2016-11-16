package com.arjanvlek.cyngnotainfo.views;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.ApplicationContext;
import com.arjanvlek.cyngnotainfo.Model.InstallGuideData;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.arjanvlek.cyngnotainfo.ApplicationContext.LOCALE_DUTCH;
import static com.arjanvlek.cyngnotainfo.ApplicationContext.NUMBER_OF_INSTALL_GUIDE_PAGES;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_DEVICE_ID;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_UPDATE_METHOD_ID;

public class UpdateInstallationGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_installation_instructions);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.updateInstallationInstructionsPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }
    
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a InstallationGuideFragment (defined as a static inner class below).
            return InstallationGuideFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return NUMBER_OF_INSTALL_GUIDE_PAGES;
        }
    }



    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
