package com.arjanvlek.cyngnotainfo.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.ApplicationContext;
import com.arjanvlek.cyngnotainfo.Model.InstallGuideData;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.ApplicationContext.LOCALE_DUTCH;
import static com.arjanvlek.cyngnotainfo.ApplicationContext.NUMBER_OF_INSTALL_GUIDE_PAGES;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_DEVICE_ID;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_UPDATE_METHOD_ID;

public class InstallationGuideFragment extends Fragment {
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
        View installGuideView =  inflater.inflate(R.layout.fragment_install_guide_base, container, false);

        int pageNumber = getArguments().getInt(ARG_PAGE_NUMBER, 1);
        SettingsManager settingsManager = new SettingsManager(getContext());
        long deviceId = settingsManager.getLongPreference(PROPERTY_DEVICE_ID);
        long updateMethodId = settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID);

        new fetchInstallGuide().execute(installGuideView, pageNumber, deviceId, updateMethodId);

        return installGuideView;
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
            returns.add(1, connector.fetchInstallGuidePageFromServer(deviceId, updateMethodId, pageNumber));
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

    private class DownloadCustomImage extends AsyncTask<Object, Void, List<Object>> {

        @Override
        public List<Object> doInBackground(Object...params) {
            ImageView imageView = (ImageView) params[0];
            InstallGuideData installGuideData = (InstallGuideData) params[1];

            Bitmap image;

            try {
                InputStream in = completeImageUrl(installGuideData.getImageUrl(), installGuideData.getFileExtension()).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch(Exception ignored) {
                image = null;
            }

            List<Object> result = new ArrayList<>();
            result.add(0, imageView);
            result.add(1, image);

            return result;
        }

        @Override
        public void onPostExecute(List<Object> result) {
            ImageView imageView = (ImageView) result.get(0);
            Bitmap image = (Bitmap) result.get(1);
            if(image == null) {
                loadErrorImage(imageView);
            } else {
                loadCustomImage(imageView, image);
            }
        }
    }

    private void displayInstallGuide(View installGuideView, InstallGuideData installGuideData, int pageNumber) {
        if(installGuideData == null || installGuideData.getDeviceId() == null || installGuideData.getUpdateMethodId() == null) {
            displayDefaultInstallGuide(installGuideView, pageNumber);
        } else {
            displayCustomInstallGuide(installGuideView, pageNumber, installGuideData);
        }
    }

    private void displayDefaultInstallGuide(View installGuideView, int pageNumber) {
        final TextView titleTextView = (TextView) installGuideView.findViewById(R.id.installGuideTitle);
        final TextView contentsTextView = (TextView) installGuideView.findViewById(R.id.installGuideText);

        final String appLocale = Locale.getDefault().getDisplayLanguage();

        int titleResourceId = getResources().getIdentifier("install_guide_page_" + pageNumber + "_title", "string", getActivity().getPackageName());
        int contentsResourceId = getResources().getIdentifier("install_guide_page_" + pageNumber + "_text", "string", getActivity().getPackageName());


        if(appLocale.equals(LOCALE_DUTCH)) {
            titleTextView.setText(getString(titleResourceId));
            contentsTextView.setText(getString(contentsResourceId));
        } else {
            titleTextView.setText(titleResourceId);
            contentsTextView.setText(contentsResourceId);
        }

        loadDefaultImage((ImageView)installGuideView.findViewById(R.id.installGuideImage), pageNumber);

        installGuideView.findViewById(R.id.installGuideLoadingScreen).setVisibility(View.GONE);
        titleTextView.setVisibility(View.VISIBLE);
        contentsTextView.setVisibility(View.VISIBLE);
        if(pageNumber == NUMBER_OF_INSTALL_GUIDE_PAGES) {
            Button closeButton = (Button) installGuideView.findViewById(R.id.installGuideCloseButton);
            closeButton.setOnClickListener(closeButtonOnClickListener());
            closeButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayCustomInstallGuide(View installGuideView, int pageNumber, InstallGuideData installGuideData) {
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

        ImageView imageView = (ImageView)installGuideView.findViewById(R.id.installGuideImage);

        if(installGuideData.getUseCustomImage()) {
            new DownloadCustomImage().execute(imageView, installGuideData);
        } else {
            loadDefaultImage(imageView, pageNumber);
        }

        installGuideView.findViewById(R.id.installGuideLoadingScreen).setVisibility(View.GONE);
        titleTextView.setVisibility(View.VISIBLE);
        contentsTextView.setVisibility(View.VISIBLE);

        if(pageNumber == NUMBER_OF_INSTALL_GUIDE_PAGES) {
            Button closeButton = (Button) installGuideView.findViewById(R.id.installGuideCloseButton);
            closeButton.setOnClickListener(closeButtonOnClickListener());
            closeButton.setVisibility(View.VISIBLE);
        }
    }

    private URL completeImageUrl(String imageUrl, String fileExtension) throws MalformedURLException {
        String imageVariant;

        switch (getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                imageVariant = "ldpi";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                imageVariant = "mdpi";
                break;
            case DisplayMetrics.DENSITY_TV:
                imageVariant = "tvdpi";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                imageVariant = "hdpi";
                break;
            case DisplayMetrics.DENSITY_280:
            case DisplayMetrics.DENSITY_XHIGH:
                imageVariant = "xhdpi";
                break;
            case DisplayMetrics.DENSITY_360:
            case DisplayMetrics.DENSITY_400:
            case DisplayMetrics.DENSITY_420:
            case DisplayMetrics.DENSITY_XXHIGH:
                imageVariant = "xxhdpi";
                break;
            case DisplayMetrics.DENSITY_560:
            case DisplayMetrics.DENSITY_XXXHIGH:
                imageVariant = "xxxhdpi";
                break;
            default:
                imageVariant = "default";
        }

        return new URL(imageUrl + "_" + imageVariant + "." + fileExtension);
    }

    private void loadCustomImage (ImageView view, Bitmap image) {
        view.setImageBitmap(image);
        view.setVisibility(View.VISIBLE);
    }

    private void loadDefaultImage (ImageView view, int pageNumber) {
        int imageResourceId = getResources().getIdentifier("install_guide_page_" + pageNumber + "_image", "drawable", getActivity().getPackageName());
        Drawable image = ResourcesCompat.getDrawable(getResources(), imageResourceId, null);
        view.setImageDrawable(image);
        view.setVisibility(View.VISIBLE);
    }

    private void loadErrorImage (ImageView view) {
        Drawable errorImage = ResourcesCompat.getDrawable(getResources(), R.drawable.install_guide_error_image, null);
        view.setImageDrawable(errorImage);
        view.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener closeButtonOnClickListener () {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        };
    }
}