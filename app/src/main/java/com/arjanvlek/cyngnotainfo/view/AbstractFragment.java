package com.arjanvlek.cyngnotainfo.view;

import android.support.v4.app.Fragment;

import com.arjanvlek.cyngnotainfo.ApplicationContext;



public abstract class AbstractFragment extends Fragment{

    private ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext() {
        if(applicationContext == null) {
            try {
                applicationContext = (ApplicationContext) getActivity().getApplication();
            } catch (Exception e) {
                applicationContext = new ApplicationContext();
            }
        }
        return applicationContext;
    }
}
