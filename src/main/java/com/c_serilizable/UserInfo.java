package com.c_serilizable;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String name;
    private int age;
    private String sex;
    private String mail;
    private String address;
    private byte[] attachment;

    public UserInfo() {
    }

    public UserInfo(String name, int age, String sex, String mail, String address, byte[] attachment) {
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.mail = mail;
        this.address = address;
        this.attachment = attachment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", mail='" + mail + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
