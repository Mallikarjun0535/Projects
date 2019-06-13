package com.rsystems.vehiclesales.command;

import java.util.Date;

public class Vehicles {


    private String vid;
    private Integer clientID;
    private String vUniqueId;
    private String vname;
    private String vStatus;
    private String vendor;
    private String status;
    private Date createDate;
    private Date updateDate;

    public Integer getClientID() {
        return clientID;
    }

    public void setClientID(Integer clientID) {
        this.clientID = clientID;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getvUniqueId() {
        return vUniqueId;
    }

    public void setvUniqueId(String vUniqueId) {
        this.vUniqueId = vUniqueId;
    }

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public String getvStatus() {
        return vStatus;
    }

    public void setvStatus(String vStatus) {
        this.vStatus = vStatus;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "Vehicles [vid=" + vid + ", clientID=" + clientID + ", vUniqueId=" + vUniqueId + ", vname=" + vname
                + ", vStatus=" + vStatus + ", vendor=" + vendor + ", status=" + status + ", createDate=" + createDate
                + ", updateDate=" + updateDate + "]";
    }


}
