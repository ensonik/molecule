package com.vtence.molecule.simple;

import com.vtence.molecule.Body;
import com.vtence.molecule.BytesBody;
import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpException;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.StringBody;
import com.vtence.molecule.util.Charsets;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Protocol;
import org.simpleframework.http.Response;

import java.io.IOException;
import java.nio.charset.Charset;

public class SimpleResponse implements com.vtence.molecule.Response {
    private final Response response;

    private Body body = BytesBody.empty();

    public SimpleResponse(Response response) {
        this.response = response;
    }

    public void redirectTo(String location) {
        status(HttpStatus.SEE_OTHER);
        header(Protocol.LOCATION, location);
    }

    public void header(String name, String value) {
        response.setValue(name, value);
    }

    public void headerDate(String name, long date) {
        response.setDate(name, date);
    }

    public void removeHeader(String name) {
        response.setValue(name, null);
    }

    public void cookie(Cookie cookie) {
        org.simpleframework.http.Cookie cooky =
                new org.simpleframework.http.Cookie(cookie.name(), cookie.value(), true);
        cooky.setProtected(cookie.httpOnly());
        response.setCookie(cooky);
    }

    public void contentType(String mediaType) {
        header(Protocol.CONTENT_TYPE, mediaType);
    }

    public String contentType() {
        return header(Protocol.CONTENT_TYPE);
    }

    public String header(String name) {
        return response.getValue(name);
    }

    public long contentLength() {
        return response.getContentLength();
    }

    public void contentLength(long length) {
        response.setContentLength(length);
    }

    public int statusCode() {
        return response.getCode();
    }

    public void status(HttpStatus status) {
        statusCode(status.code);
        statusText(status.text);
    }

    public void statusCode(int code) {
        response.setCode(code);
    }

    public void statusText(String reason) {
        response.setDescription(reason);
    }

    public void body(String text) throws IOException {
        body(new StringBody(text, charset()));
    }

    public void body(Body body) throws IOException {
        this.body = body;
        body.writeTo(response.getOutputStream((int) body.size()));
    }

    public Body body() {
        return body;
    }

    public long size() {
        return body.size();
    }

    public boolean empty() {
        return size() == 0;
    }

    public Charset charset() {
        ContentType type = response.getContentType();

        if (type == null || type.getCharset() == null) {
            return Charsets.ISO_8859_1;
        }

        return Charset.forName(type.getCharset());
    }

    public void reset() {
        try {
            response.reset();
        } catch (IOException e) {
            throw new HttpException("Response has already been committed", e);
        }
    }

    public <T> T unwrap(Class<T> type) {
        if (!type.isAssignableFrom(response.getClass()))
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        return type.cast(response);
    }
}
