import { Client } from '@stomp/stompjs';
import { useNotificationStore } from '@/stores/notification';
import { useSettlementStore } from '@/stores/settlement';
import { useFriendStore } from '@/stores/friend';

let client = null;

export const connectStomp = (token) => {
  if (client?.active) return;

  const notificationStore = useNotificationStore();
  const settlementStore = useSettlementStore();
  const friendStore = useFriendStore();

  client = new Client({
    brokerURL: 'ws://localhost:8080/ws',

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    reconnectDelay: 5000,

    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,

    debug: (str) => console.log(str),
  });

  client.onConnect = () => {
    console.log('웹소켓 연결 성공');

    // 알림
    client.subscribe('/user/queue/notifications', (message) => {
      const notification = JSON.parse(message.body);

      console.log('웹소켓 알림 수신:', notification);

      notificationStore.addNotification(notification);

      window.dispatchEvent(
        new CustomEvent('notification-received', {
          detail: notification,
        }),
      );
    });

    // 정산
    client.subscribe('/user/queue/settlements', (message) => {
      const data = JSON.parse(message.body);

      console.log('정산 웹소켓 원본:', data);
      console.log('정산 members:', data.settlement?.members);

      if (!data?.type || !data?.settlement) {
        return;
      }

      switch (data.type) {
        case 'CREATE':
          settlementStore.addSettlement(data.settlement);
          break;

        case 'UPDATE':
          settlementStore.updateSettlement(data.settlement);
          break;

        default:
          console.warn('알 수 없는 정산 웹소켓 타입:', data.type);
      }
    });

    // 친구
    client.subscribe('/user/queue/friends', (message) => {
      const friendEvent = JSON.parse(message.body);

      console.log('친구 웹소켓:', friendEvent);

      friendStore.handleFriendEvent(friendEvent);
    });
  };

  client.onDisconnect = () => {
    console.log('웹소켓 연결 종료');
  };

  client.onStompError = (frame) => {
    console.error('STOMP ERROR', frame);
  };

  client.onWebSocketClose = (event) => {
    console.warn('WebSocket 연결 종료:', event);
  };

  client.onWebSocketError = (event) => {
    console.error('WebSocket 오류:', event);
  };

  client.onStompError = (frame) => {
    console.error('STOMP ERROR:', frame);
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
