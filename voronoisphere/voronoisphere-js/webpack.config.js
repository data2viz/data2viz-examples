var webpack = require("webpack");
var path = require("path");
var outputMin = "build/classes/main/min/";

module.exports = {
    entry: path.resolve(__dirname, outputMin + "voronoisphere-js.js"),
    output: {
        path: path.resolve(__dirname, "build"),
        libraryTarget: 'var',
        library: 'Voronoi',
        filename: "bundle.js"
    },
    resolve: {
        alias: {
            'd2v-color-js':          path.resolve(__dirname, outputMin + "d2v-color-js.js"),
            'd2v-core-js':          path.resolve(__dirname, outputMin + "d2v-core-js.js"),
            'd2v-interpolate-js':          path.resolve(__dirname, outputMin + "d2v-interpolate-js.js"),
            'd2v-svg-js':          path.resolve(__dirname, outputMin + "d2v-svg-js.js"),
            'd2v-voronoi-js':          path.resolve(__dirname, outputMin + "d2v-voronoi-js.js"),
            'kotlin':           path.resolve(__dirname, outputMin + "kotlin.js")
        }
    },
    plugins: [
        new webpack.optimize.UglifyJsPlugin()
    ]
};
