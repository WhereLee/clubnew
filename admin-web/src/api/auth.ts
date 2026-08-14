import request from '../utils/request'

export const login = (data: { username: string; password: string }) =>
  request.post('/auth/login', data)

export const logout = () =>
  request.post('/auth/logout', { refreshToken: localStorage.getItem('refreshToken') })

export const getInfo = () => request.get('/getInfo')

export const getRouters = () => request.get('/getRouters')
