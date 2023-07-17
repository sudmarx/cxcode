package CxReportingAPI.CxReportingAPI;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CxReportingAPI{

	private static String accesstoken;
	static String cx_url="https://fmdev.cxone.cloud";
	static String proxy_url="http://nvzenpxy.fhlmc.com";

	public static void main(String [] args) {
	
		try {
		    	// Create new http client   			
            	HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxy_url, 9400)))
            						.build();
            	
				try (Scanner inputforapikey = new Scanner(System.in)) {
                    System.out.println("Enter your checkmarx one API key to authenticate and press enter when finished:");
                    String input = inputforapikey.nextLine(); 
 
                     if (!input.isBlank()) {
                    	            
                        //new http post request to fetch bearer token
                        HttpRequest request = HttpRequest.newBuilder()
                        		.uri(URI.create(cx_url+"/auth/realms/fmdev/protocol/openid-connect/token"))
                        		.method("POST", HttpRequest.BodyPublishers.ofString("grant_type=refresh_token&client_id=ast-app&refresh_token="+ input))
                        		.header("Content-Type", "application/x-www-form-urlencoded")
                            	.build();
                        
                        HttpResponse<String> tokenresponse = client.send(request,HttpResponse.BodyHandlers.ofString());
                    	
                        if (tokenresponse.statusCode() != 200) {
                            throw new RuntimeException("There was an error fetching bearer token, please try again");	                
                        } 
                        else 
                        {
                        	ObjectMapper tokenobjmapper = new ObjectMapper();
                        	JsonNode tokenjn = tokenobjmapper.readTree(tokenresponse.body());
                            accesstoken = tokenjn.get("access_token").asText();
                            if(!accesstoken.isEmpty())
                            {
                            	FetchScans();		                	
                            }
                        }
                     }
                     else {
                    	 System.out.println("Entering your Checkmarx API key is required");
                     }
                }
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
				
		}
	
	public static void FetchScans() {
		
					
		try {
			   
				List<scansMetaData> firstlstscansmetadata=new ArrayList<>();
				List<scansMetaData> secondlstscansmetadata=new ArrayList<>();
				List<scansMetaData> thirdlstscansmetadata=new ArrayList<>();
				// Create new http client   			
            	HttpClient client1 = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxy_url, 9400)))
											  .build();
				//new http get request to fetch scan all results and filter by prod deployed and latest (scan tags)                
	            HttpRequest scansrequest = HttpRequest.newBuilder()
	            		.uri(URI.create(cx_url+"/api/scans/?limit=10000"))
	            		.method("GET", HttpRequest.BodyPublishers.noBody())
	            		.header("Content-Type", "application/x-www-form-urlencoded")
	            		.header("Authorization", accesstoken)
	                	.build();                 	
	            
	            HttpResponse<String> scansresponse = client1.send(scansrequest,HttpResponse.BodyHandlers.ofString());
	            
	            ObjectMapper scansmetaDatamapper = new ObjectMapper();
	            scansmetaDatamapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                scansmetaDatamapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	            JsonNode scansjn = scansmetaDatamapper.readTree(scansresponse.body());
                
                
				scansjn.get("scans").forEach(scan -> { 
                    if(scan.get("tags").has("latest") && 
                       scan.get("tags").has("buildNumber") && 
                       scan.get("tags").has("sor")  && 
                       scan.get("tags").has("scanSource") && 
                       scan.get("tags").has("codeRepo") && 
                       scan.get("tags").has("pipelineName") && 
                       scan.get("tags").has("commitId") && 
                       scan.get("tags").has("prodDeployed") && 
                       scan.get("tags").has("prodDeployedDate") && 
                       scan.get("tags").has("releaseTicketNumber") && 
                       scan.get("tags").has("releaseTicketStatus"))
                    {                        
                        scansMetaData scanitems = new scansMetaData();
                        
                        scanitems.setprojectName(scan.get("projectName").textValue());
                        scanitems.setprojectId(scan.get("projectId").textValue());
                        scanitems.setscanId(scan.get("id").textValue());
                        
                        scanitems.setlatest(scan.get("tags").get("latest").textValue());
                        scanitems.setbuildNumber(scan.get("tags").get("buildNumber").textValue());
                        scanitems.setsor(scan.get("tags").get("sor").textValue());
                        scanitems.setscanSource(scan.get("tags").get("scanSource").textValue());
                        scanitems.setcodeRepo(scan.get("tags").get("codeRepo").textValue());
                        scanitems.setpipelineName(scan.get("tags").get("pipelineName").textValue());
                        scanitems.setcommitId(scan.get("tags").get("commitId").textValue());
                        scanitems.setprodDeployed(scan.get("tags").get("prodDeployed").textValue());
                        scanitems.setprodDeployedDate(scan.get("tags").get("prodDeployedDate").textValue());
                        scanitems.setreleaseTicketNumber(scan.get("tags").get("releaseTicketNumber").textValue());
                        scanitems.setreleaseTicketStatus(scan.get("tags").get("releaseTicketStatus").textValue());
                       
                        FetchProjects(scan.get("projectId").textValue(), scanitems); 
                        
                        firstlstscansmetadata.add(scanitems);
                        //FetchresultsSAST(scanitems.getscanId(), scanitems);
                        
                    }
                });

				firstlstscansmetadata.forEach( scanrecord -> {
                	FetchresultsSAST(scanrecord).forEach( scanresultlistsecond -> {
                		secondlstscansmetadata.add(scanresultlistsecond);
                	}); 
                }); 
				
				firstlstscansmetadata.forEach( scanrecord -> {
                	FetchresultsSCA(scanrecord).forEach( scanresultlistthird -> {
                		thirdlstscansmetadata.add(scanresultlistthird);
                	}); 
                }); 				
				

                // Export Scans Data to CSV
                ExportCSV(secondlstscansmetadata,"SAST"); 
                ExportCSV(thirdlstscansmetadata,"SCA");  
                //////
	
			}	
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

        public static void FetchProjects(String projectId, scansMetaData scanitems){
        
            try
            {
                 // Create new http client   			
            	HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxy_url, 9400)))
						  					  .build(); 
				//new http get request to fetch project by project id                 
	            HttpRequest projectsrequest = HttpRequest.newBuilder()
	            		.uri(URI.create(cx_url+"/api/projects/"+projectId))
	            		.method("GET", HttpRequest.BodyPublishers.noBody())
	            		.header("Content-Type", "application/x-www-form-urlencoded")
	            		.header("Authorization", accesstoken)
	                	.build();                 	
	            
	            HttpResponse<String> projectresponse = client.send(projectsrequest,HttpResponse.BodyHandlers.ofString());
	            
	            ObjectMapper projectmetaDatamapper = new ObjectMapper();
	            projectmetaDatamapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                projectmetaDatamapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	            JsonNode projectjn = projectmetaDatamapper.readTree(projectresponse.body());

                if(projectjn.get("tags").has("assetId") && 
                       projectjn.get("tags").has("methodology"))
                {
                    scanitems.setassetId(projectjn.get("tags").get("assetId").textValue());
                    scanitems.setmethodology(projectjn.get("tags").get("methodology").textValue());
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }


        }

         public static List<scansMetaData> FetchresultsSAST(scansMetaData scanrecord){
        	 
        	List<scansMetaData> lstscansmetadata=new ArrayList<>();
        
            try{
                
                // Create new http client   			
	        	//HttpClient client = HttpClient.newHttpClient();  
	        	HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxy_url, 9400)))
						  					  .build();
	        	
	        					//new http get request to fetch project by project id                 
	            HttpRequest resultsrequest = HttpRequest.newBuilder()
	            		.uri(URI.create(cx_url+"/api/results?limit=10000&scan-id="+scanrecord.getscanId()))
	            		.method("GET", HttpRequest.BodyPublishers.noBody())
	            		.header("Content-Type", "application/x-www-form-urlencoded")
	            		.header("Authorization", accesstoken)
	                	.build();                 	
	            
	            HttpResponse<String> resultresponse = client.send(resultsrequest,HttpResponse.BodyHandlers.ofString());
	            
	            ObjectMapper resultmapper = new ObjectMapper();
	            resultmapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                resultmapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	            JsonNode resultjn = resultmapper.readTree(resultresponse.body());

                resultjn.get("results").forEach(result -> { 
        
                    if(result.get("type").textValue().equals("sast"))
                    {
                    
                    	scansMetaData newscan = new scansMetaData();
                    	
                    	newscan.setsimilarityId(result.get("similarityId").textValue());                       
                    	newscan.setseverity(result.get("severity").textValue());
                    	newscan.setscanDate(result.get("created").textValue());
                        if(result.get("description")!=null) {                        	
                        	newscan.setqueryDescription(result.get("description").textValue());
                    	}
                        else { 
                        	newscan.setqueryDescription("");                    	
                        }
                        
                        newscan.setqueryName(result.get("data").get("queryName").textValue());
                        newscan.setcweId(result.get("vulnerabilityDetails").get("cweId").textValue());    
                        
                    	newscan.setprojectName(scanrecord.getprojectName());
                        newscan.setprojectId(scanrecord.getprojectId());
                        newscan.setassetId(scanrecord.getassetId());
                        newscan.setmethodology(scanrecord.getmethodology());
                        
                        newscan.setscanId(scanrecord.getscanId());                        
                        newscan.setlatest(scanrecord.getlatest());
                        newscan.setbuildNumber(scanrecord.getbuildNumber());
                        newscan.setsor(scanrecord.getsor());
                        newscan.setscanSource(scanrecord.getscanSource());
                        newscan.setcodeRepo(scanrecord.getcodeRepo());
                        newscan.setpipelineName(scanrecord.getpipelineName());
                        newscan.setcommitId(scanrecord.getcommitId());
                        newscan.setprodDeployed(scanrecord.getprodDeployed());
                        newscan.setprodDeployedDate(scanrecord.getprodDeployedDate());
                        newscan.setreleaseTicketNumber(scanrecord.getreleaseTicketNumber());
                        newscan.setreleaseTicketStatus(scanrecord.getreleaseTicketStatus());
                    	
                        lstscansmetadata.add(newscan);
                        //System.out.println(newscan.getsimilarityId());
                    }                 
                    
                   
                });      
            }
            
            catch(Exception e)
            {
                e.printStackTrace();
            }
            
            return lstscansmetadata;
        }

        public static List<scansMetaData> FetchresultsSCA(scansMetaData scanrecord){
        
         
            	List<scansMetaData> lstscadata=new ArrayList<>();
                
                try{
                    
                    // Create new http client   			
    	        	//HttpClient client = HttpClient.newHttpClient();  
    	        	HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxy_url, 9400)))
							  					  .build();
    	        	
    	        					//new http get request to fetch project by project id                 
    	            HttpRequest resultsrequest = HttpRequest.newBuilder()
    	            		.uri(URI.create(cx_url+"/api/results?limit=10000&scan-id="+scanrecord.getscanId()))
    	            		.method("GET", HttpRequest.BodyPublishers.noBody())
    	            		.header("Content-Type", "application/x-www-form-urlencoded")
    	            		.header("Authorization", accesstoken)
    	                	.build();                 	
    	            
    	            HttpResponse<String> resultresponse = client.send(resultsrequest,HttpResponse.BodyHandlers.ofString());
    	            
    	            ObjectMapper resultmapper = new ObjectMapper();
    	            resultmapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                    resultmapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	            JsonNode resultjn = resultmapper.readTree(resultresponse.body());

                    resultjn.get("results").forEach(result -> { 
                                   
                        if(result.get("type").textValue()=="sca")
                        {
                                                   
                            scansMetaData newscan = new scansMetaData();
                        	
                        	newscan.setpackageName(result.get("data").get("packageIdentifier").textValue());                       
                        	newscan.setseverity(result.get("severity").textValue());
                        	newscan.setscanDate(result.get("created").textValue());
                            if(result.get("description")!=null) {                        	
                            	newscan.setcveDescription(result.get("description").textValue());
                        	}
                            else { 
                            	newscan.setcveDescription("");                    	
                            }
                            String pkg=result.get("data").get("packageIdentifier").textValue();
                            
                            newscan.setpackageVersion(result.get("data").get("packageIdentifier").textValue());
                            newscan.setcveId(result.get("vulnerabilityDetails").get("cweId").textValue());    
                            newscan.setmajorVersion(result.get("data").get("recommendedVersion").textValue()); 
                            newscan.setminorVersion(result.get("data").get("recommendedVersion").textValue()); 

                        	newscan.setprojectName(scanrecord.getprojectName());
                            newscan.setprojectId(scanrecord.getprojectId());
                            newscan.setassetId(scanrecord.getassetId());
                            newscan.setmethodology(scanrecord.getmethodology());
                            
                            newscan.setscanId(scanrecord.getscanId());                        
                            newscan.setlatest(scanrecord.getlatest());
                            newscan.setbuildNumber(scanrecord.getbuildNumber());
                            newscan.setsor(scanrecord.getsor());
                            newscan.setscanSource(scanrecord.getscanSource());
                            newscan.setcodeRepo(scanrecord.getcodeRepo());
                            newscan.setpipelineName(scanrecord.getpipelineName());
                            newscan.setcommitId(scanrecord.getcommitId());
                            newscan.setprodDeployed(scanrecord.getprodDeployed());
                            newscan.setprodDeployedDate(scanrecord.getprodDeployedDate());
                            newscan.setreleaseTicketNumber(scanrecord.getreleaseTicketNumber());
                            newscan.setreleaseTicketStatus(scanrecord.getreleaseTicketStatus());
                        	
                            lstscadata.add(newscan);
                        }
                        
                       
                    });      
                }
                
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                
                return lstscadata;         

        }

        public static void ExportCSV(List<scansMetaData> lstscansmetadata2, String type){
        
            try{

                 File csvOutputFile;

                if(lstscansmetadata2!=null && type!="")
                {
                    CsvMapper mapper = new CsvMapper();
                    mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

                    if(type=="SAST"){

                        csvOutputFile = new File("SAST.csv");
                        CsvSchema schema = CsvSchema.builder().setUseHeader(true)

                        // Project Meta Data
                        .addColumn("projectName", null)
                        .addColumn("assetId", null)
                        .addColumn("methodology", null)                   
                        .addColumn("scanId")

                        // Scan details
                        .addColumn("similarityId")
                        .addColumn("queryDescription")
                        .addColumn("queryName")
                        .addColumn("severity")
                        .addColumn("scanDate")
                        .addColumn("cweId")

                        // Scan Meta Data
                        .addColumn("sor")
                        .addColumn("scanSource")
                        .addColumn("codeRepo")
                        .addColumn("buildNumber")
                        .addColumn("pipelineName")
                        .addColumn("commitId")
                        .addColumn("latest")
                        .addColumn("prodDeployed")
                        .addColumn("prodDeployedDate")
                        .addColumn("releaseTicketNumber")
                        .addColumn("releaseTicketStatus")

                        .build();

                        ObjectWriter writer = mapper.writerFor(lstscansmetadata2.getClass()).with(schema);
                        writer.writeValues(csvOutputFile).writeAll(Arrays.asList(lstscansmetadata2));
                    }

                    if(type=="SCA"){
                        
                        csvOutputFile = new File("SCA.csv");
                        CsvSchema schema = CsvSchema.builder().setUseHeader(true)
                        // Project Meta Data
                        .addColumn("projectName", null)
                        .addColumn("assetId", null)
                        .addColumn("methodology", null)                   
                        .addColumn("id")

                        // Scan details
                        .addColumn("packageName")
                        .addColumn("packageVersion")
                        .addColumn("cveId")
                        .addColumn("cveDescription")
                        .addColumn("majorVersion")
                        .addColumn("minorVersion")
                        .addColumn("severity")
                        .addColumn("scanDate")

                        // Scan Meta Data
                        .addColumn("sor")
                        .addColumn("scanSource")
                        .addColumn("codeRepo")
                        .addColumn("buildNumber")
                        .addColumn("pipelineName")
                        .addColumn("commitId")
                        .addColumn("latest")
                        .addColumn("prodDeployed")
                        .addColumn("prodDeployedDate")
                        .addColumn("releaseTicketNumber")
                        .addColumn("releaseTicketStatus")

                        .build();

                        ObjectWriter writer = mapper.writerFor(lstscansmetadata2.getClass()).with(schema);
                        writer.writeValues(csvOutputFile).writeAll(Arrays.asList(lstscansmetadata2));
                    }

                    System.out.println("SAST and SCA files generated and saved");
                    //System.out.println(csvOutputFile);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
}
