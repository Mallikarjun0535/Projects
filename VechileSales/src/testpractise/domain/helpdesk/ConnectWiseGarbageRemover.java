package com.dizzion.portal.domain.helpdesk;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.helpdesk.dto.TicketUpdateRequest;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static com.dizzion.portal.domain.filter.FieldFilter.LogicOperator.AND;
import static com.dizzion.portal.domain.filter.FieldFilter.Operator.EQUALS;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService.STATUS_FIELD;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService.SUMMARY_FIELD;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Board.NO_ACTION_REQUIRED;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status.CUSTOMER_EXPERIENCE_NEW;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status.NO_ACTION_REQUIRED_CLOSED;

@Component
@ConditionalOnProperty("connectwise.garbage.remover.enabled")
@Slf4j
public class ConnectWiseGarbageRemover {
    private final ConnectWiseTicketingService ticketingService;
    private final AuthenticatedUserAccessor auth;
    private final FieldFilter[] garbageFilters;

    public ConnectWiseGarbageRemover(ConnectWiseTicketingService ticketingService,
                                     AuthenticatedUserAccessor auth,
                                     @Value("${connectwise.garbage.ticket.summary}") String garbageTicketSummary) {
        this.ticketingService = ticketingService;
        this.auth = auth;
        this.garbageFilters = new FieldFilter[]{
                new FieldFilter(SUMMARY_FIELD, AND, EQUALS, garbageTicketSummary),
                new FieldFilter(STATUS_FIELD, AND, EQUALS, CUSTOMER_EXPERIENCE_NEW.name())};
    }

    @Scheduled(cron = "${connectwise.garbage.check.cron}")
    public void removeGarbage() {
        auth.asAdmin(() -> ticketingService.getTickets(garbageFilters)
                .parallelStream()
                .forEach(ticket -> auth.asAdmin(() -> {
                    try {
                        log.info("Closing garbage ticket={}", ticket.getId());
                        closeTicket(ticket.getId());
                    } catch (HttpClientErrorException ex) {
                        log.warn("Failed to close ticket={}. RestClientResponseException: {}; Response body:\n{}", ticket.getId(), ex.getMessage(), ex.getResponseBodyAsString());
                    } catch (Exception ex) {
                        log.warn("Failed to close ticket=" + ticket.getId(), ex);
                    }
                })));
    }

    private void closeTicket(long id) {
        ticketingService.updateTicket(id, TicketUpdateRequest.builder()
                .boardId(NO_ACTION_REQUIRED.getId())
                .statusName(NO_ACTION_REQUIRED_CLOSED)
                .build());
    }
}
