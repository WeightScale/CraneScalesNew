package com.konst.testwifi;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.ObjectCommand;
import com.konst.scaleslibrary.module.wifi.ClientWiFi;
import com.konst.scaleslibrary.module.wifi.WifiBaseManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private ClientWiFi clientWiFi;
    WifiBaseManager wifiBaseManager;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PORT = 1011;

    TextView response;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear, buttonGetVRS;
    InetSocketAddress inetSocketAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonClear = (Button) findViewById(R.id.clearButton);
        buttonGetVRS = (Button) findViewById(R.id.buttonGetVRS);
        response = (TextView) findViewById(R.id.responseTextView);
        Commands.setInterfaceCommand(new InterfaceModule() {
            @Override
            public void write(String command) {
                clientWiFi.write(command);
            }

            @Override
            public ObjectCommand sendCommand(Commands commands) {
                try {
                    return clientWiFi.sendCommand(Commands.VRS);
                }catch (Exception e){
                    return null;
                }
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //clientWiFi = new ClientWiFi(MainActivity.this, editTextAddress.getText().toString(), PORT);
                //clientWiFi = new ClientWiFi(MainActivity.this, inetSocketAddress);
                clientWiFi.restartWorkingThread();
                /*Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), response);
                myClient.execute();*/
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clientWiFi.killWorkingThread();
                response.setText("");
            }
        });

        buttonGetVRS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = Commands.VRS.getParam();
                response.append(str);
            }
        });

        wifiBaseManager = new WifiBaseManager(getApplicationContext(),"SCALES.ESP.36.6.4","12345678", new WifiBaseManager.OnWifiBaseManagerListener() {
            @Override
            public void onConnect(String ssid, InetSocketAddress ipAddress) {
                clientWiFi = new ClientWiFi(MainActivity.this, ipAddress);
                clientWiFi.restartWorkingThread();

                Log.i(TAG,"Соединение с сетью " + ssid);
            }

            @Override
            public void onDisconnect() {clientWiFi.killWorkingThread();}
        });

        /*WiFiBaseManager wiFiBaseManager = new WiFiBaseManager(this);
        try {
            inetSocketAddress = wiFiBaseManager.getInetAddressServer(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiBaseManager.terminate();
    }

    public class Client extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        TextView textResponse;

        Client(String addr, int port, TextView textResponse) {
            dstAddress = addr;
            dstPort = port;
            this.textResponse = textResponse;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                        1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

         /*
          * notice: inputStream.read() will block if no data return
          */
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, e.getMessage());
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }
}
