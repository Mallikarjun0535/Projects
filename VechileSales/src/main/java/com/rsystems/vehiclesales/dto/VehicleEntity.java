package com.rsystems.vehiclesales.dto;

import java.io.Serializable;
import java.util.Date;

public class VehicleEntity implements Serializable {

    private Integer vehicleId;
    private String vehicleUID;
    private String vehicleName;
    private String vendor;
    private String vehicleStatus;
    private String status;
    private Integer createBy;
    private Integer updateBy;
    private Date createDate;
    private Date updateDate;

    public Integer getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Integer createBy) {
        this.createBy = createBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleUID() {
        return vehicleUID;
    }

    public void setVehicleUID(String vehicleUID) {
        this.vehicleUID = vehicleUID;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(String vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Integer updateBy) {
        this.updateBy = updateBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "VehicleEntity [vehicleId=" + vehicleId + ", vehicleUID=" + vehicleUID + ", vehicleName=" + vehicleName
                + ", vendor=" + vendor + ", vehicleStatus=" + vehicleStatus + ", status=" + status + ", createBy="
                + createBy + ", updateBy=" + updateBy + ", createDate=" + createDate + ", updateDate=" + updateDate
                + "]";
    }
}
