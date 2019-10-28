
const webpack = require("webpack");
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const path = require("path");

const dist = path.resolve(__dirname, "build/kotlin-js-min/main");

module.exports = {
    entry: 'barchart-js.js',
    output: {
        filename: "[name].bundle.js",
        path: dist,
        publicPath: ""
    },
    module: {
    },
    resolve: {
        modules: [
            path.resolve(__dirname, "build/kotlin-js-min/main")
        ]
    },
    devtool: 'source-map',
    plugins: [
        new UglifyJSPlugin({
            sourceMap: true
        })
    ]
};
