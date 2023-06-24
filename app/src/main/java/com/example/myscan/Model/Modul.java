package com.example.myscan.Model;

public class Modul {
    private String moduleId;
    private String model;
    private String serialNumber;
    private String partNumber;
    private String manufactureDate;
    private String imageUri;
    private String warehouses;


    public Modul() {
        // Пустой конструктор без аргументов (не забудьте добавить его)
    }
    public Modul(String moduleId, String model, String serialNumber, String partNumber, String manufactureDate, String imageUri, String warehouses) {
        this.moduleId = moduleId;
        this.model = model;
        this.serialNumber = serialNumber;
        this.partNumber = partNumber;
        this.manufactureDate = manufactureDate;
        this.imageUri = imageUri;
        this.warehouses = warehouses;
    }

    public String getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(String warehouses) {
        this.warehouses = warehouses;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}

