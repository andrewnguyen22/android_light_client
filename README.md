# Experimental Android Light Client Tutorial
Let me preface by reiterating the experimental nature of this repository. Right now the go-ethereum android library appears to be incomplete or at the very least convoluted. Most likely this 'client' will not be able to access all of the functionality that can be on other platforms (for now).
## Step 1: Setup Geth and Manifest
### The official link for the new releases is https://geth.ethereum.org/downloads/
#### Add `compile 'org.ethereum:geth:1.6.6'` (Or latest version) to your App level gradle file under dependencies
### Manifest:
#### Add the following permissions to (AndroidManifest.xml)
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />

## Step 2: Setup UI XML file
**Important** : Currently the send 'set string' button will have no functionaity: I have not been able to figure out the smart contract interaction step yet
#### Use the following code to setup a (very) basic user interface (activity_main.xml):
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
    <TextView
        android:id="@+id/textBox"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="" />

    <Button
        android:text="Balance"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_alignParentLeft="true"
        android:onClick="getAccountBalanceButton"
        android:id="@+id/button" />
    <Button
        android:text="Send Ether"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_alignParentRight="true"
        android:onClick="sendTransaction"
        android:id="@+id/transaction" />
    <Button
        android:text="SetString"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:onClick="smart_contract_interaction_get_string"
        android:id="@+id/set_string" />
</RelativeLayout>

## Step 3: Setup Rinkeby Genesis File
#### Create a file under your assets folder (may need to create assets folder) called 'rinkeby_genesis.json' 
#### Under that file copy and paste the following rinkeby genesis code:
http://www.mobilefish.com/download/ethereum/rinkeby.json.txt

(You can test with https://jsonlint.com/ JSON Validator)
## Step 4: Setup Node
(Copy Paste these as global variables)

     EthereumClient ec;
     Context ctx;
     TextView textbox;
     Node node;
     KeyStore keyStore;
     NodeConfig conf;
     long nonce;
     final String TAG = "Transaction info";

#### a) Genesis File to String
    textbox = (TextView) findViewById(R.id.textBox);
    InputStream is = this.getAssets().open("rinkeby_genesis.json");
    int size = is.available();
    byte[] buffer = new byte[size];
    is.read(buffer);
    is.close();
    String genesis = new String(buffer, "UTF-8");
#### b) Configure the node 
    conf = Geth.newNodeConfig();
    conf.setEthereumGenesis(genesis);
    conf.setEthereumNetworkID(4);
    node = Geth.newNode(getFilesDir() + "./ethereum", conf); //Folder to store the node
    node.start(); // Start the node
#### c) Print the node information to the screen 
    NodeInfo info = node.getNodeInfo();
    textbox.append("My name: " + info.getName() + "\n");
    textbox.append("My address: " + info.getListenerAddress() + "\n");
    textbox.append("My protocols: " + info.getProtocols() + "\n\n");
## Setup 5: Setup Accounts
    keyStore = new KeyStore(this.getFilesDir() + "/keystore",
        Geth.StandardScryptN, Geth.StandardScryptP);
    if (keyStore.getAccounts().size() == 0) {
        Account acc = keyStore.newAccount("Password");
        keyStore.getAccounts().set(0, acc);
        acc = keyStore.newAccount("Password");
        keyStore.getAccounts().set(1, acc);
    }
  
    textbox.append("Sender Address: " + keyStore.getAccounts().get(0).getAddress().getHex().toString() + "\n");
    textbox.append("Receiver Address: " + keyStore.getAccounts().get(1).getAddress().getHex().toString() + "\n");

## Step 6: Get Balance Function
     public void getAccountBalanceButton(View view) {
          try {
                 ec = node.getEthereumClient();
                 System.out.println("Sender: " + ec.getBalanceAt(ctx, keyStore.getAccounts().get(0).getAddress(), -1).toString() + "\n");
                 System.out.println("Receiver: " + ec.getBalanceAt(ctx, 
                 keyStore.getAccounts().get(1).getAddress(), -1).toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
## Step 7: Get test ether from Rinkeby
#### a) Go to https://www.rinkeby.io/
#### b) Setup a Gist via Github and paste your account address there (seen on UI)
#### c) Request 3 Eth (Should not take too long to receive) 
#### d) You can check your accounts

## Step 8: Setup transaction function

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

This is as far as I have gotten, if you understand how to interact with Smart Contracts on the blockchain please answer my question below:
Current Issue: https://github.com/ethereum/go-ethereum/issues/14832

