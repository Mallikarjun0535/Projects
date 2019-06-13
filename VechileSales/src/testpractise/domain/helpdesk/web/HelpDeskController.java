package com.dizzion.portal.domain.helpdesk.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.helpdesk.ConnectWiseCompanyService;
import com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService;
import com.dizzion.portal.domain.helpdesk.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_TICKETS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_TICKETS;

@RestController
public class HelpDeskController {
    private final ConnectWiseTicketingService ticketingService;
    private final ConnectWiseCompanyService companyService;

    public HelpDeskController(ConnectWiseTicketingService ticketingService, ConnectWiseCompanyService companyService) {
        this.ticketingService = ticketingService;
        this.companyService = companyService;
    }

    @GetMapping("/tickets")
    @Secured(VIEW_TICKETS)
    public Page<ConnectWiseTicket> getTicketsPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return ticketingService.getTicketsPage(filters, pageRequest);
    }

    @GetMapping("/tickets/{id}")
    @Secured(VIEW_TICKETS)
    public TicketDetails getTicketDetails(@PathVariable long id) {
        return ticketingService.getTicketDetails(id);
    }

    @GetMapping("/tickets/user/{id}/contact")
    @Secured(VIEW_TICKETS)
    public Contact getHelpDeskContact(@PathVariable long id) {
        return companyService.getContactOrDefault(id);
    }

    @PostMapping("/tickets/user/{id}/contact")
    @Secured(EDIT_TICKETS)
    public Contact createHelpDeskContact(@PathVariable long id) {
        return companyService.createContact(id);
    }

    @PostMapping("/tickets")
    @Secured(EDIT_TICKETS)
    public ConnectWiseTicket createTicket(@RequestBody @Valid TicketCreateRequest ticket) {
        return ticketingService.createTicket(ticket);
    }

    @PutMapping("/tickets/{id}")
    @Secured(EDIT_TICKETS)
    public ConnectWiseTicket updateTicket(@PathVariable long id, @RequestBody @Valid TicketUpdateRequest ticket) {
        return ticketingService.updateTicket(id, ticket);
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @Secured(EDIT_TICKETS)
    public TicketComment createTicketComment(@PathVariable long ticketId, @RequestBody @Valid TicketCommentCreateUpdateRequest comment) {
        return ticketingService.createTimeEntryWithAuthor(ticketId, comment);
    }

    @GetMapping("/tickets/attachments/{attachmentId}")
    @Secured(VIEW_TICKETS)
    public byte[] getAttachment(@PathVariable long attachmentId) {
        return ticketingService.downloadDocument(attachmentId);
    }

    @PostMapping("/tickets/{ticketId}/attachments")
    @Secured(EDIT_TICKETS)
    public void uploadAttachment(@PathVariable long ticketId, @RequestParam("file") MultipartFile file) {
        ticketingService.uploadDocument(ticketId, file);
    }

    @GetMapping("/tickets/members")
    @Secured(VIEW_TICKETS)
    public Set<ConnectWiseMember> getMembers(Set<FieldFilter> filters) {
        return companyService.getMembers(filters);
    }
}
