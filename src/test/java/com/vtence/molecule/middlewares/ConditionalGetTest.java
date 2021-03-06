package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class ConditionalGetTest {

    ConditionalGet conditional = new ConditionalGet();

    Request request = new Request().method(GET);
    Response response = new Response();

    @Test
    public void
    sendsNotModifiedWithoutMessageBodyWhenGettingEntityWhoseRepresentationHasNotChanged() throws Exception {
        request.header("If-None-Match", "12345678");
        conditional.handle(request, response);

        response.header("ETag", "12345678")
                .contentType("text/plain").contentLength(32).body("response content")
                .done();

        assertNoExecutionError();
        assertThat(response).hasStatus(NOT_MODIFIED)
                            .hasBodySize(0)
                            .hasNoHeader("Content-Type")
                            .hasNoHeader("Content-Length");
    }

    @Test
    public void
    leavesResponseUnchangedOnGetWhenCacheValidatorsAreMissing() throws Exception {
        conditional.handle(request, response);
        response.body("response content").done();

        assertNoExecutionError();
        assertThat(response).hasStatus(OK)
                            .hasBodyText("response content");
    }

    @Test
    public void
    ignoresCacheValidatorsOnGetIfResponseNotOK() throws Exception {
        request.header("If-None-Match", "12345678");
        conditional.handle(request, response);
        response.status(CREATED).header("ETag", "12345678").done();

        assertNoExecutionError();
        assertThat(response).hasStatus(CREATED);
    }

    @Test
    public void
    appliesConditionalLogicToHeadRequestsAsWell() throws Exception {
        request.method(HEAD).header("If-None-Match", "12345678");
        conditional.handle(request, response);
        response.header("ETag", "12345678").done();

        assertNoExecutionError();
        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test
    public void
    ignoresNonGetOrHeadRequests() throws Exception {
        request.method(POST).header("If-None-Match", "12345678");
        conditional.handle(request, response);
        response.header("ETag", "12345678").done();

        assertNoExecutionError();
        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    sendsNotModifiedWhenGettingEntityWhichHasNotBeenModifiedSinceLastServed() throws Exception {
        final String lastModification = httpDate(Instant.now());
        request.header("If-Modified-Since", lastModification);
        conditional.handle(request, response);
        response.header("Last-Modified", lastModification).done();

        assertNoExecutionError();
        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test
    public void
    leavesResponseUnchangedWhenEntityHasNotBeenModifiedButETagIndicatesItIsNotCurrent() throws Exception {
        final String lastModification = httpDate(Instant.now());

        request.header("If-None-Match", "87654321")
               .header("If-Modified-Since", lastModification);
        conditional.handle(request, response);
        response.header("ETag", "12345678").header("Last-Modified", lastModification).done();

        assertNoExecutionError();
        assertThat(response).hasStatus(OK);
    }

    @Test
    public void
    leavesResponseUnchangedWhenEntityWasModifiedButETagIndicatesItIsCurrent() throws Exception {
        request.header("If-None-Match", "12345678")
               .header("If-Modified-Since", httpDate(oneHourAgo()));
        conditional.handle(request, response);
        response.header("ETag", "12345678")
                .header("Last-Modified", httpDate(Instant.now())).done();

        assertNoExecutionError();
        assertThat(response).hasStatus(OK);
    }

    private Instant oneHourAgo() {
        return Instant.now().minus(1, ChronoUnit.HOURS);
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}