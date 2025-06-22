import {create} from 'zustand'
import {User} from '@/lib/models/models'

export const useSearchQueryStore = create((set) => ({
    searchQuery: "",
    setSearchQuery: (query: string) => set(() => ({ searchQuery: query.toLowerCase() }))
  }));

interface UserStore {
    user: User;
    setUser: (newUser: User) => void;
}

export const useUserStore = create<UserStore>((set) => ({
    user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : {},
    setUser: (newUser: User) => set(() => ({ user: newUser }))
}));

