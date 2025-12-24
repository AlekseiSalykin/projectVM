package ru.salykin.VisualMapping.DTO;

import java.util.List;

public class Field {
    private String name;
    private String type;
    private String path;
    private List<Field> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Field> getChildren() {
        return children;
    }

    public void setChildren(List<Field> children) {
        this.children = children;
    }
}
