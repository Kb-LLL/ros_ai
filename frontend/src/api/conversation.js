import request from './auth.js'

export const conversationApi = {
    create:      ()           => request.post('/conversation/create'),
    list:        ()           => request.get('/conversation/list'),
    getMessages: (id)         => request.get(`/conversation/${id}/messages`),
    saveMessage: (id, role, content) =>
        request.post(`/conversation/${id}/message`, { role, content }),
    rename:      (id, title)  => request.put(`/conversation/${id}/rename`, { title }),
    remove:      (id)         => request.delete(`/conversation/${id}`),
}