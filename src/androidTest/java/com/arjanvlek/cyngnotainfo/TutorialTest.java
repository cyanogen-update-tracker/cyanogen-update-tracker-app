package com.arjanvlek.cyngnotainfo;

import android.test.ActivityInstrumentationTestCase2;

import com.arjanvlek.cyngnotainfo.views.TutorialActivity;
import com.robotium.solo.Solo;

import java.util.Locale;

public class TutorialTest extends ActivityInstrumentationTestCase2<TutorialActivity> {

    private Solo solo;
    public final static String LOCALE_DUTCH = "Nederlands";

    public TutorialTest() {
        super(TutorialActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

//    public void testTutorial() throws Exception {
//        String appLocale = Locale.getDefault().getDisplayLanguage();
//
//
//    }
}