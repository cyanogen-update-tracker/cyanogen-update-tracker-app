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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUpdateMethodId() {
        return updateMethodId;
    }

    public void setUpdateMethodId(Long updateMethodId) {
        this.updateMethodId = updateMethodId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isUseCustomImage() {
        return useCustomImage;
    }

    public void setUseCustomImage(boolean useCustomImage) {
        this.useCustomImage = useCustomImage;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public String getDutchTitle() {
        return dutchTitle;
    }

    public void setDutchTitle(String dutchTitle) {
        this.dutchTitle = dutchTitle;
    }

    public String getEnglishText() {
        return englishText;
    }

    public void setEnglishText(String englishText) {
        this.englishText = englishText;
    }

    public String getDutchText() {
        return dutchText;
    }

    public void setDutchText(String dutchText) {
        this.dutchText = dutchText;
    }
}
