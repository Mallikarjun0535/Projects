package com.dizzion.portal.domain.emergencycommunication.web;

import com.dizzion.portal.domain.emergencycommunication.EmergencyCommunicationService;
import com.dizzion.portal.domain.emergencycommunication.dto.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static com.dizzion.portal.domain.role.Permission.Constants.EMERGENCY_COMMUNICATION;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Secured(EMERGENCY_COMMUNICATION)
public class EmergencyCommunicationController {
    private final EmergencyCommunicationService emergencyCommunicationService;

    public EmergencyCommunicationController(EmergencyCommunicationService emergencyCommunicationService) {
        this.emergencyCommunicationService = emergencyCommunicationService;
    }

    @RequestMapping(path = "/emergency-conversations", method = GET)
    public List<EmergencyConversation> getConversations() {
        return emergencyCommunicationService.getLastConversations();
    }

    @RequestMapping(path = "/emergency-conversations", method = POST)
    public EmergencyConversation createConversation(@RequestBody @Valid EmergencyConversationCreateRequest createRequest) {
        return emergencyCommunicationService.createConversation(createRequest);
    }

    @RequestMapping(path = "/emergency-conversations/{id}", method = PUT)
    public EmergencyConversation updateConversation(@PathVariable long id, @RequestBody @Valid EmergencyConversationUpdateRequest updateRequest) {
        return emergencyCommunicationService.updateConversation(id, updateRequest);
    }

    @RequestMapping(path = "/emergency-conversations/{id}/closed", method = PUT)
    public EmergencyConversation closeConversations(@PathVariable long id) {
        return emergencyCommunicationService.closeConversation(id);
    }

    @RequestMapping(path = "/emergency-conversations/{id}/messages", method = GET)
    public List<EmergencyMessage> getConversationMessages(@PathVariable long id) {
        return emergencyCommunicationService.getConversationMessages(id);
    }

    @RequestMapping(path = "/emergency-conversations/{id}/messages", method = POST)
    public EmergencyMessage sendUpdateMessage(@PathVariable long id, @RequestBody @Valid EmergencyMessageSendRequest sendRequest) {
        return emergencyCommunicationService.sendUpdateMessage(id, sendRequest);
    }
}
