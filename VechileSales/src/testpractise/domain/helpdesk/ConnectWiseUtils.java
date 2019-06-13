package com.dizzion.portal.domain.helpdesk;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.user.dto.User;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Pageable;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import static com.dizzion.portal.domain.helpdesk.ConnectWiseQueryFactory.queryFromFilters;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@UtilityClass
public class ConnectWiseUtils {
    private static final String AUTHOR_PREFIX = "Portal user: ";
    private static final String AUTHOR_POSTFIX = "\r\n\r\n";
    private static final String DEFAULT_ORDER_BY = "lastUpdated desc";

    public static Optional<ExtractionResult> extractAuthor(String text) {
        if (!hasText(text)) {
            return Optional.empty();
        }
        int start = text.indexOf(AUTHOR_PREFIX);
        int end = text.indexOf(AUTHOR_POSTFIX);
        if (start >= 0 && end > start) {
            String author = text.substring(start + AUTHOR_PREFIX.length(), end);
            String remaining = end + AUTHOR_POSTFIX.length() < text.length()
                    ? text.substring(end + AUTHOR_POSTFIX.length())
                    : "";
            return Optional.of(new ExtractionResult(author, remaining));
        } else {
            return Optional.empty();
        }
    }

    public static String embedAuthor(User author, String text) {
        return AUTHOR_PREFIX + author.toNameEmailString() + AUTHOR_POSTFIX + text;
    }

    public static URI urlWithConditions(String uri, FieldFilter... filters) {
        return urlWithConditions(uri, asList(filters));
    }

    public static URI urlWithConditions(String uri, Collection<FieldFilter> filters) {
        return fromUriString(uri)
                .queryParam("conditions", queryFromFilters(filters))
                .build().toUri();
    }

    public static URI urlWithConditions(String uri, Collection<FieldFilter> filters, Pageable pageRequest) {
        return fromUriString(uri)
                .queryParam("conditions", queryFromFilters(filters))
                .queryParam("orderBy", getOrderByExp(pageRequest))
                .queryParam("page", pageRequest.getPageNumber() + 1)
                .queryParam("pageSize", pageRequest.getPageSize())
                .build().toUri();
    }

    public static String urlWithPageSize(String uri, int pageSize) {
        return fromUriString(uri)
                .queryParam("pageSize", pageSize)
                .build().toUriString();
    }

    public static URI urlWithChildConditions(String uri, FieldFilter... filters) {
        return fromUriString(uri)
                .queryParam("childConditions", queryFromFilters(filters))
                .build().toUri();
    }

    private static String getOrderByExp(Pageable pageRequest) {
        return Optional.ofNullable(pageRequest.getSort())
                .flatMap(sort -> Optional.ofNullable(getOnlyElement(sort, null)))
                .map(order -> order.getProperty() + " " + order.getDirection())
                .orElse(DEFAULT_ORDER_BY);
    }

    @Value
    public static class ExtractionResult {
        String author;
        String remainingText;
    }
}
