package com.vtence.molecule.middlewares;

import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.util.HttpDate;
import com.vtence.molecule.util.Streams;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.vtence.molecule.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.HttpStatus.OK;
import static com.vtence.molecule.support.MockRequest.GET;
import static com.vtence.molecule.support.MockResponse.aResponse;
import static com.vtence.molecule.support.ResourceLocator.onClasspath;
import static java.lang.String.valueOf;
import static org.hamcrest.Matchers.equalTo;

public class FileServerTest {

    static final String SAMPLE_IMAGE = "images/sample.png";

    File base = onClasspath().locate("assets");
    FileServer fileServer = new FileServer(base);
    File file = new File(base, SAMPLE_IMAGE);

    MockRequest request = GET(SAMPLE_IMAGE);
    MockResponse response = aResponse();

    @Test public void
    servesFiles() throws Exception {
        fileServer.handle(request, response);

        response.assertStatus(OK);
        response.assertContentSize(file.length());
        response.assertContent(contentOf(file));
        response.assertHeader("Content-Length", valueOf(file.length()));
    }

    @Test public void
    guessesMimeTypeFromExtension() throws Exception {
        fileServer.handle(request, response);

        response.assertContentType("image/png");
    }

    @Test public void
    learnsNewMediaTypes() throws Exception {
        fileServer.registerMediaType("png", "image/custom-png");
        fileServer.handle(request, response);

        response.assertContentType("image/custom-png");
    }

    @Test public void
    setsLastModifiedHeader() throws Exception {
        fileServer.handle(request, response);

        response.assertHeader("Last-Modified", equalTo(HttpDate.format(file.lastModified())));
    }

    @Test public void
    rendersNotFoundWhenFileIsNotFound() throws Exception {
        fileServer.handle(request.withPath("/images/missing.png"), response);
        response.assertStatus(NOT_FOUND);
    }

    @Test public void
    sendsNotModifiedIfFileHasNotBeenModifiedSinceLastServe() throws Exception {
        request.withHeader("If-Modified-Since", HttpDate.format(file.lastModified()));
        fileServer.handle(request, response);
        response.assertStatus(NOT_MODIFIED);
    }

    @Test public void
    addsConfiguredCustomHeadersToResponse() throws Exception {
        fileServer.
                addHeader("Cache-Control", "public, max-age=60").
                addHeader("Access-Control-Allow-Origin", "*");

        fileServer.handle(request, response);
        response.assertHeader("Cache-Control", "public, max-age=60");
        response.assertHeader("Access-Control-Allow-Origin", "*");
    }

    private byte[] contentOf(final File file) throws IOException, URISyntaxException {
        return Streams.toBytes(new FileInputStream(file));
    }
}