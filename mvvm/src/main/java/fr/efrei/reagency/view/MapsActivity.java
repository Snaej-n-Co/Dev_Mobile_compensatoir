package fr.efrei.reagency.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import fr.efrei.reagency.R;
import fr.efrei.reagency.bo.Property;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Property> memProperties;
    private String curProperty;
    private String curLatLong;
    private String curAddr;
    private String[] arrLatLong;
    private String[] arrAddr;
    private String[] arrSold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            curProperty = extras.getString("curProperty");
            String sProperties = extras.getString("listProperty");
            String sSold = extras.getString("listSoldProperty");
            String[] val = curProperty.split("@");
            curLatLong = val[0];
            curAddr = val[1];
            String[] val1 = sProperties.split("#");
            arrSold = sSold.split("#");
            arrLatLong = new String[val1.length];
            arrAddr = new String[val1.length];
            for (int i = 0; i < val1.length; i++) {
                String[] val2 = val1[i].split("@");
                arrLatLong[i] = val2[0];
                arrAddr[i] = val2[1];
            }
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;




        // Add exemples of locations
        LatLng latlng;
        for (int i = 0; i < arrLatLong.length; i++) {
            String[] ll = arrLatLong[i].split(",");
            latlng = new LatLng(Double.valueOf(ll[0]).doubleValue(), Double.valueOf(ll[1]).doubleValue());
            if (arrAddr[i].equals(curAddr)) {
                Marker location = mMap.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title(arrAddr[i])
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                location.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14));
            }
            else {
                if (arrSold[i].equals("Sold"))
                    mMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title(arrAddr[i]));
                else
                    mMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title(arrAddr[i])
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }

        }

    }
}