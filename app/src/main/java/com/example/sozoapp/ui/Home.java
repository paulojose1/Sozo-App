package com.example.sozoapp.ui;


/**
 * This is the main class of the application
 * the bulk code for the app is in here
 * methods for camera, permissions, and location.
**/
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.sozoapp.Fragments.GroupsFragment;
import com.example.sozoapp.Fragments.GuardiansFragment;
import com.example.sozoapp.Fragments.HomeFragment;
import com.example.sozoapp.R;
import com.example.sozoapp.models.CreateUser;
import com.example.sozoapp.models.Groups;
import com.example.sozoapp.models.UserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.sozoapp.util.Constants.ERROR_DIALOG_REQUEST;
import static com.example.sozoapp.util.Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.sozoapp.util.Constants.PERMISSION_REQUEST_ENABLE_GPS;
import static com.example.sozoapp.util.Constants.REQUEST_CAMERA_PERMISSION_RESULT;
import static com.example.sozoapp.util.Constants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT;


public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Home";


    //Firebase widgets
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore mDb;
    private DocumentReference mRootRef;


    //Camera Settings Variables
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private File mVideoFolder;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    //Location Variables
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;


    //User validation Variables
    private CreateUser user;
    private ListenerRegistration mUserEventListener;
    private Set<String> mUsersIds = new HashSet<>();


    // Variables
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private String mCameraId;
    private String mVideoFileName;
    private int mTotalRotation;
    private boolean mIsRecording = false;
    private boolean mLocationPermissionGranted = false;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);


        //init
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        mDb = FirebaseFirestore.getInstance();



        //Record activity
        createVideoFolder();
        mMediaRecorder = new MediaRecorder();
        mTextureView = findViewById(R.id.textureView);

        //location services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        initDrawer();
        updateNavHeader();
        initHomeFragment();
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

                   setupCamera(width, height);
                   connectCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            };

    private void initDrawer(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void getUserDetails(){
        if(mUserLocation == null){
            mUserLocation = new UserLocation();


            DocumentReference userRef = mDb.collection("Users")
                    .document(currentUser.getUid());


            userRef.get().addOnCompleteListener(task -> {

                if (task.isSuccessful() && task.getResult() != null){
                    Log.d(TAG, "onComplete: successfully got user details.");

                    user = new CreateUser(currentUser.getDisplayName(), currentUser.getEmail(),null,null,currentUser.getUid());
                   getLastKnownLocation();
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    private void saveUserLocation (){
        if(mUserLocation != null){
            DocumentReference locationRef = mDb.
                    collection("User Locations")
                    .document(currentUser.getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(task -> {

                if (task.isSuccessful()){
                    Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                            "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                            "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                }
            });
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called");

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful()&& task.getResult() != null){
                Location location = task.getResult();
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
                Log.d(TAG, "onComplete: longitude: " + geoPoint.getLongitude());
                mUserLocation = new UserLocation(user,geoPoint,null);
                mUserLocation.setTimeStamp(null);
                saveUserLocation();
            }
        });
    }

    private boolean checkMapsServices(){
        if(isServicesOK()){
            return isMapsEnabled();
        }
        return false;
    }

    private boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Home.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Home.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;

    }
    
    private boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSION_REQUEST_ENABLE_GPS);
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    private void initHomeFragment(){

        HomeFragment homeFragment = new HomeFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, homeFragment).commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSION_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){



                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    private void getLocationPermission() {

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
           getUserDetails();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                // mRecordImageButton.setImageResource(R.mipmap.record_busy);
                try {
                   createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this,
                        "Permission successfully granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(checkMapsServices()){
            if(mLocationPermissionGranted){

               getUserDetails();

            } else {
                getLocationPermission();
            }
        }
        startBackgroundThread();

        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        
        if (id == R.id.action_create_groups){
            
            createNewGroupDialog();
        }

        if (id == R.id.nav_groups) {
            GroupsFragment groupsFragment = new GroupsFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.container, groupsFragment).commit();
        }



        return super.onOptionsItemSelected(item);
    }

    private void createNewGroupDialog()
    {
        AlertDialog.Builder groupBuilder = new AlertDialog.Builder(Home.this);
        groupBuilder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(Home.this);
        groupNameField.setHint("e.g Family");
        groupBuilder.setView(groupNameField);

        groupBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(Home.this, "Plase type in Group Name", Toast.LENGTH_SHORT).show();

                } else
                {

                    createGroup(groupName);

                }
            }
        });

        groupBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

               dialog.cancel();

            }
        });

        groupBuilder.show();



    }

    private void createGroup(String groupName)
    {
        final Groups groups = new Groups();

        groups.setTitle(groupName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newGroupRef = mDb
                .collection("Groups")
                .document();

        groups.setGroupId(newGroupRef.getId());

        newGroupRef.set(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(Home.this, groupName + " Created successfully!", Toast.LENGTH_SHORT).show();
                }
                else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something Went Wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }


    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home)
        {

            HomeFragment homeFragment = new HomeFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.container, homeFragment).commit();


        }
        else if (id == R.id.nav_groups)
        {
            GroupsFragment groupsFragment = new GroupsFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.container, groupsFragment).commit();

        }

        else if (id == R.id.nav_guardians)
        {
            getSupportActionBar().setTitle("Guardians");
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new GuardiansFragment()).commit();

        }
//
//        else if (id == R.id.nav_places)
//        {
//            getSupportActionBar().setTitle("Places");
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, new PlacesFragment()).commit();
//
//        }
//        else if (id == R.id.nav_settings)
//        {
//            getSupportActionBar().setTitle("Settings");
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
//
//        }
//        else if (id == R.id.nav_files)
//        {
//            getSupportActionBar().setTitle("Files");
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, new FilesFragment()).commit();
//
//        }
//        else if (id == R.id.nav_help)
//        {
//            getSupportActionBar().setTitle("Help");
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, new HelpFragment()).commit();
//
//        }
        else if (id == R.id.nav_signOut)
        {
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(),Login.class);
            startActivity(loginActivity);
            finish();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavHeader()
    {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);


        navUserMail.setText(currentUser.getEmail());
        navUserName.setText(currentUser.getDisplayName());

        //Load glove to get user photo
        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);


    }

    /**
     * Start recording camera once user presses
     * volume up key three times.
     *
     * @param keyCode is the hardware button to be pressed by user
    */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
        {
            Toast.makeText(this, "Show something!", Toast.LENGTH_SHORT).show();

            //if camera is recording it will stop,
            //save data and reset camera.
            if(mIsRecording)
            {
                mIsRecording = false;
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            else
                {
               //Calls method to check for storage permission
               //and get camera ready for new recording
               checkWriteStoragePermission();

                }
        }

        return true;
    }


    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            if(mIsRecording) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                //startPreview();
            }
            // Toast.makeText(getApplicationContext(),
            //         "Camera connection made!", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if(mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupCamera(int viewWidth, int viewHeight) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = viewWidth;
                int rotatedHeight = viewHeight;
                if(swapRotation) {
                    rotatedWidth = viewHeight;
                    rotatedHeight = viewWidth;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private static int sensorToDeviceRotation(CameraCharacteristics c, int deviceOrientation) {
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);
        // Get device orientation in degrees
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new VideoRecorderActivity.CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "camera2VideoImage");
        if(!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkWriteStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;

                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            mIsRecording = true;

            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
        }
    }

    private void setUpMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    private void startRecord() {
        try {
            setUpMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            try {
                                session.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
