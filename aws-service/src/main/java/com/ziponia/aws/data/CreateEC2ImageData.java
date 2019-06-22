package com.ziponia.aws.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class CreateEC2ImageData {
    private String name;
    private String instanceId;
    private String description;

    public ArrayList<HashMap<String, String>> tags;
}
