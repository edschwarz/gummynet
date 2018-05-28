import React from "react";
import { render } from "react-dom";
import { Logo, Tips } from "./Utils";

//Import React Table
import ReactTable from "react-table";
import "./react-table.css";

//window.alert("dang");
console.log("crang!");

class App extends React.Component {

	getScoreboardFromServer(path) {
		var respclone;
		var myComponent = this; 
		var url = '/gummy-rest/rest/scoreboard/' + path;
		fetch(url)
		.then(function(response) {
			respclone = response.clone();
			return response.json(); 
		})
		.then(function(data) {
			// Do what you want with your data
			myComponent.setState({scoreboard:data});
		})
		.catch(function(err) {
			respclone.text()
			.then(function(text) {
				console.error('An error ocurred on response "' + text + '":',err);
			});
		});
	}

	getSummaryFromServer() {
		var respclone;
		var myComponent = this; 
		fetch('/gummy-rest/rest/summary')
		.then(function(response) {
			respclone = response.clone();
			return response.json(); 
		})
		.then(function(data) {
			// Do what you want with your data
			myComponent.setState({summary:data});
		})
		.catch(function(err) {
			respclone.text().then(function(text) {
				console.error('An error ocurred on response "' + text + '":',err);
			});
		});
	}

	refreshData() {
		try {  
			this.getSummaryFromServer();
			this.getScoreboardFromServer(this.state.scoreboardHowFarBack);
		} catch(error) {console.error("error refreshing data: " + error);}

	}  

	constructor() {
		try {  
			super();
			console.log("ctor");  		

			this.handleHowFarBackSubmit = this.handleHowFarBackSubmit.bind(this);
			this.handleHowFarBackChange = this.handleHowFarBackChange.bind(this);
			this.handleHowFarBackBlur = this.handleHowFarBackBlur.bind(this);
			this.refreshData = this.refreshData.bind(this);

			this.state = {
					summary:{},
					scoreboard:[],
					scoreboardHowFarBack:40,
					howFarBackVal:40,
					refreshMs:60000
			};
			this.state.howFarBackVal = this.state.scoreboardHowFarBack;

			this.refreshData();  		
			this.refreshHandle = setInterval(this.refreshData,this.state.refreshMs);

		} catch(error) {console.error("error constructing gneScoreboard App object: " + error);}
	}

	handleHowFarBackBlur(event) {
		if (this.state.howFarBackVal === "") {
			this.setState({howFarBackVal: this.state.scoreboardHowFarBack});
		}
	}

	handleHowFarBackChange(event) {
		if (!(event.target.value === this.state.howFarBackVal)) {
			if (this.isOKHowFarBack(event.target.value) || event.target.value === "") {
				this.setState({howFarBackVal: event.target.value});
				if (!(event.target.value === "")) {
					this.setState({scoreboardHowFarBack: Number(event.target.value)});
				}
			}
		}
	}

	handleHowFarBackSubmit(event) {
	}

	isOKHowFarBack(str) {
		return /^\+?\d+$/.test(str);
	}

	render() {
		const { summary } = this.state;
		const { scoreboard } = this.state;
		const { scoreboardHowFarBack } = this.state;
		const { howFarBackVal } = this.state;
		console.log("render - howFarBackVal:" + this.state.howFarBackVal + "  scoreboardHowFarBack:" + this.state.scoreboardHowFarBack);
		return (
				<div>
				<div>
				<p>{summary.summary}
				<br/>
				<input type="text" 
				value={howFarBackVal} 
				onChange={this.handleHowFarBackChange}
				onBlur={this.handleHowFarBackBlur}/>
				<em/>
				<button onClick={this.refreshData}>Refresh</button>
				</p>
				</div>
				<ReactTable
				data={scoreboard}
				columns={scoreboardColumns}
				defaultPageSize={20}
				minRows={3}
				className="-striped -highlight"
				SubComponent={row =>{
					return (
							<div style={{ padding: "20px" }}>
							<br />
							<pre>
							{row.original.histogram}
							</pre>
							<br />
							</div>
					);
				}}
				/>
				<br />
				<Tips />
				<Logo />
				</div>
		);
	}

}

const scoreboardColumns = [
	{
		Header: "Score Header",
		columns: [
			{
				Header: "Count",
				id: "count",
				accessor: "count"
			},
			{
				Header: "Score",
				id: "score",
				accessor: "score"
			},
			{
				Header: "Raw",
				id: "raw",
				accessor: "raw"
			},
			{
				Header: "Progeny Contrib",
				id: "progenyContrib",
				accessor: "progenyContrib"
			}
			]
	},
	{
		Header: "Details",
		columns: [
			{
				Header: "Win Ratio",
				id: "winRatio",
				accessor: "winRatio"
			},
			{
				Header: "Fitness",
				id: "fitness",
				accessor: "fitness"
			},
			{
				Header: "Turns/Hand",
				id: "turnsPerHand",
				accessor: "turnsPerHand"
			},
			{
				Header: "Selected",
				id: "numSelected",
				accessor: "numSelected"
			},
			{
				Header: "Progeny",
				id: "numProgeny",
				accessor: "numProgeny"
			},
			{
				Header: "Avg Progeny Score",
				id: "avgProgenyScore",
				accessor: "avgProgenyScore"
			},
			{
				Header: "Age",
				id: "age",
				accessor: "age"
			},
			{
				Header: "Model Name",
				id: "dqnName",
				accessor: "dqnName"
			}
			]
	},
	];

console.log("===> render begins...");

render(<App />, document.getElementById("root"));

console.log("===> render completed!");

