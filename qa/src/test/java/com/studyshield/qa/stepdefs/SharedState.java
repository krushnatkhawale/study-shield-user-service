package com.studyshield.qa.stepdefs;

import io.restassured.response.Response;

public class SharedState {

    public Response response;
    public String loginId;
    public String password;
    public String sessionId;
    public String accountId;
    public String parentId;
    public String parentName;
    public String studentId;

    public void reset() {
        response = null;
        loginId = null;
        password = null;
        sessionId = null;
        accountId = null;
        parentId = null;
        parentName = null;
        studentId = null;
    }
}
