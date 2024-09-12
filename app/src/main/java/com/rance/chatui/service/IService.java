package com.rance.chatui.service;

public interface IService {
    void initialize();
    void sendRequest(String question, IRequestCallback callback);
}
