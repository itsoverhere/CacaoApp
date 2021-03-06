package research.dlsu.cacaoapp;

import java.sql.Date;

/**
 * Created by student on 13/01/2017.
 */

public class Cacao {

    private int serverIdCacao;

    private long id;
    private long phoneidcacao;
    private long idonsiteuser;
    private long idoffsiteuser;

    private double weight;
    private double latitude;
    private double longitude;

    private String status;
    private String tag;
    private String result;

    private String farm;
    private String city;

    private String serialNumber;

    private Date lastUpdate;
    private int numOfUpdates;

    public Cacao(){}

    public Cacao(long id, String status, String tag, Date lastUpdate, int numOfUpdates) {
        this.id = id;
        this.status = status;
        this.tag = tag;
        this.lastUpdate = lastUpdate;
        this.numOfUpdates = numOfUpdates;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getNumOfUpdates() {
        return numOfUpdates;
    }

    public void setNumOfUpdates(int numOfUpdates) {
        this.numOfUpdates = numOfUpdates;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFarm() {
        return farm;
    }

    public void setFarm(String farm) {
        this.farm = farm;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getServerIdCacao() {
        return serverIdCacao;
    }

    public void setServerIdCacao(int serverIdCacao) {
        this.serverIdCacao = serverIdCacao;
    }

    public long getPhoneidcacao() {
        return phoneidcacao;
    }

    public void setPhoneidcacao(long phoneidcacao) {
        this.phoneidcacao = phoneidcacao;
    }

    public long getIdonsiteuser() {
        return idonsiteuser;
    }

    public void setIdonsiteuser(long idonsiteuser) {
        this.idonsiteuser = idonsiteuser;
    }

    public long getIdoffsiteuser() {
        return idoffsiteuser;
    }

    public void setIdoffsiteuser(long idoffsiteuser) {
        this.idoffsiteuser = idoffsiteuser;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
