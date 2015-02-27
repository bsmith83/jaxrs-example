package com.westbrain.sandbox.jaxrs.group;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * A simple model representing a single member of a group.
 *
 * @see com.westbrain.sandbox.jaxrs.group.Group
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
@ApiModel(value = "A member is an entity of a group")
public class Member {

    @ApiModelProperty(value = "Unique Id of member", required=true)
    private Long id;

    @ApiModelProperty(value = "Name of the member", required=true)
    private String name;

    public Member() {}

    public Member(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
