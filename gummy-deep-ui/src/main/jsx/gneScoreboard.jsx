import React from "react";import { render } from "react-dom";import { Logo, Tips } from "./Utils";// Import React Tableimport ReactTable from "react-table";import "./react-table.css";//window.alert("dang");console.log("crang!");class App extends React.Component {  scoreboardHowFarBack : 40;  refreshMs : 5000;  refreshHandle : null;   getScoreboardFromServer(path) {    var respclone;    var myComponent = this; 	fetch('/gummy-rest/rest/scoreboard')    	.then(function(response) {    			respclone = response.clone();    			return response.json();     		})    	.then(function(data) {        		// Do what you want with your data		    	myComponent.setState({scoreboard:data});    	 	})    	.catch(function(err) {    			respclone.text().then(function(text) {        			console.error('An error ocurred on response "' + text + '":',err);        			});    	    });  }  getSummaryFromServer() {    var respclone;    var myComponent = this; 	fetch('/gummy-rest/rest/summary')    	.then(function(response) {    			respclone = response.clone();    			return response.json();     		})    	.then(function(data) {        		// Do what you want with your data		    	myComponent.setState({summary:data});    	 	})    	.catch(function(err) {    			respclone.text().then(function(text) {        			console.error('An error ocurred on response "' + text + '":',err);        			});    	    });  }    constructor() {	try {  	    super();	    this.state = {summary:{},scoreboard:[]};	    this.refreshData();	    setInterval(refreshData,this.refreshMs);	} catch(error) {console.error(error);}  }    refreshData() {	try {  	    this.getSummaryFromServer();	    this.getScoreboardFromServer();	} catch(error) {console.error(error);}	  }      render() {    const { summary } = this.state;    const { scoreboard } = this.state;    return (      <div>        <div><p><pre>{summary.summary}</pre></p></div>        <ReactTable          data={scoreboard}          columns={[            {              Header: "Score Header",              columns: [                {                  Header: "Count",                  id: "count",                  accessor: "count"                },                {                  Header: "Score",                  id: "score",                  accessor: "score"                },                {                  Header: "Raw",                  id: "raw",                  accessor: "raw"                },                {                  Header: "Progeny Contrib",                  id: "progenyContrib",                  accessor: "progenyContrib"                }              ]            },            {              Header: "Details",              columns: [                {                  Header: "Win Ratio",                  id: "winRatio",                  accessor: "winRatio"                },                {                  Header: "Fitness",                  id: "fitness",                  accessor: "fitness"                },                {                  Header: "Times Selected",                  id: "numSelected",                  accessor: "numSelected"                },                {                  Header: "Progeny Created",                  id: "numProgeny",                  accessor: "numProgeny"                },                {                  Header: "Avg Progeny Score",                  id: "avgProgenyScore",                  accessor: "avgProgenyScore"                },                {                  Header: "Age",                  id: "age",                  accessor: "age"                },                {                  Header: "Model Name",                  id: "dqnName",                  accessor: "dqnName"                }              ]            },          ]}          defaultPageSize={20}          minRows={3}          className="-striped -highlight"        />        <br />        <Tips />        <Logo />      </div>    );  }  }console.log("===> render begins...");render(<App />, document.getElementById("root"));console.log("===> render completed!");