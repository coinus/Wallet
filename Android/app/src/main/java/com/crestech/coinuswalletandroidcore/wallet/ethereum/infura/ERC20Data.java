package com.crestech.coinuswalletandroidcore.wallet.ethereum.infura;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ERC20Data {

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("jsonrpc")
    @Expose
    private String jsonrpc;
    @SerializedName("method")
    @Expose
    private String method;
    @SerializedName("params")
    @Expose
    private Object[] params;
}
