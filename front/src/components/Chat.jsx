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
    } catch (err) {
      console.error('Error fetching messages:', err);
      setError('Не удалось загрузить сообщения');
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || !receiverId || !currentUserKeycloakId) return;

    setSending(true);
    setError(null);

    try {
      await apiService.sendMessage({
        senderId: currentUserKeycloakId,
        receiverId: receiverId,
        content: newMessage.trim(),
        postId: postId
      });
      setNewMessage('');
      fetchMessages();
      fetchUnreadCount();
    } catch (err) {
      console.error('Error sending message:', err);
      setError('Не удалось отправить сообщение');
    } finally {
      setSending(false);
    }
  };

  if (!receiverId || !currentUserKeycloakId) {
    return (
      <div className="chat-container">
        <div className="chat-error">Необходимо выбрать получателя</div>
      </div>
    );
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h3>{receiverName || 'Чат'}</h3>
        <button className="close-btn" onClick={onClose}>&times;</button>
      </div>

      <div className="chat-messages">
        {loading ? (
          <div className="chat-loading">Загрузка сообщений...</div>
        ) : error ? (
          <div className="chat-error">{error}</div>
        ) : messages.length === 0 ? (
          <div className="chat-empty">Нет сообщений</div>
        ) : (
          messages.map((msg) => (
            <div key={msg.id} className={`chat-message ${msg.senderId === currentUserKeycloakId ? 'own' : 'other'}`}>
              <div className="message-content">{msg.content}</div>
              <div className="message-time">
                {new Date(msg.createdAt).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input-form" onSubmit={sendMessage}>
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Введите сообщение..."
          disabled={sending}
        />
        <button type="submit" disabled={sending || !newMessage.trim()}>
          {sending ? '...' : 'Отправить'}
        </button>
      </form>
    </div>
  );
}

export default Chat;
