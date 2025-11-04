// src/lib/savedStore.js
// Namespaced saved-jobs storage tied to a specific user id.

const keyForUser = (userId) => `ih:saved:${userId ?? "anon"}`;

export function loadSaved(userId) {
    try {
        const raw = localStorage.getItem(keyForUser(userId));
        return raw ? JSON.parse(raw) : [];
    } catch {
        return [];
    }
}

export function saveSaved(userId, items) {
    localStorage.setItem(keyForUser(userId), JSON.stringify(items ?? []));
}

const makeId = () => `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;

export function addSaved(userId, job) {
    const list = loadSaved(userId);

    // ensure each saved job has a stable id
    const withId = job.id ? job : { ...job, id: makeId() };

    if (!list.some((j) => j.id === withId.id)) {
        list.push(withId);
        saveSaved(userId, list);
    }
    return list;
}

export function removeSaved(userId, jobId) {
    const list = loadSaved(userId).filter((j) => j.id !== jobId);
    saveSaved(userId, list);
    return list;
}

export function clearSaved(userId) {
    localStorage.removeItem(keyForUser(userId));
}
