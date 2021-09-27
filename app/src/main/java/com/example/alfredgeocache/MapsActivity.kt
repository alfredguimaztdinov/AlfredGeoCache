package com.example.alfredgeocache

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
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








//    override fun onSaveInstanceState(outState: Bundle) {
//        mMap?.let { map ->
//            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
//            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
//        }
//        super.onSaveInstanceState(outState)
//    }






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
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                    lastKnownLocation!!.latitude,
                                                    lastKnownLocation!!.longitude
                                            ), DEFAULT_ZOOM.toFloat()
                                    )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(
                                CameraUpdateFactory
                                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
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
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
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
            val lon = sharedPreferencess.getFloat(this.LONGITUDE, 20F)
            val lat = sharedPreferencess.getFloat(this.LATITUDE, 20F)


            val resumedPosition = LatLng(lon.toDouble(),lat.toDouble())
            Log.e(TAG,"My coordinates: $resumedPosition")

            mMap.addMarker(MarkerOptions().position(resumedPosition).draggable(false))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(resumedPosition))


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
                        mMap.clear();
                        val marker = mMap.addMarker(
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
                        editor.apply()
                        Log.e(TAG,"My coordinates: $lon + $lat")

                    }

                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}







    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

}




