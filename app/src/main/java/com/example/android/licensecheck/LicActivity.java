package com.example.android.licensecheck;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

/**
 * Created by Arvind on 14/12/17.
 */

public class LicActivity extends AppCompatActivity {
    private static final String BASE64_PUBLIC_KEY = "REPLACE THIS WITH YOUR PUBLIC KEY";

    // Generate your own 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[]{
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64,
            89
    };
    boolean licensed;
    boolean checkingLicense;
    boolean didCheck;
    private Handler mHandler;
    private LicenseChecker mChecker;
    private LicenseCheckerCallback mLicenseCheckerCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Log.i("Device Id: ", deviceId);  //AN EXAMPLE OF LOGGING THAT YOU SHOULD BE
        // DOING :)
        Log.v("LicActivity", "device id is PLASRESJS" + deviceId);
        mHandler = new Handler();
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mChecker = new LicenseChecker(this, new ServerManagedPolicy(this, new AESObfuscator(SALT, getPackageName(),
                deviceId)), BASE64_PUBLIC_KEY);
        Button licCheck = (Button) findViewById(R.id.check_license_button);
        licCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Checking the license", Toast.LENGTH_SHORT).show();

                //doCheck();

            }
        });
    }


    private void doCheck() {

        didCheck = false;
        checkingLicense = true;
        setProgressBarIndeterminateVisibility(true);

        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    protected Dialog onCreateDialog(int id) {
        // We have only one dialog.
        return new AlertDialog.Builder(this)
                .setTitle("UNLICENSED APPLICATION DIALOG TITLE")
                .setMessage("This application is not licensed, please buy it from the play store.")
                .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);
                        finish();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("Re-Check", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doCheck();
                    }
                })

                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        Log.i("License", "Key Listener");
                        finish();
                        return true;
                    }
                })
                .create();

    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.i("LicActivity", "Accepted!");

            //You can do other things here, like saving the licensed status to a
            //SharedPreference so the app only has to check the license once.

            licensed = true;
            checkingLicense = false;
            didCheck = true;

        }

        @SuppressWarnings("deprecation")
        @Override
        public void dontAllow(int reason) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.i("License", "Denied!");
            Log.i("License", "Reason for denial: " + reason);

            //You can do other things here, like saving the licensed status to a
            //SharedPreference so the app only has to check the license once.

            licensed = false;
            checkingLicense = false;
            didCheck = true;

            showDialog(0);

        }

        @SuppressWarnings("deprecation")
        @Override
        public void applicationError(int reason) {
            // TODO Auto-generated method stub
            Log.i("License", "Error: " + reason);
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            licensed = true;
            checkingLicense = false;
            didCheck = false;

            Log.i("LicActivity", "App is not licensed please buy from store");
            showDialog(0);
        }


    }
}

