import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../hooks/useAuth';
import './Chat.css';

function Chat({ receiverId, receiverName, postId, onClose }) {
  const { apiService, fetchUnreadCount, user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);
  const pollingRef = useRef(null);

  // Используем keycloakId из user (уже есть в контексте)
  const currentUserKeycloakId = user?.sub;

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Запускаем загрузку и polling при наличии receiverId и currentUserKeycloakId
  useEffect(() => {
    if (receiverId && currentUserKeycloakId) {
      fetchMessages();

      pollingRef.current = setInterval(() => {
        fetchMessages(true);
      }, 2000);

      return () => {
        if (pollingRef.current) {
          clearInterval(pollingRef.current);
        }
      };
    }
  }, [receiverId, currentUserKeycloakId]);

  const fetchMessages = async (isPolling = false) => {
    try {
      const response = await apiService.getConversation(currentUserKeycloakId, receiverId);
      const fetchedMessages = response.data || [];
      
      if (isPolling && messages.length > 0) {
        // Проверяем, есть ли новые сообщения
        const lastLocalMessage = messages[messages.length - 1];
        const lastFetchedMessage = fetchedMessages[fetchedMessages.length - 1];
        
        if (lastFetchedMessage && (!lastLocalMessage || lastFetchedMessage.id !== lastLocalMessage.id)) {
          // Есть новые сообщения, обновляем список
          setMessages(fetchedMessages);
        }
      } else {
        // Первоначальная загрузка
        setMessages(fetchedMessages);
        setLoading(false);
      }
      
      // Отмечаем все сообщения от этого отправителя как прочитанные
      if (currentUserKeycloakId && receiverId && fetchedMessages.length > 0) {
        try {
          const unreadMessages = fetchedMessages.filter(msg => !msg.isRead && msg.senderKeycloakId === receiverId);
          for (const msg of unreadMessages) {
            await apiService.markAsRead(msg.id);
          }
          if (fetchUnreadCount) {
            await fetchUnreadCount();
          }
        } catch (markReadErr) {
          console.error('Error marking messages as read:', markReadErr);
        }
      }
      
      setError(null);
    } catch (err) {
      console.error('Error fetching messages:', err);
      setError(err.response?.data?.message || 'Не удалось загрузить сообщения');
      if (!isPolling) {
        setLoading(false);
      }
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || sending) return;

    try {
      setSending(true);
      const messageData = {
        receiverKeycloakId: receiverId,
        message: newMessage.trim()
      };

      const response = await apiService.sendMessage(messageData);
      setMessages(prev => [...prev, response.data]);
      setNewMessage('');
      setError(null);
    } catch (err) {
      console.error('Error sending message:', err);
      setError(err.response?.data?.message || 'Не удалось отправить сообщение');
    } finally {
      setSending(false);
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return `Сегодня, ${formatTime(dateString)}`;
    } else if (date.toDateString() === yesterday.toDateString()) {
      return `Вчера, ${formatTime(dateString)}`;
    } else {
      return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  };

  const isMyMessage = (message) => {
    return message.senderKeycloakId === currentUserKeycloakId;
  };

  return (
    <div className="chat-overlay" onClick={onClose}>
      <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
        <div className="chat-header">
          <h3>Чат с {receiverName || 'пользователем'}</h3>
          <button className="chat-close-btn" onClick={onClose}>×</button>
        </div>

        <div className="chat-messages">
          {loading && (
            <div className="chat-loading">Загрузка сообщений...</div>
          )}

          {error && !loading && (
            <div className="chat-error">{error}</div>
          )}

          {!loading && messages.length === 0 && (
            <div className="chat-empty">Нет сообщений. Начните диалог!</div>
          )}

          {!loading && messages.map((message) => (
            <div
              key={message.id}
              className={`chat-message ${isMyMessage(message) ? 'chat-message-sent' : 'chat-message-received'}`}
            >
              <div className="chat-message-content">
                <div className="chat-message-text">{message.message}</div>
                <div className="chat-message-time">{formatDate(message.createdAt)}</div>
              </div>
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>

        <form className="chat-input-form" onSubmit={handleSendMessage}>
          <input
            type="text"
            className="chat-input"
            placeholder="Введите сообщение..."
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            disabled={sending}
          />
          <button
            type="submit"
            className="chat-send-btn"
            disabled={sending || !newMessage.trim()}
          >
            {sending ? '...' : 'Отправить'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Chat;
