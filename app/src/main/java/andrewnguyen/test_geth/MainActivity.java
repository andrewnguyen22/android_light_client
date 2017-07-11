package andrewnguyen.test_geth;

import android.accounts.AccountManager;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import org.ethereum.geth.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    Account newAcc;
    EthereumClient ec;
    Context ctx;
    TextView textbox;
    Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Android In-Process Node");
        textbox = (TextView) findViewById(R.id.textBox);
        try {
            InputStream is = this.getAssets().open("ropsten_genesis.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String genesis = new String(buffer, "UTF-8");
            NodeConfig conf = Geth.newNodeConfig();
            conf.setEthereumGenesis(genesis);
            conf.setEthereumNetworkID(4);
            SessionIdentifierGenerator sessionIdentifierGenerator = new SessionIdentifierGenerator();
            node = Geth.newNode(getFilesDir() + sessionIdentifierGenerator.nextSessionId(), conf);
            node.start();
            ctx = Geth.newContext();

            NodeInfo info = node.getNodeInfo();
            textbox.append("My name: " + info.getName() + "\n");
            textbox.append("My address: " + info.getListenerAddress() + "\n");
            textbox.append("My protocols: " + info.getProtocols() + "\n\n");
            KeyStore keyStore = new KeyStore(this.getFilesDir() + "/keystore",
                    Geth.StandardScryptN, Geth.StandardScryptP);
            newAcc = keyStore.newAccount("password");
            textbox.append("My Account Address: " + newAcc.getAddress().getHex().toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getAccountBalanceButton(View view){
        System.out.println("My Account Address: " + newAcc.getAddress().getHex().toString() + "\n");
        try {
            ec = node.getEthereumClient();
            System.out.println("My Account Balance: " + ec.getBalanceAt(ctx, newAcc.getAddress(),-1) + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}