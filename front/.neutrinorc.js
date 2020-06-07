const react = require('@neutrinojs/react');

module.exports = {
  use: [
    react({
      hot: true,
      publicPath: '/',
      html: {
        title: 'OpenSearchServer',
      },
    })
  ]
};
