package atirek.pothiwala.locator;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

public class PlacePicker {

    private Context context;

    public PlacePicker(@NonNull Context context, String TAG, boolean enableDebug) {
        this.context = context;
        PickerActivity.TAG = TAG;
        PickerActivity.enableDebug = enableDebug;
    }

    public PlacePicker setListener(@NonNull PickerActivity.Listener listener) {
        PickerActivity.listener = listener;
        return this;
    }

    public PlacePicker setGoogleApi(@NonNull String key) {
        PickerActivity.key = key;
        return this;
    }


    public PlacePicker setLanguage(@Nullable String language) {
        PickerActivity.language = language;
        return this;
    }

    public PlacePicker setCountryCode(@Nullable String countryCode) {
        PickerActivity.countryCode = countryCode;
        return this;
    }

    public PlacePicker setSourceLocation(@NonNull LatLng source) {
        PickerActivity.sourceLocation = source;
        return this;
    }

    void start(boolean startPicker) {
        Intent intent = new Intent(context, PickerActivity.class);
        intent.putExtra("startPicker", startPicker);
        context.startActivity(intent);
    }

    public static String getAddress(Context context, LatLng position) {
        return getAddress(context, position, "en");
    }

    public static String getAddress(Context context, LatLng position, String language) {
        final String[] address = {context.getString(R.string.unknownAddress)};
        try {
            address[0] = getGeoAddress(context, position, language).getAddressLine(0).replace(",,", ",");
        } catch (Exception e) {
            e.printStackTrace();
            address[0] = context.getString(R.string.unknownAddress);
        }
        return address[0];
    }

    public static Address getGeoAddress(Context context, LatLng position, String language) {
        try {
            Geocoder geocoder = new Geocoder(context, new Locale(language));
            List<Address> addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
