package com.arjanvlek.cyngnotainfo.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstallGuideData {

    private Long id;
    @JsonProperty(value = "device_id")
    private Long deviceId;
    @JsonProperty(value = "update_method_id")
    private Long updateMethodId;
    @JsonProperty(value = "page_number")
    private int pageNumber;
    @JsonProperty(value = "file_extension")
    private String fileExtension;
    @JsonProperty(value = "image_url")
    private String imageUrl;
    @JsonProperty(value = "use_custom_image")
    private boolean useCustomImage;
    @JsonProperty(value = "title_en")
    private String englishTitle;
    @JsonProperty(value = "title_nl")
    private String dutchTitle;
    @JsonProperty(value = "text_en")
    private String englishText;
    @JsonProperty(value = "text_nl")
    private String dutchText;
}
