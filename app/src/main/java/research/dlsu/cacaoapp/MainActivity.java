package research.dlsu.cacaoapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_DEVICEID = 1;

    Button buttonTakePicture;
    Button buttonViewAlbums;
    Button buttonAddNewCacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDeviceId();

        buttonTakePicture = (Button) findViewById(R.id.button_take_picture);
        buttonViewAlbums = (Button) findViewById(R.id.button_view_albums);
        buttonAddNewCacao = (Button) findViewById(R.id.button_add_new_cacao);

        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), ChoosePhotoActivity.class));
            }
        });

        buttonViewAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), ViewAlbumsActivity.class));
            }
        });

        buttonAddNewCacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), AddNewCacaoActivity.class));
            }
        });
    }

    public void saveDeviceIdToSharedPreferences(){
        SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
        if(sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null) == null) {
            SharedPreferences.Editor spEditor = sp.edit();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            spEditor.putString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, telephonyManager.getDeviceId());
            spEditor.commit();
        }
    }

    public void getDeviceId(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION_DEVICEID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_DEVICEID && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveDeviceIdToSharedPreferences();
        }else{
            Snackbar.make(buttonAddNewCacao, "We need your permission to enable location services.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View v = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog_ipaddress, null);
        final EditText etIpAddress = (EditText) v.findViewById(R.id.et_ipaddress);
        int menuId = item.getItemId();
        switch(menuId){
            case R.id.ip_address:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(v)
                        .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RemoteServer.IPADDRESS = etIpAddress.getText().toString();
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
