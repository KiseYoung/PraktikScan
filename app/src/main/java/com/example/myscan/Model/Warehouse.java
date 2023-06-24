package com.example.myscan.Model;

import java.util.ArrayList;
import java.util.List;

public class Warehouse {
    private String warehouseId;
    private String warehouseName;

    public static final Warehouse PLACEHOLDER = new Warehouse("", "Выберите склад");

    public Warehouse() {
        // Пустой конструктор (необходим для использования с Firebase)
    }

    public Warehouse(String warehouseId, String warehouseName) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    @Override
    public String toString() {
        return warehouseName;
    }

    // Добавление записей складов
    public static List<Warehouse> getSampleWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();
        warehouses.add(new Warehouse("1", "Склад 1"));
        warehouses.add(new Warehouse("2", "Склад 2"));
        warehouses.add(new Warehouse("3", "Склад 3"));
        return warehouses;
    }
}