import React from "react";
import { render } from "react-dom";
import { Logo, Tips, scoreFormat, fitnessFormat, percentFormat, floatFormat } from "./Utils";

//Import React Table
import ReactTable from "react-table";
import "./react-table.css";

//window.alert("dang");
console.log("crang!");

class App extends React.Component {

	getFromServer(path, stateVar) {
		var respclone;
		var myComponent = this; 
		var url = '/gummy-rest/rest/' + path;
		fetch(url)
		.then(function(response) {
			respclone = response.clone();
			return response.json(); 
		})
		.then(function(data) {
			// Do what you want with your data
			myComponent.setState({[stateVar]:data});
		})
		.catch(function(err) {
			respclone.text()
			.then(function(text) {
				console.error('An error ocurred fetching ' + stateVar + ': response: "' + text + '"  error: ',err);
			});
		});
	}

	getScoreboardFromServer(howFarBack) {
		var path = 'scoreboard/' + howFarBack;
		this.getFromServer(path, "scoreboard")
	}

	getSummaryFromServer() {
		this.getFromServer("summary", "summary")
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
		return (
				<div>
					<div>
						<p>
						{summary.summary}
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
									<pre>{row.original.histogram}</pre>
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

function isNumeric(n) {return !isNaN(parseFloat(n)) && isFinite(n);}
function nformat(number, format) {
	return isNumeric(number) ? format.format(number) :
			typeof number == 'undefined' ? "" 
					: number;} 
function hms(seconds) {
	var fmt = new Intl.NumberFormat('en-US',{style: 'decimal', minimumIntegerDigits: '2' });
	var s = seconds % 60;
    var m = Math.floor(seconds / 60) % 60;
    var h = Math.floor(seconds / (60 * 60));
    return h + ':' + fmt.format(m) + ':' + 	fmt.format(s);
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
				accessor: "score",
				Cell: props => (nformat(props.original.score,scoreFormat))
			},
			{
				Header: "Raw",
				id: "raw",
				accessor: "raw",
				Cell: props => (nformat(props.original.raw,floatFormat))
			},
			{
				Header: "Progeny Contrib",
				id: "progenyContrib",
				accessor: "progenyContrib",
				Cell: props => (nformat(props.original.progenyContrib,floatFormat))
			}
			]
	},
	{
		Header: "Details",
		columns: [
			{
				Header: "Win Ratio",
				id: "winRatio",
				accessor: "winRatio",
				Cell: props => (nformat(props.original.winRatio,percentFormat))
			},
			{
				Header: "Fitness",
				id: "fitness",
				accessor: "fitness",
				Cell: props => (nformat(props.original.fitness,fitnessFormat))
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
				accessor: "avgProgenyScore",
				Cell: props => (nformat(props.original.avgProgenyScore,scoreFormat))
			},
			{
				Header: "Age",
				id: "age",
				accessor: "age",
				Cell: props => (hms(props.original.age))
			},
			{
				Header: "Model Name",
				id: "dqnName",
				accessor: "dqnName"
			}
			]
	},
	];

// ///////////////////////////////////////////
render(<App />, document.getElementById("root"));

