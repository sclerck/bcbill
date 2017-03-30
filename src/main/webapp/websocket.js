export default class WS {
  constructor(url, messageDispatcher, closeDispatcher) {
    this.websocket = new WebSocket(`ws://${url}`);
    this.messageDispatcher = messageDispatcher
    this.closeDispatcher = closeDispatcher
    this.websocket.onmessage = function (event) {
      messageDispatcher(event.data)
    }
    this.websocket.onclose = function () {
      closeDispatcher()
    }
  }

  close() {
    this.websocket.close();
  }

}
