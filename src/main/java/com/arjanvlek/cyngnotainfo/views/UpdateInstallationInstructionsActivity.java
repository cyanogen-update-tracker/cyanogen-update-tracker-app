package com.arjanvlek.cyngnotainfo.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arjanvlek.cyngnotainfo.R;

public class UpdateInstallationInstructionsActivity extends AppCompatActivity {

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_installation_instructions);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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
            // Return a TutorialFragment (defined as a static inner class below).
            return TutorialFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TutorialFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TutorialFragment newInstance(int sectionNumber) {
            TutorialFragment fragment = new TutorialFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int sectionNumber = args.getInt(ARG_SECTION_NUMBER,0);
            if(sectionNumber == 1) {
                return inflater.inflate(R.layout.fragment_update_installation_instructions_1, container, false);
            }
            else if(sectionNumber == 2) {
                return inflater.inflate(R.layout.fragment_update_installation_instructions_2, container, false);

            }
            else if(sectionNumber == 3) {
                return inflater.inflate(R.layout.fragment_update_installation_instructions_3, container, false);

            }
            else if(sectionNumber == 4) {
                return inflater.inflate(R.layout.fragment_update_installation_instructions_4, container, false);

            }
            else if(sectionNumber == 5) {
                return inflater.inflate(R.layout.fragment_update_installation_instructions_5, container, false);

            }
            return null;
        }
    }
    public void closeTutorial(View view) {
        finish();
    }

}
