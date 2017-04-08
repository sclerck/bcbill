import { CONNECT, DISCONNECT } from '../ActionTypes';

export function connect() {
  return {
    type: CONNECT
  }
}

export function disconnect() {
  return {
    type: DISCONNECT
  }
}
