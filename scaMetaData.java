package CxReportingAPI.CxReportingAPI;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class scaMetaData {

	public scaMetaData(){};

    private String projectId;
    public String getprojectId(){
        return projectId;
    }
    public void setprojectId(String projectId){
        this.projectId=projectId;
    }
	
	private String projectName;
	public String getprojectName() {
	        return projectName;
	}
	public void setprojectName(String projectName) {
	        this.projectName = projectName;
	}
	    
	private String assetId;	
	public String getassetId() {
	        return assetId;
	}
	public void setassetId(String assetId) {
	        this.assetId = assetId;
	}
	
	private String methodology;	
	public String getmethodology() {
	        return methodology;
	}
	public void setmethodology(String methodology) {
	        this.methodology = methodology;
	}	

	private String tags;
	public String gettags(){
		return tags;
	}
	public void settags(String tags){
		this.tags=tags;
	}

    private String sor;	
    public String getsor() {
            return sor;
    }
    public void setsor(String sor) {
            this.sor = sor;
    }
    
    private String scanSource;	
    public String getscanSource() {
            return scanSource;
    }
    public void setscanSource(String scanSource) {
            this.scanSource = scanSource;
    }
    
    private String codeRepo;	
    public String getcodeRepo() {
            return codeRepo;
    }
    public void setcodeRepo(String codeRepo) {
            this.codeRepo = codeRepo;
    }
    
    private String buildNumber;	
    public String getbuildNumber() {
            return buildNumber;
    }
    public void setbuildNumber(String buildNumber) {
            this.buildNumber = buildNumber;
    }
    
    private String pipelineName;	
    public String getpipelineName() {
            return pipelineName;
    }
    public void setpipelineName(String pipelineName) {
            this.pipelineName = pipelineName;
    }
    
    private String commitId;	
    public String getcommitId() {
            return commitId;
    }
    public void setcommitId(String commitId) {
            this.commitId = commitId;
    }
    
    private String latest;	
    public String getlatest() {
            return latest;
    }
    public void setlatest(String latest) {
            this.latest = latest;
    }
        
    ////
    private String prodDeployed;	
    public String getprodDeployed() {
            return prodDeployed;
    }
    public void setprodDeployed(String prodDeployed) {
            this.prodDeployed = prodDeployed;
    }
    
    
    ////
    private String prodDeployedDate;	
    public String getprodDeployedDate() {
            return prodDeployedDate;
    }
    public void setprodDeployedDate(String prodDeployedDate) {
            this.prodDeployedDate = prodDeployedDate;
    }
    
    //////
    private String releaseTicketNumber;	
    public String getreleaseTicketNumber() {
            return releaseTicketNumber;
    }
    public void setreleaseTicketNumber(String releaseTicketNumber) {
            this.releaseTicketNumber = releaseTicketNumber;
    }
    
    ///
    private String releaseTicketStatus;	
    public String getreleaseTicketStatus() {
            return releaseTicketStatus;
    }
    public void setreleaseTicketStatus(String releaseTicketStatus) {
            this.releaseTicketStatus = releaseTicketStatus;
    }

    

}