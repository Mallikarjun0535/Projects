package com.rsystems.vehiclesales.services;

import com.rsystems.vehiclesales.command.Vehicles;
import com.rsystems.vehiclesales.dao.VehiclesDemoDaoI;
import com.rsystems.vehiclesales.dto.VehicleEntity;
import com.rsystems.vehiclesales.exceptions.ErrorCodes;
import com.rsystems.vehiclesales.exceptions.WebappException;
import com.rsystems.vehiclesales.utils.IConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class VehiclesDemoServiceImpl implements VehiclesDemoServiceI {

    @Autowired
    private VehiclesDemoDaoI vehicleDemoRepository;

    /**
     * Rest service to return list of vehicles ids based some search criteria
     * provided by client
     */
    @Override
    public List<Vehicles> getDemovechiclesList(String vendor) {

        List<Vehicles> vechileslist = new ArrayList<>();

        List<VehicleEntity> ventityList = vehicleDemoRepository.getVehiclesList(vendor);

        if (!CollectionUtils.isEmpty(ventityList)) {
            vechileslist = ventityList.stream().map(entity -> {
                Vehicles vehicle = new Vehicles();
                vehicle.setvUniqueId(entity.getVehicleUID());
                vehicle.setVname(entity.getVehicleName());
                vehicle.setVendor(entity.getVendor());
                vehicle.setStatus(entity.getStatus());
                vehicle.setvStatus(entity.getVehicleStatus());
                vehicle.setCreateDate(entity.getCreateDate());
                return vehicle;
            }).collect(Collectors.toList());

            return vechileslist;
        } else {
            return vechileslist;
        }
    }

    /**
     * Client sends request with vehicle id on the server to LOCK it for purchasing,
     * when one vehicle is locked for purchase no other user should be able to
     * access it. An appropriate error shoudl be thrown if it already locked or not
     * available
     *
     * @throws WebappException
     */
    @Override
    public String lockForVehicle(Vehicles vehicle) throws Exception {
        int value;
        String response = "";
        //fetching already  locked vehicles list
        List<VehicleEntity> ventityList = vehicleDemoRepository
                .getlockedVehiclesList(IConstants.Vehilce_Status.LOCK.toString());

        if (!CollectionUtils.isEmpty(ventityList)) {
            VehicleEntity ventity = ventityList.stream()
                    .filter(entity -> entity.getVehicleUID().equals(vehicle.getvUniqueId())).findAny().orElse(null);

            if (null != ventity && !StringUtils.isBlank(ventity.getVehicleUID())) {
                //no other Delar should not lock already locked vehicle
                throw new WebappException(ErrorCodes.WARN_CODE, ErrorCodes.EXISTING_LOCK_MESSAGE);
            }

        }
        // new vehicle for lock
        if (null != vehicle) {
            value = vehicleDemoRepository.updateVehicleforLock(IConstants.Vehilce_Status.LOCK.toString(),
                    vehicle.getClientID(), vehicle.getvUniqueId());

            if (StringUtils.isNotBlank(value + "")) {
                response = IConstants.VEHICLE_LOCKED;
            }
        } else {
            throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.NO_PROPER_DATA);
        }
        return response;
    }

    /**
     * Rest service to purchase locked vehicle, when this is called any vehicle
     * which is already locked shoudl be marked as purchased. Otherwise an
     * appropriate error should be thrown.
     */
    @Override
    public String purchaseVehicle(Vehicles vehicle) throws Exception {
        int value;
        String responce = "";
        List<VehicleEntity> ventityList = vehicleDemoRepository
                .getlockedVehiclesList(IConstants.Vehilce_Status.LOCK.toString());
        if (!CollectionUtils.isEmpty(ventityList)) {

            VehicleEntity ventity = ventityList.stream()
                    .filter(entity -> entity.getVehicleUID()
                            .equals(vehicle.getvUniqueId()))
                    .findAny()
                    .orElse(null);

            if (null != vehicle) {
                if (null != ventity && !StringUtils.isBlank(ventity.getVehicleUID())) {
                    value = vehicleDemoRepository.updateVehicleforPurchase(
                            IConstants.Vehilce_Status.PURCHASED.toString(), vehicle.getClientID(),
                            vehicle.getvUniqueId());
                    if (StringUtils.isNotBlank(value + "")) {
                        responce = IConstants.ORDER_SUCESS;
                    }
                } else {
                    throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.UNABLE_TO_PRUCHASE);
                }
            } else {
                throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.NO_PROPER_DATA);
            }
        } else {
            // no vechiles are in locked state
            throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.UNABLE_TO_PRUCHASE);
        }
        return responce;
    }

    /**
     * Rest service to release locked vehicle if vehicle is already locked else
     * appropriate error shoudl be thrown.
     */

    @Override
    public String releaseLock(Vehicles vehicle) throws Exception {
        int value;
        String responce = "";

        List<VehicleEntity> ventityList = vehicleDemoRepository
                .getlockedVehiclesList(IConstants.Vehilce_Status.LOCK.toString());

        if (!CollectionUtils.isEmpty(ventityList)) {

            VehicleEntity ventity = ventityList.stream()
                    .filter(entity -> entity.getVehicleUID().equals(vehicle.getvUniqueId()))
                    .findAny().orElse(null);

            if (null != vehicle) {
                if (null != ventity && !StringUtils.isBlank(ventity.getVehicleUID())) {
                    value = vehicleDemoRepository.updateVehicleToReleaseLock(IConstants.Vehilce_Status.OPEN.toString(),
                            ventity.getUpdateBy(), vehicle.getvUniqueId());
                    if (StringUtils.isNotBlank(value + "")) {
                        responce = IConstants.UNLOCK_VEHICLE;
                    }
                } else {
                    throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.NO_LOCK_STATE);
                }
            } else {
                throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.NO_PROPER_DATA);
            }
        } else {
            throw new WebappException(ErrorCodes.ERROR_CODE, ErrorCodes.NO_LOCK_STATE);
        }

        return responce;
    }
}
