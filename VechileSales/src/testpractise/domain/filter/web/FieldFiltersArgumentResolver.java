package com.dizzion.portal.domain.filter.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class FieldFiltersArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String FILTER_OPERATOR_AND_VALUE_DELIMITER = ":";
    private static final HandlerMethodArgumentResolver paramsMapResolver = new RequestParamMapMethodArgumentResolver();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramClass = parameter.getParameterType();
        Class<?> generic = ResolvableType.forMethodParameter(parameter).resolveGeneric(0);
        return Set.class.isAssignableFrom(paramClass) && FieldFilter.class.isAssignableFrom(generic);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Map<String, String> allParams = (Map<String, String>) paramsMapResolver.resolveArgument(
                parameter, mavContainer, webRequest, binderFactory);

        return allParams.entrySet().stream()
                .filter(entry -> entry.getValue().contains(FILTER_OPERATOR_AND_VALUE_DELIMITER))
                .map(e -> FieldFilter.fromString(e.getKey(), e.getValue()))
                .filter(fieldFilter -> !fieldFilter.getValue().isEmpty())
                .collect(toSet());
    }
}
