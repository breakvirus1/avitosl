import React, { useEffect, useState, useContext, useRef } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import SockJS from 'sockjs-client';
import webstomp from 'webstomp-client';
import './Chat.css';

function Chat() {
  const { user, isAuthenticated } = useContext(AuthContext);
  const [stompClient, setStompClient] = useState(null);
  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const [newMessage, setNewMessage] = useState('');
  const reconnectTimeoutRef = useRef(null);
  const connectWebSocketRef = useRef(null);

  const getAuthHeaders = () => {
    if (!user || !user.access_token) {
      return null;
    }
    return { Authorization: `Bearer ${user.access_token}` };
  };

  const connectWebSocket = () => {
    const socket = new SockJS('http://127.0.0.1:8081/ws');
    const client = webstomp.over(socket, {
      debug: false,
      heartbeat: { incoming: 0, outgoing: 1000 },
      protocols: ['v12.stomp']
    });

    const headers = getAuthHeaders();
    if (!headers) {
      return;
    }

    client.connect(headers, () => {
      setStompClient(client);
      setConnected(true);
      
      client.subscribe("/topic/public", (message) => {
        const newMsg = JSON.parse(message.body);
        setMessages(prev => [...prev, {
          ...newMsg,
          timestamp: new Date()
        }]);
      });
    }, (error) => {
      console.error('WebSocket error: ', error);
      setConnected(false);
      // Try to reconnect after 3 seconds
      reconnectTimeoutRef.current = setTimeout(() => {
        if (connectWebSocketRef.current) {
          connectWebSocketRef.current();
        }
      }, 3000);
    });
  };

  useEffect(() => {
    connectWebSocketRef.current = connectWebSocket;
  });

  useEffect(() => {
    if (isAuthenticated && user?.access_token) {
      connectWebSocketRef.current();
    }

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, [isAuthenticated, user, stompClient]);

  const sendMessage = () => {
    if (!newMessage.trim() || !stompClient || !connected) {
      return;
    }

    stompClient.send("/app/chat.sendMessage", newMessage, getAuthHeaders());
    setNewMessage('');
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (!isAuthenticated || !user?.access_token) {
    return (
      <div className="chat-container">
        <div className="chat-not-authenticated">
          <p>Для доступа к чату необходимо авторизоваться</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h3>Чат</h3>
        <div className="connection-status">
          <span className={`status-indicator ${connected ? 'connected' : 'disconnected'}`}></span>
          {connected ? 'Подключено' : 'Отключено'}
        </div>
      </div>

      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="no-messages">Сообщений пока нет</div>
        ) : (
          messages.map((msg, index) => (
            <div key={index} className="message">
              <div className="message-header">
                <span className="sender">{msg.sender}</span>
                <span className="time">{formatTime(msg.timestamp)}</span>
              </div>
              <div className="message-content">{msg.content}</div>
            </div>
          ))
        )}
      </div>

      <div className="chat-input-area">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Введите сообщение..."
          disabled={!connected}
          className="chat-input"
        />
        <button
          onClick={sendMessage}
          disabled={!connected || !newMessage.trim()}
          className="send-button"
        >
          Отправить
        </button>
      </div>
    </div>
  );
}

export default Chat;