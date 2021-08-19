package com.example.mmovie3.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//SerializedName, Expose взяты из библиотеки gson
public class TestClass {
    @SerializedName("testClassId")
    @Expose
    private int testClassId;
    @SerializedName("name")
    @Expose
    private String name;

    public TestClass(int testClassId, String name) {
        this.testClassId = testClassId;
        this.name = name;
    }

    public int getTestClassId() {
        return testClassId;
    }

    public void setTestClassId(int testClassId) {
        this.testClassId = testClassId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
