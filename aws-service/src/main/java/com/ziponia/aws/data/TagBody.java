package com.ziponia.aws.data;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.ec2.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TagBody {

    @Getter
    private String[] resource_ids;

    @Setter
    @Getter
    private List<HashMap<String, String>> tags = new ArrayList<>();

    public void setResource_ids(String... resource_ids) {
        this.resource_ids = resource_ids;
    }

    public void addTag(String key, String value) {
        HashMap<String, String> hm = new HashMap<>();
        hm.put(key, value);
        tags.add(hm);
    }

    public ArrayList<Tag> toTags() {
        ArrayList<Tag> result = new ArrayList<>();
        Tag.Builder builder = Tag.builder();
        for (HashMap<String, String> hm : tags) {
            for (String key : hm.keySet()) {
                builder.key(key).value(hm.get(key));
                result.add(builder.build());
            }
        }

        return result;
    }
}
