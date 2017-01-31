var webpack = require('webpack');

var webpackConfig = {
    entry: {
        index: './index.js'
    },
    output: {
        path: './static',
        filename: '[name].js',
        publicPath: '/'
    },
    resolve: {
        extensions: ['', '.js'],
        modulesDirectories: ['node_modules', './build/libs']
    },
    module: {
        loaders: [
            {
                test: /\.css$/,
                loaders: [
                    'style',
                    'css',
                    'postcss?pack=circlet'
                ]
            }
        ]
    }
};

module.exports = webpackConfig;
