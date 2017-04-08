import ReactDOM from 'react-dom';
import { Actions } from 'react-redux-grid';
import BCBill from './provider.jsx';
import Store from './store/store.js';
import WSInstance from './websocket.js';
import * as ConnectActions from './actions/ActionsCreators.js';
import * as ActionTypes from './ActionTypes.js';
import { columns, stateKey } from './config.js'; 

const render = (id) => {
    ReactDOM.render(BCBill, document.querySelector(id));
};

document.addEventListener(
    'DOMContentLoaded', render.bind(this, '#application-mount')
);

const sock = {
  exchanges: {},
  ws: null,
  URL: 'localhost:8080',
  wsMessageDipatcher: (msg) => {
	var inbound = JSON.parse(msg);
	
	var type = inbound['type'];
	
	if(type === 'tick') {
	
		var data = inbound['data'];
		
		var exchange = data['exchange'];
		
		var exchangeIndex = sock.exchanges[exchange];
		
		if(exchangeIndex === undefined) {
			
			data['status'] = 'Live';
			
			var rowIndex = Object.keys(sock.exchanges).length;
	
			Store.dispatch(
					Actions.EditorActions.addNewRow({
						columns,
			    		data,
			    		stateKey,
			    		rowIndex: rowIndex
					}));
			    
			var values = data;
			    
			Store.dispatch(
					Actions.EditorActions.saveRow({
						values,
			    		rowIndex,
			    		stateKey
					}));
			
			sock.exchanges[exchange] = rowIndex;
		} else {
			var rowIndex = exchangeIndex;
	
			var values = data;
		    
			Store.dispatch(
					Actions.EditorActions.updateRow({
						stateKey,
						rowIndex,
						values
					}));
		}
	} else if (type === 'exchangeInfo') {
		var data = inbound['data'];
		
		var exchange = data['exchange'];
		
		var exchangeIndex = sock.exchanges[exchange];
		
		if(exchangeIndex === undefined) {
			data['timestamp'] = Date.now();
			data['bid'] = 0;
			data['ask'] = 0;
			
			var rowIndex = Object.keys(sock.exchanges).length;
			
			Store.dispatch(
					Actions.EditorActions.addNewRow({
						columns,
			    		data,
			    		stateKey,
			    		rowIndex: rowIndex
					}));
			    
			var values = data;
			    
			Store.dispatch(
					Actions.EditorActions.saveRow({
						values,
			    		rowIndex,
			    		stateKey
					}));
			
			sock.exchanges[exchange] = rowIndex;
			
		} else {
			var rowIndex = exchangeIndex;
			
			var values = data;
		    
			Store.dispatch(
					Actions.EditorActions.updateRow({
						stateKey,
						rowIndex,
						values
					}));
		}
	}
  },
  wsCloseDispatcher: () => {
    return Store.dispatch(ConnectActions.disconnect());
  },
  wsListener: () => {

	const { lastAction } = Store.getState();
	
	switch (lastAction.type) {
	  case ActionTypes.CONNECT:
	    return sock.startWS();
	
	  case ActionTypes.DISCONNECT:
	    return sock.stopWS();
	
	  default:
	    return;
	}
  },
  stopWS: () => {
    sock.ws.close();
    sock.ws = null
  },
  startWS: () => {
    if(!!sock.ws) sock.ws.close();

    sock.ws = new WSInstance(sock.URL, sock.wsMessageDipatcher, sock.wsCloseDispatcher)
  }
};

Store.subscribe(() => sock.wsListener());

