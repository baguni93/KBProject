import { Client } from '@stomp/stompjs';

const userId = 3;

const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',

  connectHeaders: {
    userId: String(userId),
  },

  reconnectDelay: 5000,

  debug: (str) => console.log(str),
});

export default client;
