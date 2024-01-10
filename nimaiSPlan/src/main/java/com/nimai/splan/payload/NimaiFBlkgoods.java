package com.nimai.splan.payload;

import java.util.Date;

public class NimaiFBlkgoods {



  
    private Long goodsId;

    private String goodsName;

    private Date insertedDate;

    private Date modifiedDate;

    private int goodsMid;


    public NimaiFBlkgoods() {
    }

    public NimaiFBlkgoods(Long goodsId) {
        this.goodsId = goodsId;
    }

    public NimaiFBlkgoods(Long goodsId, int goodsMid) {
        this.goodsId = goodsId;
        this.goodsMid = goodsMid;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Date getInsertedDate() {
        return insertedDate;
    }

    public void setInsertedDate(Date insertedDate) {
        this.insertedDate = insertedDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getGoodsMid() {
        return goodsMid;
    }

    public void setGoodsMid(int goodsMid) {
        this.goodsMid = goodsMid;
    }


}
