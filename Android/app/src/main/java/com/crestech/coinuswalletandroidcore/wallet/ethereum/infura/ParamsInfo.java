package com.crestech.coinuswalletandroidcore.wallet.ethereum.infura;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ParamsInfo {

    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("data")
    @Expose
    private String data;

}
