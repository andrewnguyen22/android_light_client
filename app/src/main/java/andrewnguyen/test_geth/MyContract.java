
// This file is an automatically generated Java binding. Do not modify as any
// change will likely be lost upon the next re-generation!

package andrewnguyen.test_geth;

import org.ethereum.geth.Address;
import org.ethereum.geth.BoundContract;
import org.ethereum.geth.CallOpts;
import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Interface;
import org.ethereum.geth.Interfaces;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.TransactOpts;
import org.ethereum.geth.Transaction;

//abigen --abi token.abi --pkg main --lang java --out MyContract.java
public class MyContract {
    // ABI is the input ABI used to generate the binding from.
    public final static String ABI = "[{\"constant\":false,\"inputs\":[],\"name\":\"get_s\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"new_s\",\"type\":\"string\"}],\"name\":\"set_s\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"inputs\":[{\"name\":\"d_s\",\"type\":\"string\"}],\"payable\":false,\"type\":\"constructor\"}]";

    //Address String
    public final String address_string = "0x8607e627604495ae9812c22bb1c98bdcba581978";
    // Ethereum address where this contract is located at.
    public Address address;

    // Ethereum transaction in which this contract was deployed (if known!).
    public Transaction deployer;

    // Contract instance bound to a blockchain address.
    private BoundContract Contract;

    //Ethereum Client
    private EthereumClient ec;

    // Creates a new instance of Main, bound to a specific deployed contract.
    public MyContract(EthereumClient client){
        try {
            address = Geth.newAddressFromHex(address_string);
            Contract = Geth.bindContract(address, ABI, client);
            ec = client;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // get_s is a paid mutator transaction binding the contract method 0x75d74f39.
    // Solidity: function get_s() returns(string)
    public String get_s(Context ctx) {
        String string = null;
        try {
            CallOpts opts = Geth.newCallOpts();
            opts.setContext(ctx);
            opts.setGasLimit(31500);
            Interfaces args = Geth.newInterfaces(0);
            Interfaces results = Geth.newInterfaces(1);
            Interface result = Geth.newInterface();
            result.setDefaultString();
            results.set(0, result);
            this.Contract.call(opts, results, "get_s", args);//TODO function throws exception - > 'abi: cannot unmarshal string in to []interface {}'
            string = results.get(0).getString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    // set_s is a paid mutator transaction binding the contract method 0xe7aab290.
    // Solidity: function set_s(new_s string) returns()
    public void set_s(KeyStore keyStore, Context ctx, String new_s) {
        try {
            TransactOpts opts = new TransactOpts();
            opts.setFrom(keyStore.getAccounts().get(0).getAddress());
            opts.setContext(ctx);
            Interfaces args = Geth.newInterfaces(1);
            args.set(0, Geth.newInterface());
            args.get(0).setString(new_s);
            Transaction transaction = this.Contract.transact(opts, "set_s", args);//TODO function throws exception - > 'abi: cannot use slice as type string as argument'
            ec.sendTransaction(ctx, transaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

