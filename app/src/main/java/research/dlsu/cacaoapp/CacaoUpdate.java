package research.dlsu.cacaoapp;

import java.sql.Date;

/**
 * Created by courtneyngo on 4/26/16.
 */
public class CacaoUpdate {

    private long id;
    private String path;
    private Date date;

    // additional
    private String result;

    // private long idCacao;
    private long serverIdCacaoUpdate;
    // private long serverIdCacao;

    private int adapterPosition;

    private CacaoType cacaoType;
    public enum CacaoType{
        SCYLLA_SERRATA, SCYLlA_TRANQUEBARICA
    }

    public CacaoUpdate(){}

    public CacaoUpdate(long id, String path, Date date) {
        this.id = id;
        this.path = path;
        this.date = date;
    }

    public CacaoUpdate(long id, String path, Date date, CacaoType cacaoType) {
        this.id = id;
        this.path = path;
        this.date = date;
        this.cacaoType = cacaoType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

//    public long getIdCacao() {
//        return idCacao;
//    }
//
//    public void setIdCacao(long idCacao) {
//        this.idCacao = idCacao;
//    }
//
    public long getServerIdCacaoUpdate() {
        return serverIdCacaoUpdate;
    }

    public void setServerIdCacaoUpdate(long serverIdCacaoUpdate) {
        this.serverIdCacaoUpdate = serverIdCacaoUpdate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    //
//    public long getServerIdCacao() {
//        return serverIdCacao;
//    }
//
//    public void setServerIdCacao(long serverIdCacao) {
//        this.serverIdCacao = serverIdCacao;
//    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public CacaoType getCacaoType() {
        return cacaoType;
    }

    public void setCacaoType(CacaoType cacaoType) {
        this.cacaoType = cacaoType;
    }
}
