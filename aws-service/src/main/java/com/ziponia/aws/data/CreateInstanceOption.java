package com.ziponia.aws.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateInstanceOption {

    private String imageId;
    private String securityGroupId;
    private String keyName;
    private Integer maxCount = 1;
    private Integer minCount = 1;
    private String instanceName;
    private InstanceType instanceType = InstanceType.T2_MICRO;
    private List<TagObject> list = new ArrayList<>();
    private boolean production = false;
    private String scripts;

    @Getter
    @Setter
    public static class TagObject {
        private String key;
        private String value;
    }
}
