import { Client } from '@stomp/stompjs';
import { useNotificationStore } from '@/stores/notification';
import { useSettlementStore } from '@/stores/settlement';

let client = null;

export const connectStomp = (userId) => {
  // 이미 연결 중이거나 연결되어 있으면 종료
  if (client?.active) return;

  const notificationStore = useNotificationStore();
  const settlementStore = useSettlementStore();

  client = new Client({
    brokerURL: 'ws://localhost:8080/ws',

    connectHeaders: {
      userId: String(userId),
    },

    reconnectDelay: 5000,

    debug: (str) => console.log(str),
  });

  client.onConnect = () => {
    console.log('웹소켓 연결 성공');

    client.subscribe('/user/queue/notifications', (message) => {
      const notification = JSON.parse(message.body);
      console.log('웹소켓 알림 수신:', notification);
      notificationStore.addNotification(notification);
      window.dispatchEvent(new CustomEvent('notification-received', { detail: notification }));
    });

    client.subscribe('/user/queue/settlements', (message) => {
      const settlement = JSON.parse(message.body);
      console.log(settlement);
      settlementStore.updateSettlement(settlement);
    });
  };

  client.onDisconnect = () => {
    console.log('웹소켓 연결 종료');
  };

  client.onStompError = (frame) => {
    console.error('STOMP ERROR', frame);
  };

  client.activate();
};

export const disconnectStomp = async () => {
  if (!client) return;

  try {
    await client.deactivate();
  } catch (e) {
    console.log('WS deactivation bypass');
  }

  client = null;
};

export const getStompClient = () => client;
