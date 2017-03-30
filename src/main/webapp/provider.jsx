import React from 'react';
import { Provider } from 'react-redux';
import { Grid, Store, applyGridConfig } from 'react-redux-grid';

import { plugins, columns, stateKey } from './config.js'; 

const config = {
	columns,
	data: [],
	stateful: false,
	stateKey,
	plugins
};

class BCBill extends React.Component {
	render() {
		return (
			<div className="bcBill">
				<div className="ticks">
					<Provider store={ Store } >
						<Grid { ...config } />
					</Provider>
				</div>
			</div>
		);
	}
}

export default (
	<BCBill />
);