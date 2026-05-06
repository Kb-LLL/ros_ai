import request from './auth.js'

export const conversationApi = {
    create:      ()           => request.post('/conversation/create'),
    list:        ()           => request.get('/conversation/list'),
    getMessages: (id)         => request.get(`/conversation/${id}/messages`),
    getDocuments: (id)        => request.get(`/conversation/${id}/documents`),
    saveMessage: (id, role, content) =>
        request.post(`/conversation/${id}/message`, { role, content }),
    bindDocuments: (id, documentIds) =>
        request.put(`/conversation/${id}/documents`, { documentIds }),
    rename:      (id, title)  => request.put(`/conversation/${id}/rename`, { title }),
    remove:      (id)         => request.delete(`/conversation/${id}`),
    deleteLastAiMessage: (id) => request.delete(`/conversation/${id}/last-ai-message`),
}
