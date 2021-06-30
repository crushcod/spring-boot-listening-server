package com.crushoncod.listeningserver.filter;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Order(2)
@Slf4j
public class RequestResponseFilter implements Filter {
    private static final String UUID_TRANSACTION_KEY = "UUID";
    @Value(value = "${enableLoggingRequestDetails}")
    private boolean enableLoggingRequestDetails;

    public RequestResponseFilter(){
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        String uuid = createUUID();
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        log.debug("enableLoggingRequestDetails: {}", enableLoggingRequestDetails);

        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        List<String> values = Collections.list(req.getHeaderNames());
        String headers = values.size() > 0 ? "masked" : "";
        if (enableLoggingRequestDetails) {
            headers = (String)values.stream().map((name) -> {
                return name + ":" + Collections.list(req.getHeaders(name));
            }).collect(Collectors.joining(", "));
        }

        String params;
        if (enableLoggingRequestDetails) {
            params =req.getParameterMap().entrySet().stream().map((entry) -> {
                return entry.getKey() + ":" + Arrays.toString((Object[])entry.getValue());
            }).collect(Collectors.joining(", "));
        } else {
            params = req.getParameterMap().isEmpty() ? "" : "masked";
        }

        String queryString = req.getQueryString();
        String queryClause = org.springframework.util.StringUtils.hasLength(queryString) ? "?" + queryString : "";
        String dispatchType = !DispatcherType.REQUEST.equals(req.getDispatcherType()) ?
                "\"" + req.getDispatcherType() + "\" dispatch for " : "";

         String message = dispatchType + req.getMethod() + " \"" + getRequestUri(req) + queryClause + "\", " +
                "parameters={" + params + "}";

        StringBuilder sb = new StringBuilder();
        sb.append("> Headers: {")
                .append(headers)
                .append("}\n")
                .append("> Parameters: ")
                .append(message).append("\n")
                .append("> Body: ")
                .append(body);

        log.info("\n {}", sb);

        filterChain.doFilter(servletRequest, servletResponse);

        body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        removeUUID();
    }

    public static String createUUID() {
        String uuid = StringUtils.lowerCase(UUID.randomUUID().toString());
        MDC.put(UUID_TRANSACTION_KEY, UUID_TRANSACTION_KEY.concat(" [").concat(uuid).concat("]"));
        log.debug(MDC.get(UUID_TRANSACTION_KEY));
        return uuid;
    }

    public static void removeUUID() {
        log.debug("removing removeUUID: {}", UUID_TRANSACTION_KEY);
        MDC.remove(UUID_TRANSACTION_KEY);
    }

    private static String getRequestUri(HttpServletRequest request) {
        String uri = (String)request.getAttribute("javax.servlet.include.request_uri");
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return uri;
    }
}