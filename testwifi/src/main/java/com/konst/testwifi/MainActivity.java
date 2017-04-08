package com.konst.testwifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
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

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ClientWiFi clientWiFi;
    WifiBaseManager wifiBaseManager;
    DetectScalesManager detectScalesManager;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PORT = 1011;

    TextView response;
    EditText editTextAddress, editTextPort, timeOutEditText;
    Button buttonConnect, buttonClear, buttonGetVRS, buttonFinedScales;
    InetSocketAddress inetSocketAddressScales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        timeOutEditText = (EditText)findViewById(R.id.timeOutEditText);

        buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonClear = (Button) findViewById(R.id.clearButton);
        buttonGetVRS = (Button) findViewById(R.id.buttonGetVRS);
        buttonFinedScales = (Button)findViewById(R.id.buttonFinedScales);
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

        buttonConnect.setOnClickListener(this);
        buttonClear.setOnClickListener(this);
        buttonGetVRS.setOnClickListener(this);
        buttonFinedScales.setOnClickListener(this);


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

        detectScalesManager = new DetectScalesManager((WifiManager) getSystemService(Context.WIFI_SERVICE), 1010, new DetectScalesManager.OnCallBackDetectScalesManager() {
            @Override
            public void onReceivedIpAddress(final InetSocketAddress address) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        response.setText("Socket address :" + address.toString());
                    }
                });
            }

            @Override
            public void onExceptionFound(final String ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        response.setText(ex);
                    }
                });

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connectButton:
                clientWiFi.restartWorkingThread();
                break;
            case R.id.clearButton:
                clientWiFi.killWorkingThread();
                response.setText("");
                break;
            case R.id.buttonGetVRS:
                String str = Commands.VRS.getParam();
                response.append(str);
                break;
            case R.id.buttonFinedScales:
                detectScalesManager.setTimeout(Integer.valueOf(timeOutEditText.getText().toString()));
                detectScalesManager.startFound();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiBaseManager.terminate();
    }

    public void geIps(){
        String   s_dns1 ;
        String   s_dns2;
        String   s_gateway;
        String   s_ipAddress;
        String   s_leaseDuration;
        String   s_netmask;
        String   s_serverAddress;
        TextView info;
        DhcpInfo d;
        WifiManager wifii;

        wifii = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        d = wifii.getDhcpInfo();

        String connections = "";
        InetAddress host, hostt;
        try {
            //host = InetAddress.getByName(intToIp(d.dns1));
            InetAddress[] ips = InetAddress.getAllByName("192.168.1.1");
            String ii = Formatter.formatIpAddress(wifii.getConnectionInfo().getIpAddress());
            hostt = InetAddress.getByName(InetAddress.getByAddress(BigInteger.valueOf(d.dns1).toByteArray()).getHostAddress());
            host = InetAddress.getByName("192.168.1.1");
            byte[] ip = host.getAddress();
            byte[] ipp = hostt.getAddress();

            int timeOut = Integer.valueOf(timeOutEditText.getText().toString());
            StringBuilder successMsg = new StringBuilder();
            Log.d(TAG, "Start fined scales :");
            Socket mSocket = new Socket();
            try {
                mSocket.connect(inetSocketAddressScales, timeOut);
                Log.d(TAG, "Socket connected :" + inetSocketAddressScales.getHostName() + " timeout :" + timeOut);
                BufferedReader successResult = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                if (successResult.ready())
                    successMsg.append(successResult.readLine());

                //Log.d(TAG, mSocket.getInetAddress().getCanonicalHostName() + inetSocketAddress.getHostName());
                //Log.d(TAG, successMsg.toString());
                mSocket.close();
                return;
            }catch (Exception e){
                mSocket.close();
            }
            for(int i = 1; i <= 254; i++){
                mSocket = new Socket();
                ip[3] = (byte) i;
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByAddress(ip), 1011);
                try {
                    mSocket.connect(inetSocketAddress, timeOut);
                    Log.d(TAG, "Socket connected :" + inetSocketAddress.getHostName() + " timeout :" + timeOut);
                    inetSocketAddressScales = inetSocketAddress;
                    BufferedReader successResult = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    if (successResult.ready())
                        successMsg.append(successResult.readLine());

                    //Log.d(TAG, mSocket.getInetAddress().getCanonicalHostName() + inetSocketAddress.getHostName());
                    //Log.d(TAG, successMsg.toString());
                    mSocket.close();
                }catch (IOException e){
                    mSocket.close();
                }
                //Log.d(TAG, inetSocketAddress.getHostName());

				/*InetAddress address = InetAddress.getByAddress(ip);
				if(address.isReachable(300))
				{
					Log.d(" address", address.getHostAddress());
					connections+= address+"\n";
				}*/
				/*else if(!address.getHostAddress().equals(address.getHostName()))
				{
					System.out.println(address + " machine is known in a DNS lookup");
				}*/

            }
            Log.d(TAG, "ALL");
        }
        catch(UnknownHostException e1)
        {
            e1.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        //System.out.println(connections);
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

    Runnable finedScales = new Runnable() {
        @Override
        public void run() {
            try {
                geIps();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    };

    private class Task extends AsyncTask<Void,Void, Boolean>{

        InetAddress[] inetAddress = null;
        List<String> hostList = new ArrayList<String>();

        @Override
        protected Boolean doInBackground(Void... arg0) {

            try {
                geIps();
                //ListNets listNets = new ListNets();
                //getIPAddress(true);
                //doTest();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {

            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(SimpleJNI.this, android.R.layout.simple_list_item_1,hostList);

            //resultList.setAdapter(adapter);
            //return 1;
        }

        private void doTest() throws UnknownHostException {

            String host = "192.168.1.14";
            inetAddress = InetAddress.getAllByName(host);

            for(int i = 0; i < inetAddress.length; i++){

                hostList.add(inetAddress[i].getClass() + " -\n"
                        + inetAddress[i].getHostName() + "\n"
                        + inetAddress[i].getHostAddress());
            }

        }
    }
}
