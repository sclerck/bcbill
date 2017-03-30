var webpack = require('webpack');
var path = require('path');

var definePlugin = new webpack.DefinePlugin({
  __BUILD__: JSON.stringify(JSON.parse(process.env.BUILD_APP || 'false'))
});

module.exports = {
  module: {
    loaders: []
  },
  plugins: [
    definePlugin
  ]
};


if(process.env.BUILD_APP) {
  module.exports.entry = [
    './app.js'
  ];
  module.exports.module.loaders.push({
      test: /\.js$|\.jsx$/,
      loader: 'babel-loader',
      exclude: /node_modules/,
      query:
      {
        presets:['react']
      }
  });
  module.exports.output = {
    path: path.resolve("./dist"),
    filename: 'bundle.js',
    publicPath: '/dist/'
  };
}
