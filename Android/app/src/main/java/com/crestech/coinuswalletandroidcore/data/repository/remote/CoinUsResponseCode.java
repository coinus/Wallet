package com.crestech.coinuswalletandroidcore.data.repository.remote;

public enum CoinUsResponseCode {
    INSPECTION(100, "Continue"),
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    CODE_NOT_EXIST(10000, "Response Code Is Not Exist"), // Giwung Customized Code
    CODE_UNKNOWN(10001, "Unknown Response Code"), // Giwung Customized Code
    CODE_PARSING_NO_DATA(10002, "No Data"), // Giwung Customized Code
    DATA_REQUIRED(10003, "Data Need"), // Giwung Customized Code
    NETWORK_UN_CONNECTED(10004, "Internet Connection Error"); // Giwung Customized Code


    private int code;
    private String desc;

    private CoinUsResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String description) {
        this.desc = description;
    }
}
