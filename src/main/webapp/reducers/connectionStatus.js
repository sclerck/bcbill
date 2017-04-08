'use strict';

import { DISCONNECT, CONNECT } from '../ActionTypes';

const initialState = {
  status: false
};

export default function connectionStatus(state = initialState, action) {

  switch (action.type) {

    case CONNECT:
      return {
        status: true
      }

    case DISCONNECT:
      return {
        status: false
      }

    default:
      return state;
  }
}
