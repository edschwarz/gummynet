const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin')

var production = process.argv.reduce(function(p, c){return p || c == '-p'}, false)

var config = {
	mode: "production", 
    context: path.join(__dirname, '/src/main/jsx'),
    entry: {
        app:'./app.jsx', // './gneScoreboard.jsx',

    },
    output: {
    	/// HACK for warfile name
        path: path.resolve(__dirname, 'target/' + 'gummy-deep-ui-0.1.1' + '/assets'),
        filename: path.normalize('[name].js'),
    	publicPath: 'assets/'
    },
    module: {
        rules: [
            {
                test: /\.less$|\.css$/,
                use: ExtractTextPlugin.extract({ 
                    fallback:'style-loader',
                    use:['css-loader','less-loader'],
                })            
            },
            {
                test: /\.jsx$|\.js$/,
                exclude: /(node_modules)/,
                use: [{loader: "babel-loader",
                	   options: {
                		   presets: ["react", ["es2015", {modules: false}], "stage-2"]
                	   }
                }]
                //loader: production ? ['babel-loader?presets[]=react,presets[]=es2015,presets[]=stage-2'] : 
                //					// ['react-hot-loader', 'babel-loader?presets[]=react,presets[]=es2015,presets[]=stage-2']
				//					  ['babel-loader?presets[]=react,presets[]=es2015,presets[]=stage-2']
            }
        ]
    },
    plugins: [
        new ExtractTextPlugin(path.normalize('[name].css')),
    ],
    stats:{
        children: false
    },
    optimization: {
        minimize: false,
        splitChunks: {
            cacheGroups: {
                commons: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendor',
                    chunks: 'all'
                }
            }
        }
    },
    devServer: {
        quiet: false,
            noInfo: false,
            stats:{
            assets: false,
                colors: false,
                version: true,
                hash: true,
                timings: true,
                chunks: true,
                chunkModules: false,
                children: false
        }
    }
}

if(production){
    config.plugins.push(new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': JSON.stringify('production')
            }
        })
    )
}

module.exports = config
