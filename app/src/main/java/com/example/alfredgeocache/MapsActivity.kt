package com.example.alfredgeocache

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val SHARED_PREF_NAME = "plot"
    private val LOGGED_SHARED = "false"
    private val LONGITUDE = "long"
    private val LATITUDE = "lat"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(47.6062, 122.3321)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        actionBarUI()


    }



    override fun onMapReady(map: GoogleMap) {
        this.mMap = map
        // Prompt the user for permission.
        getLocationPermission()
        // Load Markers
        loadMarker()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    private fun getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                    lastKnownLocation!!.latitude,
                                                    lastKnownLocation!!.longitude
                                            ), DEFAULT_ZOOM.toFloat()
                                    )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(
                                CameraUpdateFactory
                                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }

    }
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                        this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }




    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun loadMarker(){

            val sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)


            val sharedPreferencess = getSharedPreferences(this.SHARED_PREF_NAME, MODE_PRIVATE)
            val lon = sharedPreferencess.getFloat(this.LONGITUDE, 0.0f)
            val lat = sharedPreferencess.getFloat(this.LATITUDE, 0.0f)


            val resumedPosition = LatLng(lon.toDouble(), lat.toDouble())
            Log.e(TAG, "My coordinates: $resumedPosition")

            mMap.addMarker(MarkerOptions().position(resumedPosition).draggable(false))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(resumedPosition))
        Log.e(TAG, "My coordinates:2 $resumedPosition")

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navigate, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val sharedPreferencess = getSharedPreferences(this.SHARED_PREF_NAME, MODE_PRIVATE)
        val lon = sharedPreferencess.getFloat(this.LONGITUDE, 0.0f).toDouble()
        val lat = sharedPreferencess.getFloat(this.LATITUDE, 0.0f).toDouble()
        //Check if navigate was clicked
        if (item.itemId == R.id.miNavigate){
        //launch Navigation in Walking form
            val gmmIntentUri =
                    Uri.parse("google.navigation:q=$lon,$lat+&mode=w")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            Log.i(TAG, "Navigate tapped")
            Log.i(TAG, "$lon, $lat")
            return true
        }

        //Calculate distance from Pin using Haversine
        if (item.itemId == R.id.miCalculate){
            val lat2 = lastKnownLocation!!.latitude
            val lon2 = lastKnownLocation!!.longitude

            val dLat = Math.toRadians(lon2 - lat)
            val dLon = Math.toRadians(lat2 - lat2)


            val originLat = Math.toRadians(lon)
            val destinationLat = Math.toRadians(lon2)
            Log.i(TAG, "$lon, $lat, $lat2, $lon2, $dLat, $dLon")
            val a = sin(dLat / 2).pow(2.0) +
                    sin(dLon / 2).pow(2.0) *
                    cos(originLat) *
                    cos(destinationLat)
            val c = 2 * asin(sqrt(a))*rad
            val distance:Double = Math.round(c * 1000.0) / 1000.0

            val distanceCar = AlertDialog.Builder(this)
                    .setTitle("Distance")
                    .setMessage("$distance KM from your vehicle")
                    .setPositiveButton("Ok"){ _, _-> }
                    .create()
            distanceCar.show()



        }

        return super.onOptionsItemSelected(item)
    }



private fun actionBarUI(){
    val fab: View = findViewById(R.id.fab)
    fab.setOnClickListener {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        //add marker to current location
                        mMap.clear()
                         mMap.addMarker(
                                MarkerOptions().position(
                                        LatLng(
                                                lastKnownLocation!!.latitude,
                                                lastKnownLocation!!.longitude
                                        )
                                )
                        )
                        val lat = lastKnownLocation!!.latitude
                        val lon = lastKnownLocation!!.longitude

                        val sharedPreferences: SharedPreferences =
                                this.getSharedPreferences(
                                        SHARED_PREF_NAME,
                                        Context.MODE_PRIVATE
                                )
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(LOGGED_SHARED, true)
                        editor.putFloat(LATITUDE, lon.toFloat())
                        editor.putFloat(LONGITUDE, lat.toFloat())
                        Log.e(TAG, "My coordinates: $lat + $lon")
                        editor.apply()


                    }

                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}







    companion object {
        private const val TAG = "Maps"
        private const val rad = 6371.0
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    }

}




