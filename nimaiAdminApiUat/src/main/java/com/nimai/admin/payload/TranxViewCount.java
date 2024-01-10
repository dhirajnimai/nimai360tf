package com.nimai.admin.payload;

public class TranxViewCount {

    String userId;
    int count;
//    Date insertedDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
