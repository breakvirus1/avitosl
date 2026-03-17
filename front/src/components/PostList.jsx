import React from 'react'

function PostList({ posts, onDelete, loading, isAdmin, pagination, onPageChange }) {
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <div style={{
          display: 'inline-block',
          width: '40px',
          height: '40px',
          border: '4px solid #f3f3f3',
          borderTop: '4px solid #1890ff',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite'
        }}></div>
        <div style={{ marginTop: '16px' }}>Загрузка объявлений...</div>
      </div>
    )
  }

  if (!posts || posts.length === 0) {
    return (
      <div style={{
        textAlign: 'center',
        padding: '50px',
        background: '#fafafa',
        borderRadius: '8px',
        border: '1px dashed #d9d9d9'
      }}>
        <div style={{ fontSize: '48px', marginBottom: '16px' }}>📭</div>
        <p style={{ fontSize: '16px', color: '#666', marginBottom: '16px' }}>Нет объявлений</p>
        <button
          style={{
            background: '#1890ff',
            color: 'white',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Создать первое объявление
        </button>
      </div>
    )
  }

  return (
    <div>
      <h2 style={{ marginBottom: '24px' }}>Объявления ({pagination.totalElements})</h2>
      <div>
        {posts.map((post) => (
          <div
            key={post.id}
            style={{
              background: '#fff',
              border: '1px solid #f0f0f0',
              borderRadius: '8px',
              marginBottom: '16px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.06)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <h3 style={{ margin: '0 0 16px 0', fontSize: '20px' }}>
                  {post.name}
                </h3>
                <div style={{ marginBottom: '12px' }}>
                  {post.category && (
                    <span style={{
                      background: '#e6f7ff',
                      color: '#1890ff',
                      padding: '2px 8px',
                      borderRadius: '4px',
                      fontSize: '12px',
                      marginRight: '8px'
                    }}>
                      {post.category.name}
                    </span>
                  )}
                  {post.subcategory && (
                    <span style={{
                      background: '#f6ffed',
                      color: '#52c41a',
                      padding: '2px 8px',
                      borderRadius: '4px',
                      fontSize: '12px'
                    }}>
                      {post.subcategory.name}
                    </span>
                  )}
                </div>
                <p style={{ color: '#666', marginBottom: '12px' }}>
                  {post.description || 'Без описания'}
                </p>
                <div style={{ display: 'flex', gap: '16px', alignItems: 'center', flexWrap: 'wrap' }}>
                  <strong style={{ fontSize: '18px', color: '#f5222d' }}>
                    {post.price?.toLocaleString()} ₽
                  </strong>
                  <span style={{ color: '#999' }}>
                    Автор: {post.author?.firstName} {post.author?.lastName || ''}
                  </span>
                  <span style={{ color: '#999' }}>
                    {new Date(post.createdAt).toLocaleDateString('ru-RU')}
                  </span>
                </div>
              </div>
              {isAdmin && (
                <button
                  onClick={() => onDelete(post.id)}
                  style={{
                    background: '#fff',
                    color: '#ff4d4f',
                    border: '1px solid #ffccc7',
                    padding: '4px 12px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}
                >
                  🗑️ Удалить
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {pagination.totalPages > 1 && (
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          marginTop: '24px',
          gap: '8px'
        }}>
          <button
            onClick={() => onPageChange(0)}
            disabled={pagination.currentPage === 0}
            style={{
              padding: '8px 16px',
              border: '1px solid #d9d9d9',
              borderRadius: '4px',
              background: pagination.currentPage === 0 ? '#f5f5f5' : '#fff',
              cursor: pagination.currentPage === 0 ? 'not-allowed' : 'pointer',
              color: pagination.currentPage === 0 ? '#999' : '#1890ff'
            }}
          >
            Первая
          </button>
          <button
            onClick={() => onPageChange(pagination.currentPage - 1)}
            disabled={pagination.currentPage === 0}
            style={{
              padding: '8px 16px',
              border: '1px solid #d9d9d9',
              borderRadius: '4px',
              background: pagination.currentPage === 0 ? '#f5f5f5' : '#fff',
              cursor: pagination.currentPage === 0 ? 'not-allowed' : 'pointer',
              color: pagination.currentPage === 0 ? '#999' : '#1890ff'
            }}
          >
            Назад
          </button>
          <span style={{ padding: '8px 16px', color: '#666' }}>
            Страница {pagination.currentPage + 1} из {pagination.totalPages}
          </span>
          <button
            onClick={() => onPageChange(pagination.currentPage + 1)}
            disabled={pagination.currentPage >= pagination.totalPages - 1}
            style={{
              padding: '8px 16px',
              border: '1px solid #d9d9d9',
              borderRadius: '4px',
              background: pagination.currentPage >= pagination.totalPages - 1 ? '#f5f5f5' : '#fff',
              cursor: pagination.currentPage >= pagination.totalPages - 1 ? 'not-allowed' : 'pointer',
              color: pagination.currentPage >= pagination.totalPages - 1 ? '#999' : '#1890ff'
            }}
          >
            Вперед
          </button>
          <button
            onClick={() => onPageChange(pagination.totalPages - 1)}
            disabled={pagination.currentPage >= pagination.totalPages - 1}
            style={{
              padding: '8px 16px',
              border: '1px solid #d9d9d9',
              borderRadius: '4px',
              background: pagination.currentPage >= pagination.totalPages - 1 ? '#f5f5f5' : '#fff',
              cursor: pagination.currentPage >= pagination.totalPages - 1 ? 'not-allowed' : 'pointer',
              color: pagination.currentPage >= pagination.totalPages - 1 ? '#999' : '#1890ff'
            }}
          >
            Последняя
          </button>
        </div>
      )}
    </div>
  )
}

export default PostList
