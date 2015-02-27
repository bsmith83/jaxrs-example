package com.westbrain.sandbox.jaxrs.group;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * A simple model representing a group.
 *
 * <p>A group has an id, a name, and a description. Groups can also have members, but that is represented by a
 * separate {@link Member} object.</p>
 *
 * @see com.westbrain.sandbox.jaxrs.group.Member
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
@ApiModel(value = "A group is a collection of members")
public class Group {

    @ApiModelProperty(value = "Unique Id of group", required=true)
    private Long id;

    @ApiModelProperty(value = "Name of the group", required=true)
    private String name;

    @ApiModelProperty(value = "Description of the group", required=true)
    private String description;

    public Group() {}

    public Group(String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
