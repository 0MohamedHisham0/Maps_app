package com.m.hisham.maps_app.models;

import java.util.List;

public class Restaurant_Model {
    private List<restaurantItem> results;

    public static class restaurantItem {
        private geometry geometry;
        private String icon;
        private String name;

        public restaurantItem(Restaurant_Model.geometry geometry, String icon, String name) {
            this.geometry = geometry;
            this.icon = icon;
            this.name = name;
        }

        public Restaurant_Model.geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Restaurant_Model.geometry geometry) {
            this.geometry = geometry;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class geometry {
        private location location;

        public geometry(Restaurant_Model.location location) {
            this.location = location;
        }

        public Restaurant_Model.location getLocation() {
            return location;
        }

        public void setLocation(Restaurant_Model.location location) {
            this.location = location;
        }
    }

    public static class location {
        private String lat;
        private String lng;

        public location(String lat, String lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }

    public Restaurant_Model(List<restaurantItem> results) {
        this.results = results;
    }

    public List<restaurantItem> getResults() {
        return results;
    }

    public void setResults(List<restaurantItem> results) {
        this.results = results;
    }
}

