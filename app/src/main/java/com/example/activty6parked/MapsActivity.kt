package com.example.activty6parked

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var carLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val saveLocationButton: Button = findViewById(R.id.saveLocationButton)
        saveLocationButton.setOnClickListener {
            saveCarLocation()
            getCurrentLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        restoreLocation()?.let {
            moveCameraToLocation(it)
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
            return
        }
        map.isMyLocationEnabled = true
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle the case where permissions are not granted
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    moveCameraToLocation(currentLatLng)
                }
            }
    }

    private fun moveCameraToLocation(location: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
    }

    private fun saveCarLocation() {
        map.cameraPosition.target?.let {
            carLocation = it

            // Create a custom icon with canvas
            val iconBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(iconBitmap)
            val iconDrawable = ContextCompat.getDrawable(this, R.drawable.baseline_directions_car_24)
            iconDrawable?.setBounds(0, 0, canvas.width, canvas.height)
            iconDrawable?.draw(canvas)

            // Add the marker with the custom icon
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Car")
                    .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
            )

            saveLocation(it)
        }
    }

    private fun saveLocation(latLng: LatLng) {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("latitude", latLng.latitude.toString())
            putString("longitude", latLng.longitude.toString())
            apply()
        }
    }

    private fun restoreLocation(): LatLng? {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getString("latitude", null)?.toDoubleOrNull()
        val longitude = sharedPreferences.getString("longitude", null)?.toDoubleOrNull()
        return latitude?.let { lat ->
            longitude?.let { lng ->
                LatLng(lat, lng)
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 10f
        private const val PERMISSIONS_REQUEST_LOCATION = 1
    }
}
