package com.vtence.molecule.util;

import com.vtence.molecule.Body;
import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Response;

import java.io.IOException;
import java.nio.charset.Charset;

public class ResponseWrapper implements Response {

    private final Response response;

    public ResponseWrapper(Response response) {
        this.response = response;
    }

    public void redirectTo(String location) {
        response.redirectTo(location);
    }

    public String header(String name) {
        return response.header(name);
    }

    public void header(String name, String value) {
        response.header(name, value);
    }

    public void headerDate(String name, long date) {
        response.headerDate(name, date);
    }

    public void removeHeader(String name) {
        response.removeHeader(name);
    }

    public void cookie(Cookie cookie) {
        response.cookie(cookie);
    }

    public void contentType(String contentType) {
        response.contentType(contentType);
    }

    public String contentType() {
        return response.contentType();
    }

    public int statusCode() {
        return response.statusCode();
    }

    public void status(HttpStatus status) {
        response.status(status);
    }

    public void statusCode(int code) {
        response.statusCode(code);
    }

    public long contentLength() {
        return response.contentLength();
    }

    public void contentLength(long length) {
        response.contentLength(length);
    }

    public Charset charset() {
        return response.charset();
    }

    public void body(String text) throws IOException {
        response.body(text);
    }

    public void body(Body body) throws IOException {
        response.body(body);
    }

    public Body body() {
        return response.body();
    }

    public long size() {
        return response.size();
    }

    public boolean empty() {
        return response.empty();
    }

    public void reset() throws IOException {
        response.reset();
    }

    public <T> T unwrap(Class<T> type) {
        return response.unwrap(type);
    }
}
