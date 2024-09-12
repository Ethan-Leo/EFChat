package com.rance.chatui.service;

public interface IRequestCallback {
    void onSuccess(String response);
    void onFailed(String error);
}
