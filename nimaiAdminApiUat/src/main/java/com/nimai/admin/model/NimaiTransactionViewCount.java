package com.nimai.admin.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="nimai_trnx_view_count")
public class NimaiTransactionViewCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column(name="USER_ID")
    private String userId;

    @Column(name="before_accepted")
    private Integer beforeAccepted;

    @Column(name="after_accepted")
    private Integer afterAccepted;

    @Column(name="accepted_flag")
    private Integer acceptedFlag;

    @Column(name="insert_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date insertDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getBeforeAccepted() {
        return beforeAccepted;
    }

    public void setBeforeAccepted(Integer beforeAccepted) {
        this.beforeAccepted = beforeAccepted;
    }

    public Integer getAfterAccepted() {
        return afterAccepted;
    }

    public void setAfterAccepted(Integer afterAccepted) {
        this.afterAccepted = afterAccepted;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public Integer getAcceptedFlag() {
        return acceptedFlag;
    }

    public void setAcceptedFlag(Integer acceptedFlag) {
        this.acceptedFlag = acceptedFlag;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }
}
