package com.rsystems.VechileSales;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsystems.vehiclesales.command.Vehicles;
import com.rsystems.vehiclesales.controller.VehiclesDemoController;
import com.rsystems.vehiclesales.dao.VehiclesDemoDaoI;
import com.rsystems.vehiclesales.exceptions.ErrorCodes;
import com.rsystems.vehiclesales.exceptions.WebappException;
import com.rsystems.vehiclesales.services.VehiclesDemoServiceI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/*@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("Test")*/
@RunWith(MockitoJUnitRunner.class)
public class VehicleDemoApplicationTest {

    @InjectMocks
    VehiclesDemoController vehiclesDemoController;
    @Mock
    VehiclesDemoServiceI vechiclesDemoService;
    @Mock
    VehiclesDemoDaoI vehicleDemoRepository;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(vehiclesDemoController).build();
    }


    //@Test
    public void getVehiclesListTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String jsString = "[\r\n" + "    {\r\n" + "        \"vid\": null,\r\n" + "        \"clientID\": null,\r\n"
                + "        \"vUniqueId\": \"H001\",\r\n" + "        \"vname\": \"Brio\",\r\n"
                + "        \"vStatus\": \"PURCHASED\",\r\n" + "        \"vendor\": \"Honda\",\r\n"
                + "        \"status\": \"A\",\r\n" + "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n"
                + "        \"updateDate\": null\r\n" + "    },\r\n" + "    {\r\n" + "        \"vid\": null,\r\n"
                + "        \"clientID\": null,\r\n" + "        \"vUniqueId\": \"H002\",\r\n"
                + "        \"vname\": \"City\",\r\n" + "        \"vStatus\": \"PURCHASED\",\r\n"
                + "        \"vendor\": \"Honda\",\r\n" + "        \"status\": \"A\",\r\n"
                + "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n" + "        \"updateDate\": null\r\n"
                + "    },\r\n" + "    {\r\n" + "        \"vid\": null,\r\n" + "        \"clientID\": null,\r\n"
                + "        \"vUniqueId\": \"H003\",\r\n" + "        \"vname\": \"BRV\",\r\n"
                + "        \"vStatus\": \"OPEN\",\r\n" + "        \"vendor\": \"Honda\",\r\n"
                + "        \"status\": \"A\",\r\n" + "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n"
                + "        \"updateDate\": null\r\n" + "    },\r\n" + "    {\r\n" + "        \"vid\": null,\r\n"
                + "        \"clientID\": null,\r\n" + "        \"vUniqueId\": \"H004\",\r\n"
                + "        \"vname\": \"CR-V\",\r\n" + "        \"vStatus\": \"LOCK\",\r\n"
                + "        \"vendor\": \"Honda\",\r\n" + "        \"status\": \"A\",\r\n"
                + "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n" + "        \"updateDate\": null\r\n"
                + "    },\r\n" + "    {\r\n" + "        \"vid\": null,\r\n" + "        \"clientID\": null,\r\n"
                + "        \"vUniqueId\": \"H005\",\r\n" + "        \"vname\": \"Accord\",\r\n"
                + "        \"vStatus\": \"LOCK\",\r\n" + "        \"vendor\": \"Honda\",\r\n"
                + "        \"status\": \"A\",\r\n" + "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n"
                + "        \"updateDate\": null\r\n" + "    },\r\n" + "    {\r\n" + "        \"vid\": null,\r\n"
                + "        \"clientID\": null,\r\n" + "        \"vUniqueId\": \"H006\",\r\n"
                + "        \"vname\": \"Amaze\",\r\n" + "        \"vStatus\": \"OPEN\",\r\n"
                + "        \"vendor\": \"Honda\",\r\n" + "        \"status\": \"A\",\r\n"
                + "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n" + "        \"updateDate\": null\r\n"
                + "    }\r\n" + "]";

        List<Vehicles> vehiclesList = mapper.readValue(jsString, new TypeReference<List<Vehicles>>() {
        });

        Mockito.when(vechiclesDemoService.getDemovechiclesList("honda")).thenReturn(vehiclesList);
        mockMvc.perform(MockMvcRequestBuilders.get("/Vehicles/list/honda"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].vUniqueId", is("H001")));
    }

    @Test
    public void updateVehicleForLock() throws Exception {
        WebappException e = new WebappException(ErrorCodes.WARN_CODE, ErrorCodes.EXISTING_LOCK_MESSAGE);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "    {\r\n" +
                "        \"vid\": null,\r\n" +
                "        \"clientID\": null,\r\n" +
                "        \"vUniqueId\": \"H004\",\r\n" +
                "        \"vname\": \"CR-V\",\r\n" +
                "        \"vStatus\": \"LOCK\",\r\n" +
                "        \"vendor\": \"Honda\",\r\n" +
                "        \"status\": \"A\",\r\n" +
                "        \"createDate\": \"2018-04-23T07:20:01.000+0000\",\r\n" +
                "        \"updateDate\": null\r\n" +
                "    }";


        //ErrorRespose er = new ErrorRespose(new Date(), "VEHICLE_IS_ALREADY_LOCKED_BY_OTHER_USER_100002", "");
        Vehicles vehicle = mapper.readValue(jsonString, Vehicles.class);

        //Mockito.when(vechiclesDemoService.lockForVehicle(vehicle),Mockito.any(Vehicles.class)).thenReturn(eresponse);
        Mockito.when(vechiclesDemoService.lockForVehicle(vehicle));
        mockMvc.perform(MockMvcRequestBuilders.put("/Vehicles/lockVehicle"))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());
        //.andExpect(MockMvcResultMatchers.jsonPath("$",is("VEHICLE_IS_ALREADY_LOCKED_BY_OTHER_USER_100002")));
    }

}
