package com.windowsazure.messaging;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public final class GcmCredential extends PnsCredential {

    private String googleApiKey;

    public GcmCredential() {
        this(null);
    }

    public GcmCredential(String googleApiKey) {
        super();
        this.setGoogleApiKey(googleApiKey);
    }

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    public void setgoogleApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
    }

    public void setGoogleApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
    }

    @Override
    public List<SimpleEntry<String, String>> getProperties() {
        ArrayList<SimpleEntry<String, String>> result = new ArrayList<>();
        result.add(new SimpleEntry<>("GoogleApiKey", getGoogleApiKey()));
        return result;
    }

    @Override
    public String getRootTagName() {
        return "GcmCredential";
    }
}
