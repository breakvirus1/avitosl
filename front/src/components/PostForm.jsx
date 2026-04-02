import { useState } from 'react'

function PostForm({ onFinish, categories }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    categoryId: '',
    subcategoryId: ''
  })
  const [errors, setErrors] = useState({})

  const validateForm = () => {
    const newErrors = {}

    if (!formData.name.trim()) {
      newErrors.name = 'Пожалуйста, введите название!'
    } else if (formData.name.length < 3) {
      newErrors.name = 'Название должно содержать минимум 3 символа'
    }

    if (formData.description && formData.description.length > 5000) {
      newErrors.description = 'Описание не должно превышать 5000 символов'
    }

    if (!formData.price && formData.price !== 0) {
      newErrors.price = 'Пожалуйста, укажите цену!'
    } else if (formData.price && parseFloat(formData.price) < 0) {
      newErrors.price = 'Цена не может быть отрицательной'
    } else if (formData.price && parseFloat(formData.price) > 99999999.99) {
      newErrors.price = 'Цена не может превышать 99,999,999.99'
    }

    if (!formData.categoryId) {
      newErrors.categoryId = 'Пожалуйста, выберите категорию!'
    }

    if (!formData.subcategoryId) {
      newErrors.subcategoryId = 'Пожалуйста, выберите подкатегорию!'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (validateForm()) {
      try {
        const postData = {
          title: formData.name.trim(),
          description: formData.description || '',
          price: parseFloat(formData.price),
          categoryId: formData.categoryId ? parseInt(formData.categoryId) : undefined,
          subcategoryId: formData.subcategoryId ? parseInt(formData.subcategoryId) : undefined
        }
        await onFinish(postData)
        setFormData({
          name: '',
          description: '',
          price: '',
          categoryId: '',
          subcategoryId: ''
        })
      } catch (err) {
        console.error('Submit error:', err)
      }
    }
  }

  const selectedCategory = categories.find(cat => cat.id === parseInt(formData.categoryId))

  const inputStyle = {
    width: '100%',
    padding: '8px 12px',
    border: '1px solid #d9d9d9',
    borderRadius: '4px',
    fontSize: '14px',
    marginTop: '4px'
  }

  const inputFocusStyle = {
    ...inputStyle,
    borderColor: '#1890ff',
    outline: 'none',
    boxShadow: '0 0 0 2px rgba(24,144,255,0.2)'
  }

  const labelStyle = {
    display: 'block',
    marginBottom: '8px',
    fontWeight: 500,
    fontSize: '14px',
    color: '#000000d9'
  }

  const errorStyle = {
    color: '#ff4d4f',
    fontSize: '12px',
    marginTop: '4px'
  }

  const buttonStyle = {
    background: '#1890ff',
    color: 'white',
    border: 'none',
    padding: '12px 24px',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '16px',
    width: '100%',
    marginTop: '16px'
  }

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        background: '#fafafa',
        padding: '24px',
        borderRadius: '8px',
        border: '1px solid #d9d9d9',
        position: 'sticky',
        top: '24px'
      }}
    >
      <div style={{ marginBottom: '16px' }}>
        <label style={labelStyle}>Название *</label>
        <input
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Введите название объявления"
          style={inputStyle}
          onFocus={(e) => e.target.style = { ...inputFocusStyle }}
          onBlur={(e) => e.target.style = inputStyle}
        />
        {errors.name && <div style={errorStyle}>{errors.name}</div>}
      </div>

      <div style={{ marginBottom: '16px' }}>
        <label style={labelStyle}>Описание</label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          rows={4}
          placeholder="Опишите ваше объявление"
          style={{ ...inputStyle, resize: 'vertical' }}
        />
        {errors.description && <div style={errorStyle}>{errors.description}</div>}
      </div>

      <div style={{ marginBottom: '16px' }}>
        <label style={labelStyle}>Цена *</label>
        <input
          type="number"
          name="price"
          value={formData.price}
          onChange={handleChange}
          placeholder="0.00"
          min="0"
          max="99999999.99"
          step="0.01"
          style={inputStyle}
        />
        {errors.price && <div style={errorStyle}>{errors.price}</div>}
      </div>

      <div style={{ marginBottom: '16px' }}>
        <label style={labelStyle}>Категория</label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          style={inputStyle}
        >
          <option value="">Выберите категорию</option>
          {categories.map(cat => (
            <option key={cat.id} value={cat.id}>{cat.name}</option>
          ))}
        </select>
        {errors.categoryId && <div style={errorStyle}>{errors.categoryId}</div>}
      </div>

      <div style={{ marginBottom: '16px' }}>
        <label style={labelStyle}>Подкатегория</label>
        <select
          name="subcategoryId"
          value={formData.subcategoryId}
          onChange={handleChange}
          style={inputStyle}
          disabled={!selectedCategory}
        >
          <option value="">Выберите подкатегорию</option>
          {selectedCategory?.subcategories?.map(sub => (
            <option key={sub.id} value={sub.id}>{sub.name}</option>
          ))}
        </select>
        {errors.subcategoryId && <div style={errorStyle}>{errors.subcategoryId}</div>}
      </div>

      <button type="submit" style={buttonStyle}>
        Создать объявление
      </button>
    </form>
  )
}

export default PostForm
