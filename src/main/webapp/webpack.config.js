var webpack = require("webpack");

var output_options = {
    path: __dirname + '/assets/',
    filename: '[name].js',
    chunkFilename: '[name].js',
    publicPath: '/nettysocketio-demo/assets/'
};
var plugins_options = [];
plugins_options.push(new webpack.SourceMapDevToolPlugin({
    test:      /\.(js|css)($|\?)/i,
    filename: 'maps/[file].map'
}));

module.exports = {
    entry: {
        main: __dirname + '/web-src/js/main.js'
    },
    output: output_options,
    plugins: plugins_options,
    cache: true,
    watch: true,
    resolve: {
        extensions: ['', '.js']
    }
};