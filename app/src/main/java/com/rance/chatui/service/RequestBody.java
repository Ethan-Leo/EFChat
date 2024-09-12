package com.rance.chatui.service;

public class RequestBody {
    public RequestBody(String question) {
        this.model = "llama3.1";
        this.prompt = question;
        this.stream = false;
    }
    private String model;
    private String prompt;
    private boolean stream;
}
