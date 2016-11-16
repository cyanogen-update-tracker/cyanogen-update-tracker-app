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

    public static class InstallationGuideFragment extends Fragment {
        /**
         * The fragment argument representing the page number for this
         * fragment.
         */
        private static final String ARG_PAGE_NUMBER = "page_number";

        /**
         * Returns a new instance of this fragment for the given page
         * number.
         */
        public static InstallationGuideFragment newInstance(int pageNumber) {
            InstallationGuideFragment fragment = new InstallationGuideFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_PAGE_NUMBER, pageNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_install_guide_base, container, false);
        }

        private class fetchInstallGuide extends AsyncTask<Object, Void, List<Object>> {

            @Override
            protected List<Object> doInBackground(Object... params) {
                List<Object> returns = new ArrayList<>();
                View installGuideView = (View) params[0];
                returns.add(0, installGuideView);

                int pageNumber = (int) params[1];
                long deviceId = (long) params[2];
                long updateMethodId = (long) params[3];
                ServerConnector connector = ((ApplicationContext)getActivity().getApplication()).getServerConnector();
                returns.add(1, connector.fetchInstallGuidePageFromServer(pageNumber, deviceId, updateMethodId));
                returns.add(2, pageNumber);
                return returns;
            }

            @Override
            protected void onPostExecute(List<Object> installGuideData) {
                View installGuideView = (View) installGuideData.get(0);
                InstallGuideData data = (InstallGuideData) installGuideData.get(1);
                int pageNumber = (int) installGuideData.get(2);
                displayInstallGuide(installGuideView, data, pageNumber);
            }
        }

        private void displayInstallGuide(View installGuideView, InstallGuideData installGuideData, int pageNumber) {
            if(installGuideData == null) {
                displayDefaultInstallGuide(installGuideView, pageNumber);
            } else {
                displayCustomInstallGuide(installGuideView, installGuideData);
            }
        }

        private void displayDefaultInstallGuide(View installGuideView, int pageNumber) {

        }

        private void displayCustomInstallGuide(View installGuideView, InstallGuideData installGuideData) {
            final TextView titleTextView = (TextView) installGuideView.findViewById(R.id.installGuideTitle);
            final TextView contentsTextView = (TextView) installGuideView.findViewById(R.id.installGuideText);
            final String appLocale = Locale.getDefault().getDisplayLanguage();


            if(appLocale.equals(LOCALE_DUTCH)) {
                titleTextView.setText(installGuideData.getDutchTitle());
                contentsTextView.setText(installGuideData.getDutchText());
            } else {
                titleTextView.setText(installGuideData.getEnglishTitle());
                contentsTextView.setText(installGuideData.getEnglishText());
            }
        }

        private void loadCustomImage (ImageView view, Bitmap image) {

        }
    }

    public void closeTutorial(View view) {
        finish();
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
