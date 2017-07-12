package andrewnguyen.test_geth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.*;
import android.view.View;
import android.widget.TextView;
import org.ethereum.geth.*;

import java.io.InputStream;


//Check imports for geth - shows all commands
//https://ethereum.karalabe.com/talks/2016-devcon.html#15
//https://github.com/ethereum/go-ethereum/wiki/Mobile-Clients:-Libraries-and-Inproc-Ethereum-Nodes
//https://github.com/ethereum/go-ethereum/issues/3789
//https://ethereum.stackexchange.com/questions/12924/how-to-do-a-testnet-transaction-on-android-with-geth-1-5-9

public class MainActivity extends AppCompatActivity {
    Account newAcc;
    EthereumClient ec;
    Context ctx;
    TextView textbox;
    Node node;
    KeyStore keyStore;
    NodeConfig conf;
    long nonce;
    final String TAG = "Transaction info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Android In-Process Node");
        textbox = (TextView) findViewById(R.id.textBox);
        try {
            //Read the genesis file to a string
            InputStream is = this.getAssets().open("ropsten_genesis.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String genesis = new String(buffer, "UTF-8");
            //configure the node with a genesis file and network id
            conf = Geth.newNodeConfig();
            conf.setEthereumGenesis(genesis);
            conf.setEthereumNetworkID(4);
            //create a folder "ethereum" to store the node
            node = Geth.newNode(getFilesDir() + "./ethereum", conf);
            //start the node
            node.start();
            ctx = new Context();
            //Print the information from the node on the UI
            NodeInfo info = node.getNodeInfo();
            textbox.append("My name: " + info.getName() + "\n");
            textbox.append("My address: " + info.getListenerAddress() + "\n");
            textbox.append("My protocols: " + info.getProtocols() + "\n\n");
            //Create an account using "Keystore"
            keyStore = new KeyStore(this.getFilesDir() + "/keyl;kasdf;ljkasdf",
                    Geth.StandardScryptN, Geth.StandardScryptP);
            if(keyStore.getAccounts().size()==0) {
                Account acc = keyStore.newAccount("Password");
                keyStore.getAccounts().set(0, acc);
                acc = keyStore.newAccount("Password");
                keyStore.getAccounts().set(1,acc);
            }
            //print account address
            textbox.append("Sender Address: " + keyStore.getAccounts().get(0).getAddress().getHex().toString() + "\n");
            textbox.append("Receiver Address: " + keyStore.getAccounts().get(1).getAddress().getHex().toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getAccountBalanceButton(View view){
        try {
            ec = node.getEthereumClient();
            System.out.println("My Account 1 Balance: " + ec.getBalanceAt(ctx, keyStore.getAccounts().get(0).getAddress(), -1).toString() + "\n");
            System.out.println("My Account 2 Balance: " + ec.getBalanceAt(ctx, keyStore.getAccounts().get(1).getAddress(), -1).toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
