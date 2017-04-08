import { createStore, combineReducers, applyMiddleware, compose  } from 'redux';
import thunk from 'redux-thunk';
import createLogger from 'redux-logger';
import rootReducer from '../reducers';

export default function configureStore(initialState) {
   
   const logger = createLogger(initialState);
   const store = createStore(rootReducer, initialState, compose(
      applyMiddleware(
        thunk,
        logger
      )
   ));	

  if (module.hot) {
    module.hot.accept('./reducers/', () => {
      const nextRootReducer = require('./reducers/index.js');
      store.replaceReducer(nextRootReducer);
    });
  }

  return store;
}
