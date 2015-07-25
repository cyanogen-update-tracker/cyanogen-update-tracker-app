package com.arjanvlek.cyngnotainfo.views;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

public class TutorialActivity extends AppCompatActivity {
    private Fragment step4Fragment;
    private SettingsManager settingsManager;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception ignored) {

        }
        settingsManager = new SettingsManager(getApplicationContext());
        setContentView(R.layout.activity_tutorial);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    if (step4Fragment != null) {
                        TutorialStep4Fragment tutorialStep4Fragment = (TutorialStep4Fragment) step4Fragment;
                        tutorialStep4Fragment.fetchUpdateMethods();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a SimpleTutorialFragment (defined as a static inner class below).
            return newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    public Fragment newInstance(int sectionNumber) {
        if (sectionNumber == 3) {
            return new TutorialStep3Fragment();
        }
        if (sectionNumber == 4) {
            step4Fragment = new TutorialStep4Fragment();
            return step4Fragment;
        }
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        SimpleTutorialFragment simpleTutorialFragment = new SimpleTutorialFragment();
        simpleTutorialFragment.setArguments(args);
        return simpleTutorialFragment;
    }


    /**
     * Contains the basic / non interactive tutorial fragments.
     */
    @SuppressLint("ValidFragment")
    public class SimpleTutorialFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int sectionNumber = args.getInt(ARG_SECTION_NUMBER, 0);
            if (sectionNumber == 1) {
                return inflater.inflate(R.layout.fragment_tutorial_1, container, false);
            } else if (sectionNumber == 2) {
                return inflater.inflate(R.layout.fragment_tutorial_2, container, false);
            } else if (sectionNumber == 5) {
                return inflater.inflate(R.layout.fragment_tutorial_5, container, false);

            }
            return null;
        }
    }

    public void closeInitialTutorial(View view) {
        if (settingsManager.checkIfDeviceIsSet()) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            showSettingsWarning();
        }
    }

    private void showSettingsWarning() {
        Toast.makeText(this, getString(R.string.settings_entered_incorrectly), Toast.LENGTH_LONG).show();
    }


}
