package com.earware.gummynet.deep;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * config object for Runner
 * @author edward
 *
 */
public class GummyRunnerConfig {
	public int getApproxNumberOfHandsToPlay() {return approxNumberOfHandsToPlay;}
	public String getPlayer1Name() {return player1Name;}
	public String getPlayer1DqnPath() {return player1DqnPath;}
	public String getPlayer1ConfigPath() {return player1ConfigPath;}
	public boolean isPlayer1IsTraining() {return player1IsTraining;}
	public String getPlayer2Name() {return player2Name;}
	public String getPlayer2DqnPath() {return player2DqnPath;}
	public String getPlayer2ConfigPath() {return player2ConfigPath;}
	public boolean isPlayer2IsTraining() {return player2IsTraining;}
	public boolean isSingleDqn() {return useSingleDqn;}
	
	public int approxNumberOfHandsToPlay=1;
	public String player1Name="joe";
	public String player1DqnPath=null;
	public String player1ConfigPath=null;
	public boolean player1IsTraining=false;
	public String player2Name="annie";
	public String player2DqnPath=null;
	public String player2ConfigPath=null;
	public boolean player2IsTraining=false;
	public boolean useSingleDqn=false;		
    public void save(String path) throws IOException {
    		FileWriter writer = new FileWriter(path);
    		new JSONObject(this).write(writer,2,0);
    		writer.close();
    }
    public static GummyRunnerConfig fromFile(String path) throws IOException {
    		JSONObject json = new JSONObject(new JSONTokener(new FileReader(path)));
    		return GummyRunnerConfig.fromJson(json);
    }        
    public static GummyRunnerConfig fromJson(JSONObject json) {
    	GummyRunnerConfig cfg = new GummyRunnerConfig();
    		cfg.setFromJson(json);
    		return cfg;
    }
    public void setFromJson(JSONObject json) {	
    		if (json.has("approxNumberOfHandsToPlay")) {this.approxNumberOfHandsToPlay = json.getInt("approxNumberOfHandsToPlay");}
    		if (json.has("singleDqn")) {this.useSingleDqn = json.getBoolean("singleDqn");}
    		
    		if (json.has("player1Name")) {this.player1Name = json.getString("player1Name");}
    		if (json.has("player1DqnPath")) {this.player1DqnPath = json.getString("player1DqnPath");}
    		if (json.has("player1ConfigPath")) {
    			this.player1ConfigPath = json.getString("player1ConfigPath");
    			if (this.player1ConfigPath!=null && this.player1ConfigPath.length()>0) {
    				this.player1IsTraining=true;
    			}
    		}
    		if (json.has("player1IsTraining")) {this.player1IsTraining = json.getBoolean("player1IsTraining");}

    		if (json.has("player2Name")) {this.player2Name = json.getString("player2Name");}
    		if (json.has("player2DqnPath")) {this.player2DqnPath = json.getString("player2DqnPath");}
    		if (json.has("player2ConfigPath")) {
    			this.player2ConfigPath = json.getString("player2ConfigPath");
    			if (this.player2ConfigPath!=null && this.player2ConfigPath.length()>0) {
    				this.player2IsTraining=true;
    			}
    		}
    		if (json.has("player2IsTraining")) {this.player2IsTraining = json.getBoolean("player2IsTraining");}
    		
    		validate();
    	}
    
    private void validate() {
    		if (useSingleDqn && player1IsTraining && player2IsTraining) {
    			String dqnPath = player1DqnPath;
    			if (dqnPath==null || dqnPath.length()==0) {
    				dqnPath = player2DqnPath;
    			}
    			String configPath = player1ConfigPath;
    			if (configPath==null || configPath.length()==0) {
    				configPath = player2ConfigPath;
    			}
    			validatePlayer(player1Name, dqnPath, configPath, player1IsTraining);
    		} else {
    			validatePlayer(player1Name, player1DqnPath, player1ConfigPath, player1IsTraining);
    			validatePlayer(player2Name, player2DqnPath, player2ConfigPath, player2IsTraining);
    		}
    }
    private void validatePlayer(String name, String dqnPath, String configPath, boolean isTraining) {
    		if (name==null || name.length()<1) {
    			throw new IllegalArgumentException("player name cannot be blank or null");
    		}
    		if (isTraining) {
        		if (configPath==null || configPath.length()<1) {
        			throw new IllegalArgumentException("player cannot train without a configuration");
        		}
    		}
    }
    
    @Override
    public String toString() {
    		return new JSONObject(this).toString(2);
    }
    
    public static void main(String[] args) {
    		try{
    			GummyRunnerConfig g = new GummyRunnerConfig();
    			g.approxNumberOfHandsToPlay=100;
    			g.useSingleDqn=true;
    			
    			g.player1ConfigPath="p1.cfg";
    			g.player1DqnPath="p1.dqn";
    			g.player1IsTraining=true;
    			g.player1Name="frank";
    			
    			g.player2ConfigPath="p2.cfg";
    			g.player2DqnPath="p2.dqn";
    			g.player2IsTraining=true;
    			g.player2Name="irving";

    			g.save("gRc");
    			
    			GummyRunnerConfig gg = GummyRunnerConfig.fromFile("gRc");
    			
    			System.out.println(gg.toString());
    			
    		}catch(Exception e) {e.printStackTrace();}
    	}
}


