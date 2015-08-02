package com.arjanvlek.cyngnotainfo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.views.UpdateDescriptionActivity;
import com.robotium.solo.Solo;

public class UpdateDescriptionTest extends ActivityInstrumentationTestCase2<MainActivity>{
    private Solo solo;

    public UpdateDescriptionTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    public void testUpdateDescription() throws Exception{
        Button updateDescriptionButton = (Button)solo.getView(R.id.updateInformationUpdateDescriptionButton);
        solo.clickOnView(updateDescriptionButton);
        solo.waitForActivity(UpdateDescriptionActivity.class);
        String updateDescription = "This update provides the best test results for your device :-)";
        TextView updateDescriptionTextView = (TextView)solo.getView(R.id.updateDescriptionView);
        assertEquals(updateDescription, updateDescriptionTextView.getText());
    }
}
