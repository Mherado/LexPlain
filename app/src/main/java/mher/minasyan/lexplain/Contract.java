package mher.minasyan.lexplain;

import java.io.Serializable;
import java.util.List;

public class Contract implements Serializable {
    private String documentId;
    private String userId;
    private String fileName;
    private String riskLevel;
    private List<String> contracttextparts;
    private List<String> contracttextrisks;
    private String conspect;

    public Contract() {
    }

    public Contract(String fileName, String riskLevel, List<String> contracttextparts, List<String> contracttextrisks) {
        this.fileName = fileName;
        this.riskLevel = riskLevel;
        this.contracttextparts = contracttextparts;
        this.contracttextrisks = contracttextrisks;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setConspect(String conspect) {
        this.conspect = conspect;
    }

    public String getFileName() {
        return fileName;
    }

    public String getConspect() {
        return conspect;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public List<String> getcontracttextparts() {
        return contracttextparts;
    }

    public List<String> getcontracttextrisks() {
        return contracttextrisks;
    }
}