package com.parfum.ecommerce.supplier;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FragellaProduct {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Brand")
    private String brand;

    @JsonProperty("Year")
    private Integer year;

    private BigDecimal rating;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Image URL")
    private String imageUrl;

    @JsonProperty("Image URL Transparent")
    private String imageUrlTransparent;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Price")
    private BigDecimal price;

    @JsonProperty("OilType")
    private String oilType;

    @JsonProperty("Longevity")
    private String longevity;

    @JsonProperty("Sillage")
    private String sillage;

    @JsonProperty("Confidence")
    private String confidence;

    @JsonProperty("Popularity")
    private String popularity;

    @JsonProperty("Price Value")
    private String priceValue;

    @JsonProperty("General Notes")
    private List<String> generalNotes;

    @JsonProperty("Main Accords")
    private List<String> mainAccords;

    @JsonProperty("Main Accords Percentage")
    private Map<String, String> mainAccordsPercentage;

    @JsonProperty("Season Ranking")
    private List<FragellaRanking> seasonRanking;

    @JsonProperty("Occasion Ranking")
    private List<FragellaRanking> occasionRanking;

    @JsonProperty("Notes")
    private FragellaNotes notes;

    @JsonProperty("Image Fallbacks")
    private List<String> imageFallbacks;

    @JsonProperty("Purchase URL")
    private String purchaseUrl;

    public FragellaProduct() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrlTransparent() {
        return imageUrlTransparent;
    }

    public void setImageUrlTransparent(String imageUrlTransparent) {
        this.imageUrlTransparent = imageUrlTransparent;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOilType() {
        return oilType;
    }

    public void setOilType(String oilType) {
        this.oilType = oilType;
    }

    public String getLongevity() {
        return longevity;
    }

    public void setLongevity(String longevity) {
        this.longevity = longevity;
    }

    public String getSillage() {
        return sillage;
    }

    public void setSillage(String sillage) {
        this.sillage = sillage;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getPopularity() {
        return popularity;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public String getPriceValue() {
        return priceValue;
    }

    public void setPriceValue(String priceValue) {
        this.priceValue = priceValue;
    }

    public List<String> getGeneralNotes() {
        return generalNotes;
    }

    public void setGeneralNotes(List<String> generalNotes) {
        this.generalNotes = generalNotes;
    }

    public List<String> getMainAccords() {
        return mainAccords;
    }

    public void setMainAccords(List<String> mainAccords) {
        this.mainAccords = mainAccords;
    }

    public Map<String, String> getMainAccordsPercentage() {
        return mainAccordsPercentage;
    }

    public void setMainAccordsPercentage(Map<String, String> mainAccordsPercentage) {
        this.mainAccordsPercentage = mainAccordsPercentage;
    }

    public List<FragellaRanking> getSeasonRanking() {
        return seasonRanking;
    }

    public void setSeasonRanking(List<FragellaRanking> seasonRanking) {
        this.seasonRanking = seasonRanking;
    }

    public List<FragellaRanking> getOccasionRanking() {
        return occasionRanking;
    }

    public void setOccasionRanking(List<FragellaRanking> occasionRanking) {
        this.occasionRanking = occasionRanking;
    }

    public FragellaNotes getNotes() {
        return notes;
    }

    public void setNotes(FragellaNotes notes) {
        this.notes = notes;
    }

    public List<String> getImageFallbacks() {
        return imageFallbacks;
    }

    public void setImageFallbacks(List<String> imageFallbacks) {
        this.imageFallbacks = imageFallbacks;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public static class FragellaRanking {

        private String name;
        private BigDecimal score;

        public FragellaRanking() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }
    }

    public static class FragellaNotes {

        @JsonProperty("Top")
        private List<FragellaNote> top;

        @JsonProperty("Middle")
        private List<FragellaNote> middle;

        @JsonProperty("Base")
        private List<FragellaNote> base;

        public FragellaNotes() {
        }

        public List<FragellaNote> getTop() {
            return top;
        }

        public void setTop(List<FragellaNote> top) {
            this.top = top;
        }

        public List<FragellaNote> getMiddle() {
            return middle;
        }

        public void setMiddle(List<FragellaNote> middle) {
            this.middle = middle;
        }

        public List<FragellaNote> getBase() {
            return base;
        }

        public void setBase(List<FragellaNote> base) {
            this.base = base;
        }
    }

    public static class FragellaNote {

        private String name;
        private String imageUrl;

        public FragellaNote() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}