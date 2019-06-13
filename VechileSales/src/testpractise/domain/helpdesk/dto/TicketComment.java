package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.helpdesk.ConnectWiseUtils.ExtractionResult;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.Optional;

import static com.dizzion.portal.domain.helpdesk.ConnectWiseUtils.extractAuthor;

@Value
@Builder(toBuilder = true)
public class TicketComment {
    String text;
    String author;
    ZonedDateTime dateCreated;
    boolean discussion;
    boolean internal;
    boolean resolution;

    public static TicketComment from(ConnectWiseTicketNote note) {
        Optional<ExtractionResult> extractedAuthor = extractAuthor(note.getText());
        return builder()
                .text(extractedAuthor.map(ExtractionResult::getRemainingText).orElse(note.getText()))
                .dateCreated(note.getDateCreated())
                .discussion(note.isDetailDescriptionFlag())
                .internal(note.isInternalAnalysisFlag())
                .resolution(note.isResolutionFlag())
                .author(extractedAuthor.map(ExtractionResult::getAuthor)
                        .orElse(note.getMember().map(NamedReference::getName)
                                .orElse(note.getContact().map(NamedReference::getName)
                                        .orElse(note.getCreatedBy()))))
                .build();

    }

    public static TicketComment from(ConnectWiseTimeEntry entry) {
        Optional<ExtractionResult> extractedAuthor = extractAuthor(entry.getNotes());
        return builder()
                .text(extractedAuthor.map(ExtractionResult::getRemainingText).orElse(entry.getNotes()))
                .dateCreated(entry.getDateEntered())
                .discussion(entry.isAddToDetailDescriptionFlag())
                .internal(entry.isAddToInternalAnalysisFlag())
                .resolution(entry.isAddToResolutionFlag())
                .author(extractedAuthor.map(ExtractionResult::getAuthor)
                        .orElse(entry.getMember().getName()))
                .build();

    }
}
