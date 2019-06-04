package atirek.pothiwala.locator;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PickerActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {


    public interface Listener {
        void OnComplete(LatLng position, String address, Address data);

        void OnFailure(LatLng position);
    }

    private void checkLog(Object data) {
        if (enableDebug) {
            Log.d(TAG + ">>", data.toString());
        }
    }

    Handler handler;
    private static final int DEFAULT_ZOOM = 18;
    private static final int DEFAULT_TILT = 0;

    GoogleMap googleMap;
    Marker pickMarker, currentMarker;

    TextView tvAddress;
    ImageButton btnLocateMe;
    Button btnConfirm;

    public static PickerActivity.Listener listener;
    public static String TAG;
    public static LatLng sourceLocation;
    public static String language;
    public static String key;
    public static String countryCode;
    public static boolean enableDebug;

    FusedLocationHelper locationHelper;
    int AUTOCOMPLETE_REQUEST_CODE = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        if (!Places.isInitialized()) {
            Places.initialize(this, key, new Locale(language));
        }

        handler = new Handler();
        tvAddress = findViewById(R.id.tvAddress);
        btnLocateMe = findViewById(R.id.btnLocateMe);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvAddress.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnLocateMe.setOnClickListener(this);

        locationHelper = new FusedLocationHelper(this, TAG, enableDebug);
        locationHelper.setListener(locationListener);
        locationHelper.initializeLocationProviders();

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("startPicker", false)) {
            pickPlace();
        }
    }

    FusedLocationHelper.LocationListener locationListener = new FusedLocationHelper.LocationListener() {
        @Override
        public void onLocationReceived(@NonNull Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (currentMarker != null) {
                currentMarker.setPosition(latLng);
            } else {
                currentMarker = googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mylocation))
                        .position(latLng)
                        .draggable(false));
                if (sourceLocation == null) {
                    adjustZoomLevel(latLng);
                }
            }
        }

        @Override
        public void onLocationAvailability(boolean isAvailable) {

        }
    };

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setCompassEnabled(false);
        this.googleMap.getUiSettings().setZoomControlsEnabled(false);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);

        this.googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng center = googleMap.getCameraPosition().target;
                tvAddress.setText(null);
                pickMarker.setPosition(center);
                handler.removeCallbacks(locationRunnable);
                handler.postDelayed(locationRunnable, 1000);
            }
        });

        pickMarker = this.googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end_point))
                .position(googleMap.getCameraPosition().target)
                .draggable(false));

        if (sourceLocation != null) {
            adjustZoomLevel(sourceLocation);
        }
    }

    void pickPlace() {
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldList).setCountry(countryCode).build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    void locationConfirmation() {
        Address address = PlacePicker.getGeoAddress(this, this.googleMap.getCameraPosition().target, language);
        checkLog(String.format(Locale.getDefault(), "Latitude: %f, Longitude: %f", pickMarker.getPosition().latitude, pickMarker.getPosition().longitude));
        if (address != null) {
            String text = address.getAddressLine(0).replace(",,", ",");
            checkLog(String.format(Locale.getDefault(), "Address: %s", text));
            listener.OnComplete(pickMarker.getPosition(), text, address);
        } else {
            listener.OnFailure(pickMarker.getPosition());
        }
        finish();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            onBackPressed();
        }
    }

    Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            tvAddress.setText(PlacePicker.getAddress(PickerActivity.this, googleMap.getCameraPosition().target, language));
        }
    };

    void adjustZoomLevel(LatLng source) {
        CameraPosition cameraPosition = CameraPosition.builder().target(source).tilt(DEFAULT_TILT).zoom(DEFAULT_ZOOM).bearing(0).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirm:
                locationConfirmation();
                break;
            case R.id.btnLocateMe:
                if (pickMarker != null) {
                    adjustZoomLevel(pickMarker.getPosition());
                }
                break;
            case R.id.tvAddress:
                pickPlace();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == AUTOCOMPLETE_REQUEST_CODE && data != null) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            LatLng latLng = place.getLatLng();
            if (latLng != null) {
                pickMarker.setPosition(latLng);
                adjustZoomLevel(latLng);
            }
        }
    }
}
