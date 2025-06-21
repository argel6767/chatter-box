import { create } from 'zustand'

export const useSearchQueryStore = create((set) => ({
    searchQuery: "",
    setSearchQuery: (query: string) => set(() => ({ searchQuery: query.toLowerCase() }))
  }));

export const useUserStore = create((set) => ({
    user: {},
    setUser: (newUser: User) => set(() => ({newUser}))
}));

