package com.diego.actividad11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private TextInputEditText editTextAddress;
    private Button buttonSearch;
    private Spinner spinnerMapType;
    private LatLng currentMarkerLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = findViewById(R.id.editTextAddress);
        buttonSearch = findViewById(R.id.buttonSearch);
        spinnerMapType = findViewById(R.id.spinnerMapType);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonSearch.setOnClickListener(v -> searchAddress());

        spinnerMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (googleMap != null) {
                    updateMapType(position);
                    if (currentMarkerLocation != null) {
                        placeMarker(currentMarkerLocation, editTextAddress.getText().toString(), position);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Si no tiene permisos, solicitamos ambos (COARSE y FINE)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, habilitamos la capa de ubicación
                enableMyLocation();
            } else {
                // Permiso denegado.
                Toast.makeText(this, "El permiso de ubicación fue denegado.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void enableMyLocation() {
        if (googleMap != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng defaultLocation = new LatLng(19.4326, -99.1332); // Ciudad de México
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        updateMapType(spinnerMapType.getSelectedItemPosition());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    /**
     * 🗺️ CORREGIDO: Método para cambiar el tipo de mapa de Google Maps.
     */
    private void updateMapType(int position) {
        if (googleMap == null) return;

        switch (position) {
            case 0: // Vista Predeterminada (Normal)
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1: // Vista Satelital
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 2: // Vista de Relieve (Terrain)
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private void searchAddress() {
        String addressString = editTextAddress.getText().toString().trim();
        if (addressString.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa una dirección", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                placeMarker(location, addressString, spinnerMapType.getSelectedItemPosition());

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            } else {
                Toast.makeText(this, "Dirección no encontrada. Intenta ser más específico.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("GEOCODER_ERROR", "Error de Geocodificación: " + e.getMessage());
            Toast.makeText(this, "Error al conectar con el servicio de búsqueda.", Toast.LENGTH_LONG).show();
        }
    }

    private void placeMarker(LatLng latLng, String title, int mapTypePosition) {
        if (googleMap == null) return;

        googleMap.clear();
        currentMarkerLocation = latLng;

        float markerColor;

        switch (mapTypePosition) {
            case 0:
                markerColor = BitmapDescriptorFactory.HUE_AZURE;
                break;
            case 1:
                markerColor = BitmapDescriptorFactory.HUE_RED;
                break;
            case 2:
                markerColor = BitmapDescriptorFactory.HUE_GREEN;
                break;
            default:
                markerColor = BitmapDescriptorFactory.HUE_AZURE;
        }

        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
    }
}