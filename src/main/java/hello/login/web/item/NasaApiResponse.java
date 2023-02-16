package hello.login.web.item;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class NasaApiResponse {
    private String date;
    private String explanation;
    private String hdurl;
    private String media_type;
    private String service_version;
    private String title;
    private String url;

    public String getMediaType() {
        return media_type;
    }

    // getters and setters for other fields
}