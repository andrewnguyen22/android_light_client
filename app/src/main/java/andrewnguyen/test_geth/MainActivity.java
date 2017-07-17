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
//https://stackoverflow.com/questions/43298324/go-ethereum-mobile-android-contract-abi-error

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
            if (keyStore.getAccounts().size() == 0) {
                Account acc = keyStore.newAccount("Password");
                keyStore.getAccounts().set(0, acc);
                acc = keyStore.newAccount("Password");
                keyStore.getAccounts().set(1, acc);
            }
            //print account address
            textbox.append("Sender Address: " + keyStore.getAccounts().get(0).getAddress().getHex().toString() + "\n");
            textbox.append("Receiver Address: " + keyStore.getAccounts().get(1).getAddress().getHex().toString() + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAccountBalanceButton(View view) {
        try {
            ec = node.getEthereumClient();
            System.out.println("Sender: " + ec.getBalanceAt(ctx, keyStore.getAccounts().get(0).getAddress(), -1).toString() + "\n");
            System.out.println("Receiver: " + ec.getBalanceAt(ctx, keyStore.getAccounts().get(1).getAddress(), -1).toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTransaction(View view) {
        try {
            ec = node.getEthereumClient();
            nonce = ec.getPendingNonceAt(ctx, keyStore.getAccounts().get(0).getAddress());
            String data_string = "Test Data for Transaction";
            byte[] data = data_string.getBytes();
            BigInt value = Geth.newBigInt(1000);
            BigInt gasLimit = Geth.newBigInt(31500);
            BigInt gasPrice = Geth.newBigInt(21001000100001L);
            Transaction transaction = Geth.newTransaction(nonce, keyStore.getAccounts().get(1).getAddress(), value, gasLimit, gasPrice, data);
            keyStore.timedUnlock(keyStore.getAccounts().get(0), "Password", 100000000);//probably too high of a timeout
            transaction = keyStore.signTx(keyStore.getAccounts().get(0), transaction, new BigInt(4));//Network ID
            ec.sendTransaction(ctx, transaction);
            log_transaction(transaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void log_transaction(Transaction transaction) {
        try {
            android.util.Log.i(TAG, "Cost: " + transaction.getCost());
            android.util.Log.i(TAG, "GasPrice: " + transaction.getGasPrice());
            android.util.Log.i(TAG, "Gas: " + transaction.getGas());
            android.util.Log.i(TAG, "Nonce: " + transaction.getNonce());
            android.util.Log.i(TAG, "Value: " + transaction.getValue());
            android.util.Log.i(TAG, "Sig-Hash Hex: " + transaction.getSigHash().getHex());
            android.util.Log.i(TAG, "Hash Hex: " + transaction.getHash().getHex());
            android.util.Log.i(TAG, "Data-Length: " + transaction.getData().length);
            android.util.Log.i(TAG, "Receiver: " + transaction.getTo().getHex());
            android.util.Log.i(TAG, "Sender: " + transaction.getFrom(new BigInt(4)).getHex().toString());//Network ID
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void smart_contract_interaction(View view){
        //Smart Contract Interactions
        try{
            final String address_string = "0x8607e627604495ae9812c22bb1c98bdcba581978";
            String abi = "[{\"constant\":false,\"inputs\":[],\"name\":\"get_s\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"new_s\",\"type\":\"string\"}],\"name\":\"set_s\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"d_s\",\"type\":\"string\"}],\"payable\":false,\"type\":\"constructor\"}]";
            Address address = Geth.newAddressFromHex(address_string);
            BoundContract contract = Geth.bindContract(address, abi, ec);
            TransactOpts transactOpts = new TransactOpts();
            transactOpts.setFrom(keyStore.getAccounts().get(0).getAddress());
            transactOpts.setContext(ctx);
            Transaction transaction = setString(contract, transactOpts,"Andrew Set This String");
//            ec.sendTransaction(ctx, transaction);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private Transaction setString(BoundContract contract, TransactOpts opts, String teststring) throws Exception {
        //Sending String to Test Contract
        Interfaces params = Geth.newInterfaces(1);
        params.set(0, Geth.newInterface());
        params.get(0).setString(teststring);
        return contract.transact(opts, "set_s", params);//TODO function throws exception - > 'abi: cannot use slice as type string as argument'
    }
}

