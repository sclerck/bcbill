import { combineReducers } from 'redux';
import { Reducers as gridReducers } from 'react-redux-grid';

import connectionStatus from './connectionStatus.js';
import lastAction from './lastAction.js';

export const rootReducer = combineReducers({
	connectionStatus,
	lastAction,
    dataSource: gridReducers.dataSource,
    editor: gridReducers.editor,
    grid: gridReducers.grid
});

export default rootReducer;

