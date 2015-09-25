package models;

import java.util.List;

import com.avaje.ebean.Model;

public class ActivityStreamModel extends Model {
    public String type;
    public List<Object> data;
    public String series_type;
    public int original_size;
    public String resolution;

}
