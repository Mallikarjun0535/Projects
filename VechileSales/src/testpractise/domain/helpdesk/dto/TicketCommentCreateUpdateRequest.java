package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
public class TicketCommentCreateUpdateRequest {
    @NotBlank
    String text;
    @NotNull
    Boolean discussion;
    @NotNull
    Boolean internal;
    @NotNull
    Boolean resolution;

    public static TicketCommentCreateUpdateRequest discussionComment(String comment) {
        return new TicketCommentCreateUpdateRequest(comment, true, false, false);
    }

    public static TicketCommentCreateUpdateRequest internalComment(String comment) {
        return new TicketCommentCreateUpdateRequest(comment, false, true, false);
    }

    public static TicketCommentCreateUpdateRequestBuilder baseOn(TicketCommentCreateUpdateRequest request) {
        return builder()
                .text(request.getText())
                .discussion(request.getDiscussion())
                .internal(request.getInternal())
                .resolution(request.getResolution());
    }
}
