import { create } from 'zustand'
import {User} from '@/lib/models/models'

export const useSearchQueryStore = create((set) => ({
    searchQuery: "",
    setSearchQuery: (query: string) => set(() => ({ searchQuery: query.toLowerCase() }))
  }));

export const useUserStore = create((set) => ({
    user: {},
    setUser: (newUser: User) => set(() => ({newUser}))
}));

