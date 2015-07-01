package com.example.btardutest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainBTActivity extends Activity {
	public static final String TAG = "BT_TEST";

	private Button btnDiscover;
    private Button btnConnect;
    private Button btnDisconnect;


    private BluetoothAdapter btAdapter = null;
    BluetoothGatt mBluetoothGatt;
    public final static UUID UUID_HM10_SERIAL =
            UUID.fromString(SampleGattAttributes.HM_10_CONF);
    public final static UUID UUID_HM_RX_TX =  UUID.fromString(SampleGattAttributes.HM_RX_TX);

    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;


    //private static final String HC06MAC = "20:14:08:14:06:50";
	//private static final String SPP_UUID = "00001101-0000-1000-8000-00805f9b34fb";
	//private static final UUID MY_UUID = UUID.fromString(SPP_UUID);
	//private BluetoothSocket btSocket;
	//private ConnectedThread mConnectedThread;
	final int RECIEVE_MESSAGE = 1;
	private Button btnSend;
	private Button btnRead;
	private SeekBar scrlRed, scrlGreen, scrlBlue;

    BluetoothDevice mDevice;
	
	/*private Handler h= new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case RECIEVE_MESSAGE:                                                   // if receive massage
                byte[] readBuf = (byte[]) msg.obj;
                String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                String val=txtOut.getText().toString();
                val+=strIncom;
                txtOut.setText(val);
                //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                break;
            }
        };
    };*/

    private BluetoothGattCallback mBluetoothGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                mBluetoothGatt.discoverServices());

                updateBtnEnabled(false, true);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                updateBtnEnabled(true,false);
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService serialSerivice= gatt.getService(UUID_HM10_SERIAL);
                if(serialSerivice!=null) {
                    characteristicTX = serialSerivice.getCharacteristic(UUID_HM_RX_TX);
                    characteristicRX = serialSerivice.getCharacteristic(UUID_HM_RX_TX);
                    Log.w(TAG, "Connection to serial service OK");
                }else{
                    Log.w(TAG, "onServicesDiscovered Not an HM10");
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (UUID_HM10_SERIAL.equals(characteristic.getUuid())) {
                    int flag = characteristic.getProperties();
                    int format = -1;
                    if ((flag & 0x01) != 0) {
                        format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        Log.d(TAG, "Heart rate format UINT16.");
                    } else {
                        format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        Log.d(TAG, "Heart rate format UINT8.");
                    }
                    final int heartRate = characteristic.getIntValue(format, 1);
                    Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                } else {
                    // For all other profiles, writes the data formatted in HEX.
                    final byte[] data = characteristic.getValue();
                    if (data != null && data.length > 0) {
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for(byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        Log.d(TAG, String.format("Received Other: %s", stringBuilder));
                    }
                }
            }else {
                Log.w(TAG, "onCharacteristicRead received: " + status);
            }
        }
    };

    void updateBtnEnabled(final boolean connect, final boolean disconect){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnConnect.setEnabled(connect);
                btnDisconnect.setEnabled(disconect);
            }
        });
    }

	private LeScanCallback leCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Toast.makeText(MainBTActivity.this, "Found: " + device.getName(),
					Toast.LENGTH_LONG).show();
            mDevice=device;
            btAdapter.stopLeScan(leCallback);
            btnConnect.setEnabled(true);
           // mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mBluetoothGattCallback);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_bt);
		btnDiscover = (Button) findViewById(R.id.btnDiscover);

		btAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!btAdapter.isEnabled()) {
			// Intent enableBtIntent = new
			// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		btnDiscover.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				btAdapter.startLeScan(leCallback);
			}
		});

        btnConnect=(Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mBluetoothGatt = mDevice.connectGatt(getApplicationContext(), false, mBluetoothGattCallback);
            }
        });
        btnDisconnect=(Button)findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mBluetoothGatt.disconnect();
            }
        });


		btnSend=(Button)findViewById(R.id.btnSend);		
		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                if(characteristicTX!=null){
                    characteristicTX.setValue("Hi From Android".getBytes());
                    mBluetoothGatt.writeCharacteristic(characteristicTX);
                }
			}
		});
		
		btnRead = (Button)findViewById(R.id.btnRead);
		btnRead.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			}
		});
		
		scrlRed=(SeekBar)findViewById(R.id.scrlRed);
		scrlGreen=(SeekBar)findViewById(R.id.scrlGreen);
		scrlBlue=(SeekBar)findViewById(R.id.scrlBlue);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		finish();
	}

}
