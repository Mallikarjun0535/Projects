package com.vehicle.dao;

import org.apache.ibatis.annotations.*;
import org.mybatis.spring.annotation.MapperScan;

import com.vehicle.domain.VehiclesEntity;

import java.util.List;

@MapperScan
public interface VehiclesDemoDaoI {

    @Select("Select * from vehicles where vendor  = #{vendor}")
    @Results({@Result(property = "vehicleId", column = "VEHICLE_ID"),
            @Result(property = "vehicleUID", column = "VUNIQUE_ID"),
            @Result(property = "vehicleName", column = "VEHICLE_NAME"),
            @Result(property = "vendor", column = "VENDOR"),
            @Result(property = "vehicleStatus", column = "VEHICLE_STATUS"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "createBy", column = "Create_by"),
            @Result(property = "updateBy", column = "Update_by"),
            @Result(property = "createDate", column = "CREATE_DATE"),
            @Result(property = "updateDate", column = "UPDATE_DATE"),})
    List<VehiclesEntity> getVehiclesList(String vendor);

    @Select("Select * from vehicles where VEHICLE_STATUS = #{vstatus}")
    @Results({@Result(property = "vehicleId", column = "VEHICLE_ID"),
            @Result(property = "vehicleUID", column = "VUNIQUE_ID"),
            @Result(property = "vehicleName", column = "VEHICLE_NAME"),
            @Result(property = "vendor", column = "VENDOR"),
            @Result(property = "vehicleStatus", column = "VEHICLE_STATUS"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "createBy", column = "Create_by"),
            @Result(property = "updateBy", column = "Update_by"),
            @Result(property = "createDate", column = "CREATE_DATE"),
            @Result(property = "updateDate", column = "UPDATE_DATE"),})
    List<VehiclesEntity> getlockedVehiclesList(String lock);

    @Update("Update vehicles set VEHICLE_STATUS = #{vStatus} , Update_by = #{clientID} where  VUNIQUE_ID = #{vUniqueId}")
        //@SelectKey(before = true, keyProperty = "vehicleName", resultType = String.class, statement = { "select VEHICLE_NAME  from vehicles where VUNIQUE_ID = #{vUniqueId} " })
        //@Select ({ "select VEHICLE_NAME  from vehicles where VUNIQUE_ID = #{vUniqueId} " })
    int updateVehicleforLock(@Param(value = "vStatus") String vStatus, @Param(value = "clientID") Integer clientID, @Param(value = "vUniqueId") String vUniqueId);


    @Update("Update vehicles set VEHICLE_STATUS = #{vStatus} , Update_by = #{clientID} where  VUNIQUE_ID = #{vUniqueId}")
        //@SelectKey(before = true, keyProperty = "vehicleName", resultType = String.class, statement = { "select VEHICLE_NAME  from vehicles where VUNIQUE_ID = #{vUniqueId} " })
    int updateVehicleforPurchase(@Param(value = "vStatus") String vStatus, @Param(value = "clientID") Integer clientID, @Param(value = "vUniqueId") String vUniqueId);


    @Update("Update vehicles set VEHICLE_STATUS = #{vStatus} , Update_by = #{clientID} where  VUNIQUE_ID = #{vUniqueId}")
        //@SelectKey(before = true, keyProperty = "vehicleName", resultType = String.class, statement = { "select VEHICLE_NAME  from vehicles where VUNIQUE_ID = #{vUniqueId} " })
    int updateVehicleToReleaseLock(@Param(value = "vStatus") String vStatus, @Param(value = "clientID") Integer clientID, @Param(value = "vUniqueId") String vUniqueId);


}
