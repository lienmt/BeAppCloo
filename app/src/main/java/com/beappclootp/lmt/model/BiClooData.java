package com.beappclootp.lmt.model;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class BiClooData {

    @Id
    private Long id;

    @Index(unique = true)
    @SerializedName("number")
    @Expose
    private Integer number;

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("address")
    @Expose
    private String address;

    @Transient
    @SerializedName("position")
    @Expose
    private Position position;

    private Double latitud;
    private Double longitud;

    @SerializedName("banking")
    @Expose
    private Boolean banking;
    @SerializedName("bonus")
    @Expose
    private Boolean bonus;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("contract_name")
    @Expose
    private String contractName;
    @SerializedName("bike_stands")
    @Expose
    private Integer bikeStands;
    @SerializedName("available_bike_stands")
    @Expose
    private Integer availableBikeStands;
    @SerializedName("available_bikes")
    @Expose
    private Integer availableBikes;

    @SerializedName("last_update")
    @Expose
    private Long lastUpdate;

    private String distance;

    private Boolean favorite;

    @Generated(hash = 808003785)
    public BiClooData(Long id, Integer number, String name, String address,
            Double latitud, Double longitud, Boolean banking, Boolean bonus,
            String status, String contractName, Integer bikeStands,
            Integer availableBikeStands, Integer availableBikes, Long lastUpdate,
            String distance, Boolean favorite) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.address = address;
        this.latitud = latitud;
        this.longitud = longitud;
        this.banking = banking;
        this.bonus = bonus;
        this.status = status;
        this.contractName = contractName;
        this.bikeStands = bikeStands;
        this.availableBikeStands = availableBikeStands;
        this.availableBikes = availableBikes;
        this.lastUpdate = lastUpdate;
        this.distance = distance;
        this.favorite = favorite;
    }

    @Generated(hash = 1738155769)
    public BiClooData() {
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Boolean getBanking() {
        return banking;
    }

    public void setBanking(Boolean banking) {
        this.banking = banking;
    }

    public Boolean getBonus() {
        return bonus;
    }

    public void setBonus(Boolean bonus) {
        this.bonus = bonus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public Integer getBikeStands() {
        return bikeStands;
    }

    public void setBikeStands(Integer bikeStands) {
        this.bikeStands = bikeStands;
    }

    public Integer getAvailableBikeStands() {
        return availableBikeStands;
    }

    public void setAvailableBikeStands(Integer availableBikeStands) {
        this.availableBikeStands = availableBikeStands;
    }

    public Integer getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(Integer availableBikes) {
        this.availableBikes = availableBikes;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}