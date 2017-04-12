import React from 'react';
import { Provider } from 'react-redux';
import { Grid, applyGridConfig } from 'react-redux-grid';

import Store from './store/store.js';
import { plugins, columns, stateKey } from './config.js'; 
import Connect from './components/connect.js';

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
			<Provider store={ Store } >
				<div className="bcBill">
		    		<Connect />
					<div className="ticks">
						<Grid { ...config } />
					</div>
				</div>
			</Provider>
		);
	}
}

export default (
	<BCBill />
);