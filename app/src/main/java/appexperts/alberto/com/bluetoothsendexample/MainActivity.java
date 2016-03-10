package appexperts.alberto.com.bluetoothsendexample;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    public Button b1;
    public Button b2;
    public Button b3;
    public Button b4;

    private static final String TAG = "THINBTCLIENT";
    private static final boolean D = true;
    private BluetoothAdapter mBluetoothAdapter = null; //Bluetooth from the device
    private BluetoothSocket btSocket = null; //connection with the server and outputStream, that it is the output of bytes from writing
    private OutputStream outStream = null;

    // Serial Port Profile UUID (RFCOMM 1 (by default) if not in use);
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    // ==> write your mac address of the server, that means,
    // of the bluetooth from the PC <== find it in settings of your phone and about-status-bluethooth address
    private static String address = "F8:E0:79:88:BC:08";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D)
            Log.e(TAG, "+++ ON CREATE +++");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) { //We check if the bluetooth is enabled or not...
            Toast.makeText(this, "Bluetooth no disponible.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(
                    this,
                    "Por favor, conecte el BT del dispositivo y vuelva a ejecutar la aplicación.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (D)
            Log.e(TAG, "+++ DONE IN ON CREATE, GOT LOCAL BT, BLUETOOTH ADAPTER +++");

        b1 = (Button) findViewById(R.id.Button01);
        b2 = (Button) findViewById(R.id.Button02);
        b3 = (Button) findViewById(R.id.Button03);
        b4 = (Button) findViewById(R.id.Button04);

        misEventos misEv = new misEventos(); //events when you click on the buttons
        b1.setOnClickListener(misEv);
        b2.setOnClickListener(misEv);
        b3.setOnClickListener(misEv);
        b4.setOnClickListener(misEv);
    }

    class misEventos implements OnClickListener { //we will define the event of each button clicked

        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v == b1) {
                enviar("A");
            } else if (v == b2) {
                enviar("B");
            } else if (v == b3) {
                enviar("C");
            } else if (v == b4) {
                enviar("D");
            }

        }

    }

    public void enviar(String texto) { //we send each text from the bluetooth to the PC

        byte[] msgBuffer = texto.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.e(TAG, "ON RESUME: Exception during write.", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");
    }

    @Override
    public void onResume() {// we will send to the device the bluetooth MAC that we want to connect
        //we will create the connection with the port COM from the PC and cancel any search that the device would be searching by BT
        super.onResume();

        if (D) {
            Log.e(TAG, "+ ON RESUME +");
            Log.e(TAG, "+ ABOUT TO ATTEMPT CLIENT CONNECT to " + address);
        }

        // cuando nos devuelve el device, conoceremos al servidor por su
        // direccion mac

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Necesitamos dos cosas antes de que se puede conectar correctamente
        // (por problemas de autenticación): una dirección MAC, que ya tenemos
        // definida,
        // y un canal RFCOMM
        //
        // Debido a que los canales RFCOMM (puertos) son limitados en número,
        // Android no permite que se utilicen directamente, sino que tendra que
        // solicitar
        // que se asigne el puerto RFCOMM sobre la base del servicio ID.
        // En nuestro caso, vamos a utilizar SPP Service ID.
        // Esta ID está en formato UUID (GUID para Microsofties).
        // Teniendo en cuenta el UUID, Android se encargará de la asignación por
        // usted.
        // En general, se devolverá RFCOMM 1, pero no siempre, sino que
        // dependera de lo
        // que otros servicios Bluetooth están en uso en su dispositivo Android.

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "ON RESUME: Socket creation failed.", e);
        }
        Log.d(TAG, "socket created");

        // podria ser que estuviese ejecutando 'búsqueda de dispositivos'
        // de en la configuracion
        // Bluetooth de tu móvil, por lo que llamamos a cancelDiscovery ().
        // ya que buscar dispositivos es un proceso muy pesado,
        // y no nos interesa ahora mismo, que intentamos conectar

        mBluetoothAdapter.cancelDiscovery();

        try {
            btSocket.connect();
            Log.e(TAG, "ON RESUME: BT connection established, data transfer link open.");
        } catch (IOException e) {
            Log.e(TAG, "ON RESUME: connect threw IOException.");
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "ON RESUME: Unable to close socket during connection failure", e2);
            }
            return;
        }

        // creamos la conexion de datos para comunicarnos con el servidor
        //create the connection data to communicate with the server
        if (D)
            Log.e(TAG, "+ ABOUT TO SAY SOMETHING TO SERVER +");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
        }

        String message = "Hola, mensaje del cliente al servidor.";
        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer); //write the message into the BT server, we will receive the message into the PC if everythings ok
        } catch (IOException e) {
            Log.e(TAG, "ON RESUME: Exception during write.", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (D)
            Log.e(TAG, "- ON PAUSE -");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "ON PAUSE: Couldn't flush output stream.", e);
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            Log.e(TAG, "ON PAUSE: Unable to close socket.", e2);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }

}
