package com.ziponia.aws.data;

public enum Ec2Commend {
    TERMINATE("terminate"),
    REBOOT("reboot"),
    STOP("stop"),
    START("start");

    public String name;

    Ec2Commend(String name) {
        this.name = name;
    }
}
