package com.souzip.docs;

import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class UriDecodingPreprocessor implements OperationPreprocessor {

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        URI uri = request.getUri();
        String decoded = UriUtils.decode(uri.toString(), StandardCharsets.UTF_8);
        return new OperationRequestFactory().create(
            URI.create(decoded),
            request.getMethod(),
            request.getContent(),
            request.getHeaders(),
            request.getParts()
        );
    }

    @Override
    public OperationResponse preprocess(OperationResponse operationResponse) {
        return operationResponse;
    }
}
